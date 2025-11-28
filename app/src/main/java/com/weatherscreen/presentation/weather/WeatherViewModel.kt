package com.weatherscreen.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherscreen.domain.usecase.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherScreenState())
    val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

    init {
        loadWeather()
    }

    fun onEvent(event: WeatherEvent) {
        when (event) {
            WeatherEvent.Refresh -> loadWeather()
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            _state.value = WeatherScreenState(uiState = WeatherUiState.Loading)

            val result = getWeatherUseCase()

            _state.value = when {
                result.isSuccess -> {
                    val domainModel = result.getOrNull()!!
                    WeatherScreenState(uiState = WeatherUiState.Success(domainModel))
                }

                else -> {
                    val error = result.exceptionOrNull()?.message ?: "Ошибка загрузки погоды"
                    WeatherScreenState(uiState = WeatherUiState.Error(error))
                }
            }
        }
    }
}