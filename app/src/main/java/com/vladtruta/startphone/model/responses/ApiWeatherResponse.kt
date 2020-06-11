package com.vladtruta.startphone.model.responses

import com.google.gson.annotations.SerializedName
import com.vladtruta.startphone.model.local.Weather

data class ApiWeatherResponse(
    @SerializedName("temp")
    val temp: Double? = null,
    @SerializedName("weather")
    val weathers: List<ApiWeather>? = null
) {
    data class ApiWeather(
        @SerializedName("main")
        val main: String? = null,
        @SerializedName("icon")
        val iconUrl: String? = null
    )

    fun toWeather(): Weather? {
        temp ?: return null
        val firstWeather = weathers?.firstOrNull() ?: return null
        firstWeather.main ?: return null
        firstWeather.iconUrl ?: return null

        return Weather(firstWeather.main, firstWeather.iconUrl, temp)
    }
}