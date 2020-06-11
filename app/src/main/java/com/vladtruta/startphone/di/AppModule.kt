package com.vladtruta.startphone.di

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.telephony.TelephonyManager
import android.view.WindowManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.vladtruta.startphone.presentation.viewmodel.LauncherViewModel
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.repository.WeatherRepository
import com.vladtruta.startphone.util.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Gson() }

    single { provideWifiManager() }
    single { provideTelephonyManager() }
    single { provideConnectivityManager() }
    single { provideBatteryManager() }
    single { providePackageManager() }
    single { provideWindowManager() }
    single { provideFusedLocationProviderClient() }
    single { provideNotificationManager() }

    single { BatteryStatusHelper(get()) }
    single { DateTimeHelper() }
    single { LocationHelper(get()) }
    single { MobileSignalHelper(get()) }
    single { NetworkConnectivityHelper(get()) }
    single { WifiConnectionHelper(get()) }
    single { ImageHelper() }
    single { LauncherApplicationsHelper(get()) }

    single<IWeatherRepo> { WeatherRepository(get()) }

    viewModel { LauncherViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}

private fun provideWifiManager(): WifiManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}

private fun provideTelephonyManager(): TelephonyManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}

private fun provideConnectivityManager(): ConnectivityManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}

private fun provideBatteryManager(): BatteryManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
}

private fun provideWindowManager(): WindowManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
}

private fun providePackageManager(): PackageManager {
    return StartphoneApp.instance.packageManager
}

private fun provideNotificationManager(): NotificationManager? {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
}

private fun provideFusedLocationProviderClient(): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(StartphoneApp.instance.applicationContext)
}
