package com.weatherscreen.presentation.weather

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import com.weatherscreen.R
import com.weatherscreen.domain.model.DayItem
import com.weatherscreen.domain.model.HourItem
import com.weatherscreen.domain.model.WeatherDomainModel

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    //var isManuallyRefreshing by remember { mutableStateOf(false) }
    var pullRefreshInProgress by remember { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(state.uiState) {
        if (state.uiState !is WeatherUiState.Loading) {
            pullRefreshInProgress = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            LandscapeStub(uiState = state.uiState)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colorResource(R.color.background_start),
                                colorResource(R.color.background_end)
                            )
                        )
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 120f && !pullRefreshInProgress
                                && state.uiState is WeatherUiState.Success
                            ) {
                                pullRefreshInProgress = true
                                viewModel.onEvent(WeatherEvent.Refresh)
                            }
                        }
                    }
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxxlarge)))

                when {
                    pullRefreshInProgress || state.uiState is WeatherUiState.Loading -> {
                        WeatherLoadingSkeleton()
                    }

                    state.uiState is WeatherUiState.Success -> {
                        val data = (state.uiState as WeatherUiState.Success).data

                        CityName(city = data.cityName)
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))

                        CurrentTemperature(data = data)
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                        ConditionText(text = data.conditionText)
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxlarge)))

                        DetailCards(data = data)
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxxlarge)))

                        SectionTitle("Почасовой прогноз")
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))
                        HourlyForecast(hourly = data.hourlyList)

                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxxlarge)))

                        SectionTitle("Прогноз на 3 дня")
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xlarge)))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_xlarge)))
                                .background(colorResource(R.color.text_white_80).copy(alpha = 0.08f))
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(dimensionResource(R.dimen.spacing_xlarge)),
                                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xlarge))
                            ) {
                                items(data.dailyList) { day ->
                                    DailyForecastItem(item = day)
                                }
                            }
                        }
                    }

                    state.uiState is WeatherUiState.Error -> {
                        WeatherLoadingSkeleton() // или отдельный экран ошибки, если хочешь
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxxlarge)))
            }
        }

        /*if (isManuallyRefreshing) {
            WeatherLoadingSkeleton()
        }*/

        if (state.uiState is WeatherUiState.Error) {
            val errorState = state.uiState as WeatherUiState.Error
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = errorState.message.asString(),
                        color = colorResource(R.color.text_white),
                        fontWeight = FontWeight.Medium
                    )
                },
                text = {
                    Text(
                        text = errorState.message.asString(),
                        color = colorResource(R.color.text_white_90)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(WeatherEvent.Refresh) }) {
                        Text(
                            stringResource(R.string.retry),
                            color = colorResource(R.color.retry_blue)
                        )
                    }
                },
                containerColor = colorResource(R.color.dialog_container),
                shape = RoundedCornerShape(dimensionResource(R.dimen.spacing_xlarge))
            )
        }
    }
}

@Composable
private fun LandscapeStub(uiState: WeatherUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.background_start),
                        colorResource(R.color.background_end)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.landscape_stub_title),
            fontSize = 32.sp,
            color = colorResource(R.color.text_white),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xxxlarge)))

        when (uiState) {
            is WeatherUiState.Success -> {
                val data = uiState.data
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.landscape_stub_saved_data),
                        color = colorResource(R.color.text_white_90),
                        fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.landscape_stub_city, data.cityName),
                        color = colorResource(R.color.text_white_80),
                        fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.landscape_stub_temp, data.currentTempC),
                        color = colorResource(R.color.text_white_80),
                        fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.landscape_stub_feels_like, data.feelsLikeC),
                        color = colorResource(R.color.text_white_80),
                        fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(
                            R.string.landscape_stub_condition,
                            data.conditionText
                        ),
                        color = colorResource(R.color.text_white_80),
                        fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is WeatherUiState.Error -> {
                Text(
                    text = stringResource(R.string.landscape_stub_error, uiState.message),
                    color = colorResource(R.color.error_red),
                    fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                    textAlign = TextAlign.Center
                )
            }

            WeatherUiState.Loading -> {
                Text(
                    text = stringResource(R.string.landscape_stub_loading),
                    color = colorResource(R.color.text_white_70),
                    fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CityName(city: String) {
    Text(
        text = city,
        fontSize = 24.sp,
        color = colorResource(R.color.text_white_90),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CurrentTemperature(data: WeatherDomainModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.temp_format, data.currentTempC),
            fontSize = dimensionResource(R.dimen.text_temp).value.sp,
            fontWeight = FontWeight.Light,
            color = colorResource(R.color.text_white),
            lineHeight = 100.sp
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.spacing_xlarge)))
        AsyncImage(
            model = data.iconUrl.replace("64x64", "128x128"),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.icon_current_size))
        )
    }
}

