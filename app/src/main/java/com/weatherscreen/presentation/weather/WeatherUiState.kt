package com.weatherscreen.presentation.weather

data class WeatherData(
    val city: String = "Москва",
    val currentTempC: Int = 3,
    val feelsLikeC: Int = 1,
    val conditionText: String = "Пасмурно",
    val iconUrl: String = "https://cdn.weatherapi.com/weather/64x64/day/122.png",
    val windKph: Int = 12,
    val humidity: Int = 87,
    val pressureMb: Int = 1012,
    val uv: Int = 1,
    val windDir: String = "СЗ",
    val hourly: List<HourlyItem> = listOf(
        HourlyItem("12:00", 4, "https://cdn.weatherapi.com/weather/64x64/day/116.png"),
        HourlyItem("15:00", 3, "https://cdn.weatherapi.com/weather/64x64/day/119.png"),
        HourlyItem("18:00", 2, "https://cdn.weatherapi.com/weather/64x64/night/122.png"),
        HourlyItem("21:00", 1, "https://cdn.weatherapi.com/weather/64x64/night/116.png"),
        HourlyItem("00:00", 0, "https://cdn.weatherapi.com/weather/64x64/night/122.png")
    ),
    val daily: List<DailyItem> = listOf(
        DailyItem("Ср", "26 ноя", 4, 0, "https://cdn.weatherapi.com/weather/64x64/day/119.png"),
        DailyItem("Чт", "27 ноя", 5, 1, "https://cdn.weatherapi.com/weather/64x64/day/116.png"),
        DailyItem("Пт", "28 ноя", 3, -1, "https://cdn.weatherapi.com/weather/64x64/day/302.png")
    )
)

data class HourlyItem(val time: String, val tempC: Int, val iconUrl: String)
data class DailyItem(val dayShort: String, val date: String, val maxC: Int, val minC: Int, val iconUrl: String)

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

data class WeatherScreenState(
    val uiState: WeatherUiState = WeatherUiState.Success(
        WeatherData()
    )
)