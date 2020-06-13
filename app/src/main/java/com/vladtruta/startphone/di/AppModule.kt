package com.vladtruta.startphone.di

import android.app.Application
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
import com.vladtruta.startphone.presentation.viewmodel.OnboardingViewModel
import com.vladtruta.startphone.presentation.viewmodel.SignUpViewModel
import com.vladtruta.startphone.presentation.viewmodel.VisibleAppsViewModel
import com.vladtruta.startphone.repository.ApplicationRepository
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.repository.WeatherRepository
import com.vladtruta.startphone.util.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { provideGson() }

    single { provideApplication() }
    single { provideWifiManager(get()) }
    single { provideTelephonyManager(get()) }
    single { provideConnectivityManager(get()) }
    single { provideBatteryManager(get()) }
    single { providePackageManager(get()) }
    single { provideWindowManager(get()) }
    single { provideFusedLocationProviderClient(get()) }
    single { provideNotificationManager(get()) }

    single { BatteryStatusHelper(get()) }
    single { DateTimeHelper() }
    single { LocationHelper(get()) }
    single { MobileSignalHelper(get()) }
    single { NetworkConnectivityHelper(get()) }
    single { WifiConnectionHelper(get()) }
    single { ImageHelper() }
    single { NotificationsHelper(get()) }
    single { LauncherApplicationsHelper(get()) }
    single { PreferencesHelper(get()) }

    single<IWeatherRepo> { WeatherRepository(get()) }
    single<IAppRepo> { ApplicationRepository(get(), get()) }

    viewModel { LauncherViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { SignUpViewModel() }
    viewModel { VisibleAppsViewModel(get()) }
}

private fun provideGson(): Gson {
    return Gson()
}

private fun provideApplication(): StartphoneApp {
    return StartphoneApp.instance
}

private fun provideWifiManager(application: Application): WifiManager {
    return application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}

private fun provideTelephonyManager(application: Application): TelephonyManager {
    return application.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}

private fun provideConnectivityManager(application: Application): ConnectivityManager {
    return application.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}

private fun provideBatteryManager(application: Application): BatteryManager {
    return application.applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
}

private fun provideWindowManager(application: Application): WindowManager {
    return application.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
}

private fun provideNotificationManager(application: Application): NotificationManager? {
    return application.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
}

private fun providePackageManager(application: Application): PackageManager {
    return application.packageManager
}

private fun provideFusedLocationProviderClient(application: Application): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(application.applicationContext)
}
