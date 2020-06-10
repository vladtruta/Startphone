package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.repository.IWeatherRepo
import com.vladtruta.startphone.util.LocationHelper
import kotlinx.coroutines.launch

class LauncherViewModel(private val weatherRepository: IWeatherRepo) :
    ViewModel(),
    LocationHelper.LocationListener {

    private val _currentWeather = MutableLiveData<Result<Weather>>()
    val currentWeather: LiveData<Result<Weather>> = _currentWeather

    init {
        LocationHelper.addListener(this)
    }

    fun getCurrentWeatherInformation() {
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

    fun getCurrentBatteryLevel() {

    }

    override fun onCleared() {
        LocationHelper.removeListener(this)
        super.onCleared()
    }
}