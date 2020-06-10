package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.webservice.IOpenWeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UnnecessaryVariable")
class WeatherRepository(private val openWeatherApi: IOpenWeatherApi) : IWeatherRepo {

    override suspend fun getCurrentWeatherInfo(latitude: Double, longitude: Double): Weather {
        return withContext(Dispatchers.IO) {
            try {
                val response = openWeatherApi.getCurrentWeatherInfo(latitude, longitude)

                val currentWeather = response.weathers?.firstOrNull()
                    ?: throw Exception("No weather info found for lat: $latitude, long: $longitude")
                val weather = currentWeather.toWeather()
                    ?: throw Exception("Incomplete weather info")

                return@withContext weather
            } catch (error: Exception) {
                throw Exception("Failed to get weather info", error)
            }
        }
    }
}