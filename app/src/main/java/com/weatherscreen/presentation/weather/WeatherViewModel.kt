package com.weatherscreen.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(WeatherScreenState(uiState = WeatherUiState.Loading))
    val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

    init {
        loadWeather()
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            WeatherEvent.Refresh -> loadWeather(isPullToRefresh = true)
        }
    }

    private fun loadWeather(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = WeatherScreenState(uiState = WeatherUiState.Loading)


            val mockData = WeatherData(
                city = "Москва",
                currentTempC = 3,
                feelsLikeC = 1,
                conditionText = "Пасмурно",
                iconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png",
                windKph = 12,
                windDir = "СЗ",
                humidity = 87,
                pressureMb = 1012,
                uv = 1,
                hourly = List(24) { hour ->
                    HourlyItem(
                        time = String.format("%02d:00", hour),
                        tempC = (0..10).random(),
                        iconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
                    )
                },
                daily = List(7) { day ->
                    DailyItem(
                        dayShort = when (day) {
                            0 -> "Сегодня"
                            1 -> "Завтра"
                            else -> listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")[day]
                        },
                        date = "26 ноя",
                        maxC = 5,
                        minC = -2,
                        iconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
                    )
                }
            )

            _state.value = WeatherScreenState(uiState = WeatherUiState.Success(mockData))
        }
    }
}