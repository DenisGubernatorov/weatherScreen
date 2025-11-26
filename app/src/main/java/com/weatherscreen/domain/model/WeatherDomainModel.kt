package com.weatherscreen.domain.model

data class WeatherDomainModel(
    val cityName: String,
    val currentTempC: Int,
    val feelsLikeC: Int,
    val conditionText: String,
    val iconUrl: String,
    val windKph: Int,
    val windDir: String,
    val humidityPercent: Int,
    val pressureMb: Int,
    val uvIndex: Int,
    val hourlyList: List<HourItem>,
    val dailyList: List<DayItem>
)

data class HourItem(
    val time: String,
    val tempC: Int,
    val iconUrl: String
)

data class DayItem(
    val dayShort: String,
    val date: String,
    val maxTempC: Int,
    val minTempC: Int,
    val iconUrl: String
)