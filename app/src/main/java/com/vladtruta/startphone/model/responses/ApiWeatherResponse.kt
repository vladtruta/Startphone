package com.vladtruta.startphone.model.responses

import com.google.gson.annotations.SerializedName
import com.vladtruta.startphone.model.local.Weather

data class ApiWeatherResponse(
    @SerializedName("weather")
    val weathers: List<ApiWeather>? = null
) {
    data class ApiWeather(
        @SerializedName("main")
        val main: String? = null,
        @SerializedName("icon")
        val iconUrl: String? = null
    ) {
        fun toWeather(): Weather? {
            main ?: return null
            iconUrl ?: return null

            return Weather(main, iconUrl)
        }
    }
}