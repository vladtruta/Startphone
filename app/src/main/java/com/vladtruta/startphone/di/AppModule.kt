package com.vladtruta.startphone.di

import android.content.Context
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.telephony.TelephonyManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.vladtruta.startphone.presentation.viewmodel.LauncherViewModel
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.repository.WeatherRepository
import com.vladtruta.startphone.util.StartphoneApp
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { provideWifiManager() }

    single { provideTelephonyManager() }

    single { provideBatteryManager() }

    single { provideFusedLocationProviderClient() }

    single { Gson() }

    single<IWeatherRepo> { WeatherRepository(get()) }

    viewModel { LauncherViewModel(get()) }
}

private fun provideWifiManager(): WifiManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}

private fun provideTelephonyManager(): TelephonyManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}

private fun provideBatteryManager(): BatteryManager {
    return StartphoneApp.instance.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
}

private fun provideFusedLocationProviderClient(): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(StartphoneApp.instance.applicationContext)
}