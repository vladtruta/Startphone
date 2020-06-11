package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.*
import com.vladtruta.startphone.R
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.FormattedDateTime
import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val weatherRepository: IWeatherRepo,
    private val batteryStatusHelper: BatteryStatusHelper,
    private val networkConnectivityHelper: NetworkConnectivityHelper,
    private val wifiConnectionHelper: WifiConnectionHelper,
    private val mobileSignalHelper: MobileSignalHelper,
    private val locationHelper: LocationHelper,
    private val dateTimeHelper: DateTimeHelper,
    private val launcherApplicationsHelper: LauncherApplicationsHelper
) : ViewModel(),
    LocationHelper.LocationListener,
    BatteryStatusHelper.BatteryStatusListener,
    DateTimeHelper.DateTimeListener,
    MobileSignalHelper.MobileSignalListener,
    WifiConnectionHelper.WifiConnectionListener,
    NetworkConnectivityHelper.NetworkConnectivityListener {

    companion object {
        private const val MAX_APPLICATIONS_PER_PAGE = 6
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

    private val _visibleAppLists = liveData(Dispatchers.Default) {
        val appPages = launcherApplicationsHelper.getApplicationInfoForAllApps().chunked(MAX_APPLICATIONS_PER_PAGE)
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

        emit(appPagesWithHelp.toList())
    }
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
}