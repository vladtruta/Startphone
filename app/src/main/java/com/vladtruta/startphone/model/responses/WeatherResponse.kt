package com.vladtruta.startphone.model.responses

import com.google.gson.annotations.SerializedName
import com.vladtruta.startphone.model.local.Weather

data class WeatherResponse(
    @SerializedName("current")
    val current: ApiCurrent? = null
) {
    data class ApiCurrent(
        @SerializedName("temp")
        val temperature: Double? = null,
        @SerializedName("weather")
        val weathers: List<ApiWeather>? = null
    ) {
        data class ApiWeather(
            @SerializedName("main")
            val main: String? = null,
            @SerializedName("icon")
            val iconUrl: String? = null
        )
    }

    fun toWeather(): Weather? {
        val temperature = current?.temperature ?: return null
        val firstWeather = current.weathers?.firstOrNull() ?: return null
        firstWeather.main ?: return null
        firstWeather.iconUrl ?: return null

        return Weather(firstWeather.main, firstWeather.iconUrl, temperature)
    }
}