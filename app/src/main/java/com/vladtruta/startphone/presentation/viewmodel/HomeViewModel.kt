package com.vladtruta.startphone.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladtruta.startphone.R
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.FormattedDateTime
import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val weatherRepository: IWeatherRepo,
    private val applicationRepository: IAppRepo,
    private val batteryStatusHelper: BatteryStatusHelper,
    private val networkConnectivityHelper: NetworkConnectivityHelper,
    private val wifiConnectionHelper: WifiConnectionHelper,
    private val mobileSignalHelper: MobileSignalHelper,
    private val locationHelper: LocationHelper,
    private val dateTimeHelper: DateTimeHelper,
    private val launcherApplicationsHelper: LauncherApplicationsHelper,
    private val preferencesHelper: PreferencesHelper
) : ViewModel(),
    LocationHelper.LocationListener,
    BatteryStatusHelper.BatteryStatusListener,
    DateTimeHelper.DateTimeListener,
    MobileSignalHelper.MobileSignalListener,
    WifiConnectionHelper.WifiConnectionListener,
    NetworkConnectivityHelper.NetworkConnectivityListener {

    companion object {
        private const val TAG = "LauncherViewModel"

        private const val MAX_APPLICATIONS_PER_PAGE = 6

        private val addHelpButton = false
    }

    private val _currentWeather = MutableLiveData<Result<Weather>>()
    val currentWeather: LiveData<Result<Weather>> = _currentWeather

    private val _batteryLevel = MutableLiveData(batteryStatusHelper.getBatteryStatus())
    val batteryLevel: LiveData<Pair<Int, Boolean>> = _batteryLevel

    private val _dateTime = MutableLiveData(dateTimeHelper.getCurrentDateAndTime())
    val dateTime: LiveData<FormattedDateTime> = _dateTime

    private val _networkConnected = MutableLiveData(networkConnectivityHelper.isNetworkConnected())
    val networkConnected: LiveData<Boolean> = _networkConnected

    private val _mobileSignalStrength =
        MutableLiveData(mobileSignalHelper.calculateSignalStrength())

    val mobileSignalStrength: LiveData<Int> = _mobileSignalStrength
    private val _wifiSignalLevel = MutableLiveData(wifiConnectionHelper.calculateSignalLevel())

    private var _networkConnectionStrength = MutableLiveData<Int>(
        if (networkConnectivityHelper.isWifiConnected()) {
            _wifiSignalLevel.value
        } else {
            _mobileSignalStrength.value
        }
    )
    val networkConnectionStrength: LiveData<Int> = _networkConnectionStrength

    @Suppress("ConstantConditionIf")
    private val _visibleAppLists = MutableLiveData<List<List<ApplicationInfo>>>(emptyList())
    val visibleAppLists: LiveData<List<List<ApplicationInfo>>> = _visibleAppLists

    init {
        batteryStatusHelper.addListener(this)
        dateTimeHelper.addListener(this)
        mobileSignalHelper.addListener(this)
        wifiConnectionHelper.addListener(this)
        networkConnectivityHelper.addListener(this)
        locationHelper.addListener(this)
        locationHelper.requestLastKnownLocation()
    }

    override fun onLastLocationRetrieved(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val weather = weatherRepository.getCurrentWeatherInfo(latitude, longitude)
                _currentWeather.postValue(Result.success(weather))
            } catch (e: Exception) {
                _currentWeather.postValue(Result.failure(e))
            }
        }
    }

    override fun onBatteryLevelChanged(percentage: Int, isCharging: Boolean) {
        _batteryLevel.postValue(Pair(percentage, isCharging))
    }

    override fun onDateTimeTick(dateTime: FormattedDateTime) {
        _dateTime.postValue(dateTime)
    }

    override fun onCellularSignalStrengthChanged(strength: Int) {
        _mobileSignalStrength.postValue(strength)
    }

    override fun onNetworkConnected() {
        _networkConnected.postValue(true)
    }

    override fun onWifiSignalLevelChanged(level: Int) {
        _wifiSignalLevel.postValue(level)
    }

    override fun onNetworkDisconnected() {
        _networkConnected.postValue(false)
    }

    override fun onNetworkStateChanged(isWifi: Boolean) {
        if (isWifi) {
            _networkConnectionStrength.postValue(_wifiSignalLevel.value)
        } else {
            _networkConnectionStrength.postValue(_mobileSignalStrength.value)
        }
    }

    override fun onCleared() {
        batteryStatusHelper.removeListener(this)
        dateTimeHelper.removeListener(this)
        locationHelper.removeListener(this)
        mobileSignalHelper.removeListener(this)
        wifiConnectionHelper.removeListener(this)
        networkConnectivityHelper.removeListener(this)

        super.onCleared()
    }

    fun retrieveTutorialsForPackageName(packageName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val tutorials = applicationRepository.getTutorialsForPackageName(packageName)
                launcherApplicationsHelper.updateTutorialsForCurrentlyRunningApplication(tutorials)
            } catch (e: Exception) {
                Log.e(TAG, "retrieveTutorialsForPackageName Failure: ${e.message}", e)
            }
        }
    }

    fun refreshVisibleApps() {
        viewModelScope.launch(Dispatchers.Default) {
            _visibleAppLists.postValue(getVisibleApps())
        }
    }

    @Suppress("ConstantConditionIf")
    private fun getVisibleApps(): List<List<ApplicationInfo>> {
        val savedPackages = preferencesHelper.getVisibleApplications().toTypedArray()
        val appPages = launcherApplicationsHelper.getApplicationInfoForPackageNames(*savedPackages)
            .chunked(MAX_APPLICATIONS_PER_PAGE)

        if (addHelpButton) {
            val appPagesWithHelp = mutableListOf<List<ApplicationInfo>>()
            appPages.forEach { page ->
                val pageWithHelp = page.toMutableList().apply {
                    add(
                        0,
                        ApplicationInfo(
                            UIUtils.getString(R.string.i_need_help),
                            "",
                            UIUtils.getDrawable(R.drawable.ic_help)!!
                        )
                    )
                }
                appPagesWithHelp.add(pageWithHelp)
            }
            return appPagesWithHelp.toList()
        } else {
            return appPages
        }
    }
}