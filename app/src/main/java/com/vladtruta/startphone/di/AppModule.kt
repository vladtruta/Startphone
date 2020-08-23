package com.vladtruta.startphone.di

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.vladtruta.startphone.presentation.viewmodel.*
import com.vladtruta.startphone.repository.ApplicationRepository
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.repository.WeatherRepository
import com.vladtruta.startphone.util.*
import com.vladtruta.startphone.work.WorkHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@SuppressLint("NewApi")
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
    single { provideRoleManager(get()) }

    single { BatteryStatusHelper(get()) }
    single { DateTimeHelper() }
    single { LocationHelper(get()) }
    single { MobileSignalHelper(get()) }
    single { NetworkConnectivityHelper(get()) }
    single { WifiConnectionHelper(get()) }
    single { ImageHelper() }
    single { NotificationsHelper(get()) }
    single { LauncherApplicationsHelper(get()) }
    single { PreferencesHelper(get(), get()) }
    single { WorkHelper(get()) }

    single<IWeatherRepo> { WeatherRepository(get(), provideIODispatcher()) }
    single<IAppRepo> { ApplicationRepository(get(), get(), get(), provideIODispatcher()) }

    viewModel { OnboardingViewModel(get(), get(), get()) }
    viewModel { SignUpViewModel() }
    viewModel { SystemSetupViewModel() }
    viewModel { VisibleAppsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
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

@RequiresApi(Build.VERSION_CODES.Q)
private fun provideRoleManager(application: Application): RoleManager? {
    return application.applicationContext.getSystemService(Context.ROLE_SERVICE) as RoleManager
}

private fun providePackageManager(application: Application): PackageManager {
    return application.packageManager
}

private fun provideFusedLocationProviderClient(application: Application): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(application.applicationContext)
}

private fun provideIODispatcher(): CoroutineDispatcher {
    return Dispatchers.IO
}

private fun provideDefaultDispatcher(): CoroutineDispatcher {
    return Dispatchers.Default
}
