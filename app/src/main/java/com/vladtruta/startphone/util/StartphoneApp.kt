package com.vladtruta.startphone.util

import android.app.Application
import com.vladtruta.startphone.di.appModule
import com.vladtruta.startphone.di.networkModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class StartphoneApp : Application() {
    private val batteryStatusHelper by inject<BatteryStatusHelper>()
    private val dateTimeHelper by inject<DateTimeHelper>()
    private val mobileSignalHelper by inject<MobileSignalHelper>()
    private val wifiConnectionHelper by inject<WifiConnectionHelper>()
    private val networkConnectivityHelper by inject<NetworkConnectivityHelper>()

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
    }

    override fun onTerminate() {
        batteryStatusHelper.unregisterBatteryReceiver(this)
        dateTimeHelper.unregisterTimeTickReceiver(this)
        mobileSignalHelper.unregisterPhoneStateListener()
        wifiConnectionHelper.unregisterWifiConnectionReceiver(this)
        networkConnectivityHelper.unregisterNetworkCallback()

        super.onTerminate()
    }
}