package com.vladtruta.startphone.webservice

import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.model.responses.ApiWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IOpenWeatherApi {

    @GET("onecall")
    suspend fun getCurrentWeatherInfo(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely,hourly,daily",
        @Query("lang") languageCode: String = "ro",
        @Query("appid") apiKey: String = BuildConfig.OPEN_WEATHER_API_KEY
    ): ApiWeatherResponse

}