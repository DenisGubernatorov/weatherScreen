package com.weatherscreen.presentation.weather

sealed interface WeatherEvent {
    object Refresh : WeatherEvent
}