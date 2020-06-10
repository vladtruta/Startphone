package com.vladtruta.startphone.util

import android.app.Application
import com.vladtruta.startphone.di.appModule
import com.vladtruta.startphone.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class StartphoneApp : Application() {
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

        BatteryStatusHelper.registerBatteryReceiver(this)
        DateTimeHelper.registerTimeTickReceiver(this)
        MobileSignalHelper.registerPhoneStateListener()
        WifiConnectionHelper.registerWifiConnectionReceiver(this)
        NetworkConnectivityHelper.registerNetworkCallback()
    }

    override fun onTerminate() {
        BatteryStatusHelper.unregisterBatteryReceiver(this)
        DateTimeHelper.unregisterTimeTickReceiver(this)
        MobileSignalHelper.unregisterPhoneStateListener()
        WifiConnectionHelper.unregisterWifiConnectionReceiver(this)
        NetworkConnectivityHelper.unregisterNetworkCallback()

        super.onTerminate()
    }
}