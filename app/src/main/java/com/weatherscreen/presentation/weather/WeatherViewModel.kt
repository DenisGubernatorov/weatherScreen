package com.weatherscreen.presentation.weather

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherscreen.R
import com.weatherscreen.domain.usecase.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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
                    val errorText = when (result.exceptionOrNull()) {
                        is HttpException -> UiText.StringResource(R.string.error_network)
                        is IOException -> UiText.StringResource(R.string.error_no_internet)
                        else -> result.exceptionOrNull()?.message?.let { UiText.DynamicString(it) }
                            ?: UiText.StringResource(R.string.error_loading_weather)
                    }
                    WeatherScreenState(uiState = WeatherUiState.Error(errorText))
                }
            }
        }
    }
}