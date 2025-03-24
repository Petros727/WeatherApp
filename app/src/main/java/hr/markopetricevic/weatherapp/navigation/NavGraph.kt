package hr.markopetricevic.weatherapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import hr.markopetricevic.weatherapp.data.AppPreferences
import hr.markopetricevic.weatherapp.screen.ForecastScreen
import hr.markopetricevic.weatherapp.screen.WeatherScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    appPreferences: AppPreferences
) {
    NavHost(
        navController = navController,
        startDestination = "weather_screen"
    ) {
        composable("weather_screen") {
            var selectedCity by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("selected_city", null)
                    ?.collect { city ->
                        selectedCity = city
                    }
            }

            WeatherScreen(
                appPreferences = appPreferences,
                onNavigateToForecast = { city ->
                    navController.navigate("forecast_screen/$city")
                },
                selectedCity = selectedCity
            )
        }
        composable(
            route = "forecast_screen/{city}",
            arguments = listOf(navArgument("city") { type = NavType.StringType })
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            ForecastScreen(
                city = city,
                appPreferences = appPreferences,
                onBack = { navController.popBackStack() }
            )
        }
    }
}