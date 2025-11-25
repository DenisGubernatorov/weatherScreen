package com.weatherscreen.presentation.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isManuallyRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isManuallyRefreshing) {
        if (isManuallyRefreshing) {
            viewModel.onEvent(WeatherEvent.Refresh)
            isManuallyRefreshing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E3A5F), Color(0xFF0F1E36))))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 120) isManuallyRefreshing = true
                    }
                },
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (val uiState = state.uiState) {
                is WeatherUiState.Success -> {
                    val data = uiState.data
                    item { CityName(data.city) }
                    item { CurrentTemperature(data) }
                    item { ConditionText(data.conditionText) }
                    item { DetailCards(data) }
                    item { SectionTitle("Почасовой прогноз") }
                    item { HourlyForecast(data.hourly) }
                    item { SectionTitle("На 3 дня") }
                    items(data.daily.take(3)) { DailyForecastItem(it) }
                }
                is WeatherUiState.Loading -> {
                    item { WeatherLoadingSkeleton() }
                }
                is WeatherUiState.Error -> {
                    item {
                         WeatherLoadingSkeleton()
                    }
                }
            }
        }

        if (isManuallyRefreshing && state.uiState !is WeatherUiState.Loading) {
            WeatherLoadingSkeleton()
        }
        if (state.uiState is WeatherUiState.Error) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Ошибка загрузки", color = Color.White, fontWeight = FontWeight.Medium) },
                text = { Text((state.uiState as WeatherUiState.Error).message, color = Color.White.copy(0.9f)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onEvent(WeatherEvent.Refresh)
                    }) {
                        Text("Повторить", color = Color(0xFF64B5F6))
                    }
                },
                containerColor = Color(0xFF1E3A5F),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}


@Composable
private fun CityName(city: String) {
    Text(
        text = city,
        fontSize = 24.sp,
        color = Color.White.copy(0.9f),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CurrentTemperature(data: WeatherData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${data.currentTempC}°",
            fontSize = 110.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            lineHeight = 100.sp
        )
        Spacer(Modifier.width(16.dp))
        AsyncImage(
            model = data.iconUrl.replace("64x64", "128x128"),
            contentDescription = null,
            modifier = Modifier.size(130.dp)
        )
    }
}

@Composable
private fun ConditionText(text: String) {
    Text(
        text = text,
        fontSize = 28.sp,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DetailCards(data: WeatherData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailCard("Ощущается", "${data.feelsLikeC}°")
            DetailCard("Ветер", "${data.windKph} км/ч")
            DetailCard("Направление", data.windDir)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailCard("Влажность", "${data.humidity}%")
            DetailCard("Давление", "${data.pressureMb} мб")
            DetailCard("UV-индекс", data.uv.toString())
        }
    }
}

@Composable
private fun DetailCard(title: String, value: String) {
    Card(
        modifier = Modifier.size(106.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A4A6B).copy(alpha = 0.7f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(title, color = Color.White.copy(0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun HourlyForecast(hourly: List<HourlyItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(hourly) { HourlyItem(it) }
    }
}

@Composable
private fun HourlyItem(item: HourlyItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Text(text = item.time, color = Color.White.copy(0.8f), fontSize = 14.sp, textAlign = TextAlign.Center)
        AsyncImage(model = item.iconUrl, contentDescription = null, modifier = Modifier.size(48.dp))
        Text(text = "${item.tempC}°", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}

@Composable
private fun DailyForecastItem(item: DailyItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.dayShort, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text(text = item.date, color = Color.White.copy(0.7f), fontSize = 14.sp)
            }
            AsyncImage(model = item.iconUrl, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "${item.maxC}° / ${item.minC}°", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
private fun WeatherLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .shimmer(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Spacer(Modifier.height(32.dp).width(160.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.15f)).fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.height(110.dp).width(200.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.15f)))
            Spacer(Modifier.width(20.dp))
            Spacer(Modifier.size(130.dp).clip(CircleShape).background(Color.White.copy(0.15f)))
        }


        Spacer(Modifier.height(36.dp).width(220.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.15f)).fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))


        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { repeat(3) { Spacer(Modifier.size(106.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.15f))) } }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { repeat(3) { Spacer(Modifier.size(106.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.15f))) } }
        }

        Spacer(Modifier.height(28.dp).width(200.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.15f)).fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
        Spacer(Modifier.height(100.dp).fillMaxWidth().background(Color.White.copy(0.08f)))
        Spacer(Modifier.height(28.dp).width(160.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.15f)).fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
        repeat(3) { Spacer(Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.08f))) }
    }

}
