package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladtruta.startphone.model.local.FormattedDateTime
import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.util.*
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val weatherRepository: IWeatherRepo,
    private val batteryStatusHelper: BatteryStatusHelper,
    private val networkConnectivityHelper: NetworkConnectivityHelper,
    private val wifiConnectionHelper: WifiConnectionHelper,
    private val mobileSignalHelper: MobileSignalHelper,
    private val locationHelper: LocationHelper,
    private val dateTimeHelper: DateTimeHelper
) : ViewModel(),
    LocationHelper.LocationListener,
    BatteryStatusHelper.BatteryStatusListener,
    DateTimeHelper.DateTimeListener,
    MobileSignalHelper.MobileSignalListener,
    WifiConnectionHelper.WifiConnectionListener,
    NetworkConnectivityHelper.NetworkConnectivityListener {

    private val _currentWeather = MutableLiveData<Result<Weather>>()
    val currentWeather: LiveData<Result<Weather>> = _currentWeather

    private val _batteryLevel = MutableLiveData(batteryStatusHelper.getBatteryStatus())
    val batteryLevel: LiveData<Pair<Int, Boolean>> = _batteryLevel

    private val _dateTime = MutableLiveData<FormattedDateTime>()
    val dateTime: LiveData<FormattedDateTime> = _dateTime

    private val _networkConnected = MutableLiveData(networkConnectivityHelper.isNetworkConnected())
    val networkConnected: LiveData<Boolean> = _networkConnected

    private val _mobileSignalStrength =
        MutableLiveData(mobileSignalHelper.calculateSignalStrength())
    private val _wifiSignalLevel = MutableLiveData(wifiConnectionHelper.calculateSignalLevel())
    var networkConnectionStrength: LiveData<Int> =
        if (networkConnectivityHelper.isWifiConnected()) {
            _wifiSignalLevel
        } else {
            _mobileSignalStrength
        }

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

    override fun onMobileSignalStrengthChanged(strength: Int) {
        _mobileSignalStrength.postValue(strength)
    }

    override fun onWifiSignalLevelChanged(level: Int) {
        _wifiSignalLevel.postValue(level)
    }

    override fun onNetworkConnected() {
        _networkConnected.postValue(true)
    }

    override fun onNetworkStateChanged(isWifi: Boolean) {
        networkConnectionStrength = if (isWifi) {
            _wifiSignalLevel
        } else {
            _mobileSignalStrength
        }
    }

    override fun onNetworkDisconnected() {
        _networkConnected.postValue(false)
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