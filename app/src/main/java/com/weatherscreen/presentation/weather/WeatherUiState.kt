package com.weatherscreen.presentation.weather

import UiText
import com.weatherscreen.domain.model.WeatherDomainModel


sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherDomainModel) : WeatherUiState
    data class Error(val message: UiText) : WeatherUiState
}

data class WeatherScreenState(
    val uiState: WeatherUiState = WeatherUiState.Loading
)