@Composable
private fun ConditionText(text: String) {
    Text(
        text = text,
        fontSize = dimensionResource(R.dimen.text_condition).value.sp,
        color = colorResource(R.color.text_white),
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DetailCards(data: WeatherDomainModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xlarge))
        ) {
            DetailCard(
                title = stringResource(R.string.feels_like),
                value = stringResource(R.string.temp_format, data.feelsLikeC),
                modifier = Modifier.weight(1f)
            )
            DetailCard(
                title = stringResource(R.string.wind),
                value = stringResource(R.string.wind_speed_format, data.windKph),
                modifier = Modifier.weight(1f)
            )
            DetailCard(
                title = stringResource(R.string.wind_direction),
                value = data.windDir,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xlarge))
        ) {
            DetailCard(
                title = stringResource(R.string.humidity),
                value = stringResource(R.string.humidity_format, data.humidityPercent),
                modifier = Modifier.weight(1f)
            )
            DetailCard(
                title = stringResource(R.string.pressure),
                value = stringResource(R.string.pressure_format, data.pressureMb),
                modifier = Modifier.weight(1f)
            )
            DetailCard(
                title = stringResource(R.string.uv_index),
                value = data.uvIndex.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .wrapContentHeight(align = Alignment.CenterVertically)
            .defaultMinSize(minHeight = dimensionResource(R.dimen.spacing_small)),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.detail_card)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_xlarge))
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = colorResource(R.color.text_white),
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = title,
                color = colorResource(R.color.text_white_80),
                fontSize = dimensionResource(R.dimen.text_detail_title).value.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = colorResource(R.color.text_white),
        fontSize = dimensionResource(R.dimen.text_section_title).value.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun HourlyForecast(hourly: List<HourItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xlarge)),
        contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.spacing_medium)),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(hourly) { HourlyItem(it) }
    }
}

@Composable
private fun HourlyItem(item: HourItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(dimensionResource(R.dimen.daily_card_height))
    ) {
        Text(
            text = item.time,
            color = colorResource(R.color.text_white_80),
            fontSize = dimensionResource(R.dimen.text_hourly_time).value.sp,
            textAlign = TextAlign.Center
        )
        AsyncImage(
            model = item.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.spacing_medium))
        )
        Text(
            text = stringResource(R.string.temp_format, item.tempC),
            color = colorResource(R.color.text_white),
            fontWeight = FontWeight.Medium,
            fontSize = dimensionResource(R.dimen.text_detail_value).value.sp
        )
    }
}

@Composable
private fun DailyForecastItem(item: DayItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.text_white_80).copy(
                alpha = 0.08f
            )
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_xlarge))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_xlarge)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.dayShort,
                    color = colorResource(R.color.text_white),
                    fontWeight = FontWeight.Medium,
                    fontSize = dimensionResource(R.dimen.text_detail_value).value.sp
                )
                Text(
                    text = item.date,
                    color = colorResource(R.color.text_white_70),
                    fontSize = dimensionResource(R.dimen.text_hourly_time).value.sp
                )
            }
            AsyncImage(
                model = item.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.spacing_medium))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_xlarge)))
            Text(
                text = stringResource(R.string.daily_temp_format, item.maxTempC, item.minTempC),
                color = colorResource(R.color.text_white),
                fontSize = dimensionResource(R.dimen.text_daily_temp).value.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WeatherLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .shimmer(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xxlarge))
    ) {
        Spacer(
            Modifier
                .height(dimensionResource(R.dimen.spacing_xxxlarge))
                .width(160.dp)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_medium)))
                .background(colorResource(R.color.skeleton))
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                Modifier
                    .height(110.dp)
                    .width(200.dp)
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_large)))
                    .background(colorResource(R.color.skeleton))
            )
            Spacer(Modifier.width(20.dp))
            Spacer(
                Modifier
                    .size(dimensionResource(R.dimen.icon_current_size))
                    .clip(CircleShape)
                    .background(colorResource(R.color.skeleton))
            )
        }

        Spacer(
            Modifier
                .height(36.dp)
                .width(220.dp)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_medium)))
                .background(colorResource(R.color.skeleton))
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xlarge))) {
                repeat(3) {
                    Spacer(
                        Modifier
                            .size(106.dp)
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.corner_radius_xlarge)))
                            .background(colorResource(R.color.skeleton))
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xlarge))) {
                repeat(3) {
                    Spacer(
                        Modifier
                            .size(106.dp)
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.corner_radius_xlarge)))
                            .background(colorResource(R.color.skeleton))
                    )
                }
            }
        }

        Spacer(
            Modifier
                .height(dimensionResource(R.dimen.spacing_medium))
                .width(200.dp)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_medium)))
                .background(colorResource(R.color.skeleton))
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(
            Modifier
                .height(100.dp)
                .fillMaxWidth()
                .background(colorResource(R.color.forecast_card))
        )
        Spacer(
            Modifier
                .height(dimensionResource(R.dimen.spacing_medium))
                .width(160.dp)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_medium)))
                .background(colorResource(R.color.skeleton))
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        repeat(3) {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.daily_card_height))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.corner_radius_xlarge)))
                    .background(colorResource(R.color.forecast_card))
            )
        }
    }
}
