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
                val currentWeather = response.toWeather()
                    ?: throw Exception("Invalid weather info for lat: $latitude, long: $longitude")

                return@withContext currentWeather
            } catch (error: Exception) {
                throw Exception("Failed to get weather info", error)
            }
        }
    }
}