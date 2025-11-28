package com.weatherscreen.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.weatherscreen.domain.model.DayItem
import com.weatherscreen.domain.model.HourItem
import com.weatherscreen.domain.model.WeatherDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@JsonClass(generateAdapter = true)
data class WeatherResponseDto(
    val location: LocationDto,
    val current: CurrentDto,
    val forecast: ForecastDto
)

@JsonClass(generateAdapter = true)
data class LocationDto(
    val name: String,
    @param:Json(name = "localtime") val localtime: String,
    @param:Json(name = "localtime_epoch") val localtimeEpoch: Long,
    @param:Json(name = "tz_id") val timeZone: String? = null,
)

@JsonClass(generateAdapter = true)
data class CurrentDto(
    @param:Json(name = "temp_c") val temp: Double,
    @param:Json(name = "feelslike_c") val feelsLike: Double,
    val condition: ConditionDto,
    @param:Json(name = "wind_kph") val windKph: Double,
    @param:Json(name = "wind_dir") val windDir: String,
    val humidity: Int,
    @param:Json(name = "pressure_mb") val pressureMb: Double,
    val uv: Double
)

@JsonClass(generateAdapter = true)
data class ConditionDto(
    val text: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class ForecastDto(
    val forecastday: List<ForecastDayDto>
)

@JsonClass(generateAdapter = true)
data class ForecastDayDto(
    val date: String,
    val day: DayDto,
    val hour: List<HourDto>
)

@JsonClass(generateAdapter = true)
data class DayDto(
    @param:Json(name = "maxtemp_c") val maxTemp: Double,
    @param:Json(name = "mintemp_c") val minTemp: Double,
    val condition: ConditionDto
)

@JsonClass(generateAdapter = true)
data class HourDto(
    val time: String,
    @param:Json(name = "temp_c") val temp: Double,
    val condition: ConditionDto
)


private object DateFormatters {
    val hour: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val day: DateTimeFormatter = DateTimeFormatter.ofPattern(
        "d MMM",
        Locale.forLanguageTag("ru")
    )

    val dayOfWeek: DateTimeFormatter = DateTimeFormatter.ofPattern(
        "EEEE",
        Locale.forLanguageTag("ru")
    )
}

fun WeatherResponseDto.toDomainModel(): WeatherDomainModel {

    val zoneId = ZoneId.of(location.timeZone ?: "UTC")
    val nowInCity = Instant
        .ofEpochSecond(location.localtimeEpoch)
        .atZone(zoneId)

    val today = nowInCity.toLocalDate()
    val tomorrow = today.plusDays(1)
    val currentHour = nowInCity.hour

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


        hourlyList = forecast.forecastday
            .asSequence()
            .flatMap { it.hour }
            .map { hourDto ->
                val hourDateTime = LocalDateTime.parse(
                    hourDto.time,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm")
                )
                val hourInCity = hourDateTime.atZone(zoneId)


                if (hourInCity.toLocalDate().isBefore(today)) return@map null
                if (hourInCity.toLocalDate() == today && hourInCity.hour < currentHour) return@map null

                HourItem(
                    time = hourDateTime.format(DateFormatters.hour),
                    tempC = hourDto.temp.toInt(),
                    iconUrl = "https:${hourDto.condition.icon}"
                )
            }
            .filterNotNull()
            .take(24)
            .toList(),

        // Дни
        dailyList = forecast.forecastday.take(7).map { forecastDay ->
            val date = LocalDate.parse(forecastDay.date)

            val dayLabel = when (date) {
                today -> "Сегодня"
                tomorrow -> "Завтра"
                else -> date.format(DateFormatters.dayOfWeek)
                    .replaceFirstChar { it.titlecase(Locale.forLanguageTag("ru")) }
            }

            DayItem(
                dayShort = dayLabel,
                date = date.format(DateFormatters.day),
                maxTempC = forecastDay.day.maxTemp.toInt(),
                minTempC = forecastDay.day.minTemp.toInt(),
                iconUrl = "https:${forecastDay.day.condition.icon}"
            )
        }
    )
}