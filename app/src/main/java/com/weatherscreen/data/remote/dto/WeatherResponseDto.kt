package com.weatherscreen.data.remote.dto

import com.weatherscreen.domain.model.DayItem
import com.weatherscreen.domain.model.HourItem
import com.weatherscreen.domain.model.WeatherDomainModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WeatherResponseDto(
    val location: LocationDto,
    val current: CurrentDto,
    val forecast: ForecastDto
)

data class LocationDto(val name: String)
data class CurrentDto(
    val temp: Double,
    val feelsLike: Double,
    val condition: ConditionDto,
    val windKph: Double,
    val windDir: String,
    val humidity: Int,
    val pressureMb: Double,
    val uv: Double
)

data class ConditionDto(val text: String, val icon: String)
data class ForecastDto(val forecastday: List<ForecastDayDto>)
data class ForecastDayDto(val date: String, val day: DayDto, val hour: List<HourDto>)
data class DayDto(val maxTemp: Double, val minTemp: Double, val condition: ConditionDto)
data class HourDto(val time: String, val temp: Double, val condition: ConditionDto)

private object DateFormatters {
    val hour: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm")
    val day: DateTimeFormatter? = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("ru"))
    val dayOfWeek: DateTimeFormatter? =
        DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("ru"))
}

fun WeatherResponseDto.toDomainModel(): WeatherDomainModel {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val currentHour = LocalTime.now().format(DateFormatters.hour)

    return WeatherDomainModel(
        cityName = location.name,
        currentTempC = current.temp.toInt(),
        feelsLikeC = current.feelsLike.toInt(),
        conditionText = current.condition.text,
        iconUrl = "https:${current.condition.icon}",
        windKph = current.windKph.toInt(),
        windDir = current.windDir,
        humidityPercent = current.humidity,
        pressureMb = current.pressureMb.toInt(),
        uvIndex = current.uv.toInt(),
        hourlyList = forecast.forecastday.first().hour
            .map {
                HourItem(
                    it.time.substring(11, 16),
                    it.temp.toInt(),
                    "https:${it.condition.icon}"
                )
            }
            .dropWhile { it.time < currentHour },
        dailyList = forecast.forecastday.take(3).map { dayDto ->
            val date = LocalDate.parse(dayDto.date)
            val dayLabel = when (date) {
                today -> "Сегодня"
                tomorrow -> "Завтра"
                else -> date.format(DateFormatters.dayOfWeek).replaceFirstChar { it.uppercase() }
            }
            DayItem(
                dayShort = dayLabel,
                date = date.format(DateFormatters.day),
                maxTempC = dayDto.day.maxTemp.toInt(),
                minTempC = dayDto.day.minTemp.toInt(),
                iconUrl = "https:${dayDto.day.condition.icon}"
            )
        }
    )
}