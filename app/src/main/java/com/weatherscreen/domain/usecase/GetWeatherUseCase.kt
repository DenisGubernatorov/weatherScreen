package com.weatherscreen.domain.usecase

import com.weatherscreen.domain.model.WeatherDomainModel
import com.weatherscreen.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        city: String? = null,
        lat: Double = LocationDefaults.LAT_MOSCOW,
        lon: Double = LocationDefaults.LON_MOSCOW
    ): Result<WeatherDomainModel> = when {
        city != null -> repository.getWeatherByCity(city)
        else -> repository.getWeatherByCoords(lat, lon)
    }
}

private object LocationDefaults {
    const val LAT_MOSCOW = 55.7569
    const val LON_MOSCOW = 37.6151
}