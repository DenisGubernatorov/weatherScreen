package com.weatherscreen.data.repository

import com.weatherscreen.BuildConfig
import com.weatherscreen.data.remote.WeatherApi
import com.weatherscreen.data.remote.dto.toDomainModel
import com.weatherscreen.domain.model.WeatherDomainModel
import com.weatherscreen.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
) : WeatherRepository {

    private val apiKey get() = BuildConfig.WEATHER_API_KEY

    override suspend fun getWeatherByCity(city: String): Result<WeatherDomainModel> =
        safeApiCall { api.getForecast(apiKey = apiKey, query = city).toDomainModel() }

    override suspend fun getWeatherByCoords(lat: Double, lon: Double): Result<WeatherDomainModel> =
        safeApiCall { api.getForecast(apiKey = apiKey, query = "$lat,$lon").toDomainModel() }

    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Result<T> =
        runCatching {
            withContext(Dispatchers.IO) { block() }
        }
}