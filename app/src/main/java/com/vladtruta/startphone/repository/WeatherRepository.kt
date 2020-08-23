package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.Weather
import com.vladtruta.startphone.webservice.IOpenWeatherApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UnnecessaryVariable")
class WeatherRepository(
    private val openWeatherApi: IOpenWeatherApi,
    private val ioDispatcher: CoroutineDispatcher
) : IWeatherRepo {

    override suspend fun getCurrentWeatherInfo(latitude: Double, longitude: Double): Weather {
        return withContext(ioDispatcher) {
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