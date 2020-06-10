package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.Weather

interface IWeatherRepo {
    suspend fun getCurrentWeatherInfo(latitude: Double, longitude: Double): Weather
}