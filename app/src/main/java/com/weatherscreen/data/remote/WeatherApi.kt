package com.weatherscreen.data.remote

import com.weatherscreen.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int = ApiDefaults.FORECAST_DAYS,
        @Query("aqi") aqi: String = ApiDefaults.AQI,
        @Query("alerts") alerts: String = ApiDefaults.ALERTS,
        @Query("lang") lang: String = ApiDefaults.LANG_RU
    ): WeatherResponseDto
}

private object ApiDefaults {
    const val FORECAST_DAYS = 3
    const val AQI = "no"
    const val ALERTS = "no"
    const val LANG_RU = "ru"
}