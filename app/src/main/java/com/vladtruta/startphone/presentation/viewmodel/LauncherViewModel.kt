package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.*
import com.vladtruta.startphone.model.local.FormattedDateTime
import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.util.*
import kotlinx.coroutines.launch

class LauncherViewModel(private val weatherRepository: IWeatherRepo) : ViewModel(),
    LocationHelper.LocationListener,
    BatteryStatusHelper.BatteryStatusListener,
    DateTimeHelper.DateTimeListener,
    MobileSignalHelper.MobileSignalListener,
    WifiConnectionHelper.WifiConnectionListener {

    private val _currentWeather = MutableLiveData<Result<Weather>>()
    val currentWeather: LiveData<Result<Weather>> = _currentWeather

    private val _batteryLevel = MutableLiveData(BatteryStatusHelper.getBatteryStatus())
    val batteryLevel: LiveData<Pair<Int, Boolean>> = _batteryLevel

    private val _dateTime = MutableLiveData<FormattedDateTime>()
    val dateTime: LiveData<FormattedDateTime> = _dateTime

    private val _mobileSignalStrength = MutableLiveData(MobileSignalHelper.calculateSignalStrength())
    val mobileSignalStrength: LiveData<Int> = _mobileSignalStrength

    private val _wifiSignalLevel = MutableLiveData(WifiConnectionHelper.calculateSignalLevel())
    val wifiSignalLevel: LiveData<Int> = _wifiSignalLevel

    init {
        BatteryStatusHelper.addListener(this)
        DateTimeHelper.addListener(this)
        MobileSignalHelper.addListener(this)
        WifiConnectionHelper.addListener(this)
        LocationHelper.addListener(this)
        LocationHelper.requestLastKnownLocation()
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

    override fun onCleared() {
        BatteryStatusHelper.removeListener(this)
        DateTimeHelper.removeListener(this)
        LocationHelper.removeListener(this)
        MobileSignalHelper.removeListener(this)
        WifiConnectionHelper.removeListener(this)

        super.onCleared()
    }
}