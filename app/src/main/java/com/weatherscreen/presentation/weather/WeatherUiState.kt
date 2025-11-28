package com.weatherscreen.presentation.weather

import com.weatherscreen.domain.model.WeatherDomainModel


sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherDomainModel) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

data class WeatherScreenState(
    val uiState: WeatherUiState = WeatherUiState.Loading
)