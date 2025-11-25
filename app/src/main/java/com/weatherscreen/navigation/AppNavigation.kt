// app/src/main/java/com/weatherscreen/navigation/AppNavigation.kt
package com.weatherscreen.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weatherscreen.presentation.weather.WeatherScreen

sealed class Screen(val route: String) {
    object Weather : Screen("weather")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Weather.route
    ) {
        composable(Screen.Weather.route) {
            WeatherScreen()
        }
    }
}