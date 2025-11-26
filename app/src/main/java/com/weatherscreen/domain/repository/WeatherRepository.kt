package com.weatherscreen.domain.repository

import com.weatherscreen.domain.model.WeatherDomainModel

interface WeatherRepository {
    suspend fun getWeatherByCity(city: String): Result<WeatherDomainModel>
    suspend fun getWeatherByCoords(lat: Double, lon: Double): Result<WeatherDomainModel>
}