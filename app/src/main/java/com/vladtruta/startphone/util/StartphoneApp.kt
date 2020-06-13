package com.vladtruta.startphone.util

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.vladtruta.startphone.di.appModule
import com.vladtruta.startphone.di.networkModule
import com.vladtruta.startphone.service.HelpingHandService
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class StartphoneApp : Application(), LifecycleObserver {

    private val batteryStatusHelper by inject<BatteryStatusHelper>()
    private val dateTimeHelper by inject<DateTimeHelper>()
    private val mobileSignalHelper by inject<MobileSignalHelper>()
    private val wifiConnectionHelper by inject<WifiConnectionHelper>()
    private val networkConnectivityHelper by inject<NetworkConnectivityHelper>()
    private val preferencesHelper by inject<PreferencesHelper>()

    private lateinit var helpingHandIntent: Intent

    companion object {
        lateinit var instance: StartphoneApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidContext(this@StartphoneApp)
            androidLogger()
            modules(appModule + networkModule)
        }

        batteryStatusHelper.registerBatteryReceiver(this)
        dateTimeHelper.registerTimeTickReceiver(this)
        mobileSignalHelper.registerPhoneStateListener()
        wifiConnectionHelper.registerWifiConnectionReceiver(this)
        networkConnectivityHelper.registerNetworkCallback()

        helpingHandIntent = Intent(this, HelpingHandService::class.java)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppEnteredBackground() {
        if (hasDrawOverlayPermissions() && preferencesHelper.isHelpingHandEnabled()) {
            startService(helpingHandIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppEnteredForeground() {
        if (hasDrawOverlayPermissions()) {
            stopService(helpingHandIntent)
        }
    }

    val isInForeground: Boolean
        get() = ProcessLifecycleOwner.get().lifecycle.currentState >= Lifecycle.State.STARTED

    fun hasDrawOverlayPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(instance)
        } else {
            true
        }
    }

    override fun onTerminate() {
        batteryStatusHelper.unregisterBatteryReceiver(this)
        dateTimeHelper.unregisterTimeTickReceiver(this)
        mobileSignalHelper.unregisterPhoneStateListener()
        wifiConnectionHelper.unregisterWifiConnectionReceiver(this)
        networkConnectivityHelper.unregisterNetworkCallback()

        stopService(helpingHandIntent)

        super.onTerminate()
    }
}