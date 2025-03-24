package hr.markopetricevic.weatherapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import hr.markopetricevic.weatherapp.data.AppPreferences
import hr.markopetricevic.weatherapp.ui.theme.Strings
import hr.markopetricevic.weatherapp.viewModel.DailyForecast
import hr.markopetricevic.weatherapp.viewModel.ForecastState
import hr.markopetricevic.weatherapp.viewModel.ForecastViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = hiltViewModel(),
    appPreferences: AppPreferences,
    city: String,
    onBack: () -> Unit
) {
    val forecastState by viewModel.forecastState
    val language by appPreferences.language.collectAsState(initial = AppPreferences.Language.HR)
    val themeMode by appPreferences.themeMode.collectAsState(initial = AppPreferences.ThemeMode.LIGHT)

    LaunchedEffect(language) {
        if (city.isNotBlank()) {
            viewModel.fetchFiveDayForecast(city)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFiveDayForecast(city)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Strings.getString("back", language),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = Strings.getString("five_day_forecast", language, city),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        when (forecastState) {
            is ForecastState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is ForecastState.Success -> {
                val dailyForecast = (forecastState as ForecastState.Success).dailyForecast
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dailyForecast) { daily ->
                        DailyForecastItem(
                            daily = daily,
                            themeMode = themeMode,
                        )
                    }
                }
            }
            is ForecastState.Error -> {
                Text(
                    text = (forecastState as ForecastState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            else -> {
                Text(
                    text = Strings.getString("no_forecast_data", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun DailyForecastItem(
    daily: DailyForecast,
    themeMode: AppPreferences.ThemeMode,
) {
    val date = LocalDate.parse(daily.date)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formattedDate = date.format(formatter)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (themeMode == AppPreferences.ThemeMode.DARK) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val weatherDescription = daily.representativeItem.weather.getOrNull(0)?.description ?: "N/A"
                Text(
                    text = weatherDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${daily.minTemp}°C / ${daily.maxTemp}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            val weatherIcon = daily.representativeItem.weather.getOrNull(0)?.icon
            AsyncImage(
                model = if (weatherIcon != null) "https://openweathermap.org/img/wn/$weatherIcon@2x.png" else null,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}