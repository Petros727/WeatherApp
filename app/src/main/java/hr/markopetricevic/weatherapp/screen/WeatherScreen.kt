package hr.markopetricevic.weatherapp.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import hr.markopetricevic.weatherapp.R
import hr.markopetricevic.weatherapp.data.AppPreferences
import hr.markopetricevic.weatherapp.data.ForecastItem
import hr.markopetricevic.weatherapp.data.WeatherResponse
import hr.markopetricevic.weatherapp.ui.theme.Strings
import hr.markopetricevic.weatherapp.viewModel.CurrentWeatherViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun getRelativeDateTime(timestamp: Long, language: AppPreferences.Language): String {
    val forecastDateTime = Instant.ofEpochSecond(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val today = LocalDateTime.now()
    val tomorrow = today.plusDays(1)

    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm")

    return when {
        forecastDateTime.toLocalDate() == today.toLocalDate() -> {
            "${Strings.getString("today", language)} ${forecastDateTime.format(timeFormat)}"
        }
        forecastDateTime.toLocalDate() == tomorrow.toLocalDate() -> {
            "${Strings.getString("tomorrow", language)} ${forecastDateTime.format(timeFormat)}"
        }
        else -> forecastDateTime.format(dateFormat)
    }
}

@Composable
fun WeatherScreen(
    viewModel: CurrentWeatherViewModel = hiltViewModel(),
    appPreferences: AppPreferences,
    onNavigateToForecast: (String) -> Unit,
    selectedCity: String?
) {
    val weatherState by viewModel.weatherState
    var city by rememberSaveable { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val themeMode by appPreferences.themeMode.collectAsState(initial = AppPreferences.ThemeMode.LIGHT)
    val language by appPreferences.language.collectAsState(initial = AppPreferences.Language.HR)
    val lastCity by appPreferences.lastCity.collectAsState(initial = null)
    val searchHistory by appPreferences.searchHistory.collectAsState(initial = emptyList())
    val favorites by appPreferences.favorites.collectAsState(initial = emptyList())

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(language) {
        if (city.isNotBlank()) {
            viewModel.fetchCurrentWeather(city)
        }
    }

    LaunchedEffect(lastCity, selectedCity) {
        if (city.isEmpty() && lastCity != null) {
            city = lastCity!!
            viewModel.fetchCurrentWeather(city)
        }
        if (selectedCity != null) {
            city = selectedCity
            coroutineScope.launch {
                appPreferences.setLastCity(city)
                appPreferences.addToSearchHistory(city)
                viewModel.fetchCurrentWeather(city)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Text(
                        Strings.getString("favorites", language),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    if (favorites.isEmpty()) {
                        Text(
                            text = Strings.getString("no_favorites", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favorites) { favoriteCity ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            city = favoriteCity
                                            coroutineScope.launch {
                                                appPreferences.setLastCity(city)
                                                appPreferences.addToSearchHistory(city)
                                                viewModel.fetchCurrentWeather(city)
                                                drawerState.close()
                                            }
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = favoriteCity,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    appPreferences.removeFromFavorites(favoriteCity)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = Strings.getString("remove_from_favorites", language),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        val backgroundColor by animateColorAsState(
            targetValue = MaterialTheme.colorScheme.background,
            animationSpec = tween(500), label = ""
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { coroutineScope.launch { drawerState.open() } }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = Strings.getString("open_favorites", language),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                LanguageSelector(appPreferences = appPreferences)

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val newMode = if (themeMode == AppPreferences.ThemeMode.LIGHT) AppPreferences.ThemeMode.DARK else AppPreferences.ThemeMode.LIGHT
                            appPreferences.setThemeMode(newMode)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (themeMode == AppPreferences.ThemeMode.LIGHT) R.drawable.moon else R.drawable.sun
                        ),
                        contentDescription = if (themeMode == AppPreferences.ThemeMode.LIGHT) Strings.getString("switch_to_dark_mode", language) else Strings.getString("switch_to_light_mode", language),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(Strings.getString("enter_city_name", language)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        appPreferences.setLastCity(city)
                        appPreferences.addToSearchHistory(city)
                        viewModel.fetchCurrentWeather(city)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(Strings.getString("get_weather", language), style = MaterialTheme.typography.labelLarge)
            }

            if (searchHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = Strings.getString("recent_searches", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchHistory) { historyCity ->
                            Card(
                                modifier = Modifier.clickable {
                                    city = historyCity
                                    coroutineScope.launch {
                                        appPreferences.setLastCity(city)
                                        appPreferences.addToSearchHistory(city)
                                        viewModel.fetchCurrentWeather(city)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = historyCity,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (showError) {
                Text(
                    text = Strings.getString("please_enter_city", language),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            when (weatherState) {
                is CurrentWeatherViewModel.CurrentWeatherState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is CurrentWeatherViewModel.CurrentWeatherState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeatherDisplay(
                            data = (weatherState as CurrentWeatherViewModel.CurrentWeatherState.Success).current,
                            themeMode = themeMode,
                            language = language,
                            onClick = {
                                if (city.isNotBlank()) {
                                    showError = false
                                    onNavigateToForecast(city)
                                } else {
                                    showError = true
                                }
                            },
                            onAddToFavorites = {
                                coroutineScope.launch {
                                    appPreferences.addToFavorites((weatherState as CurrentWeatherViewModel.CurrentWeatherState.Success).current.name)
                                }
                            }
                        )
                        ThreeDayHourlyForecastDisplay(
                            threeDayHourlyForecast = (weatherState as CurrentWeatherViewModel.CurrentWeatherState.Success).threeDayHourlyForecast,
                            themeMode = themeMode,
                            language = language
                        )
                    }
                }
                is CurrentWeatherViewModel.CurrentWeatherState.Error -> {
                    Text(
                        text = (weatherState as CurrentWeatherViewModel.CurrentWeatherState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                else -> {
                    Text(
                        text = Strings.getString("enter_city_to_see_weather", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSelector(
    appPreferences: AppPreferences
) {
    val language by appPreferences.language.collectAsState(initial = AppPreferences.Language.HR)
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .size(width = 120.dp, height = 40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                text = when (language) {
                    AppPreferences.Language.HR -> Strings.getString("language_hr", language)
                    AppPreferences.Language.EN -> Strings.getString("language_en", language)
                    AppPreferences.Language.DE -> Strings.getString("language_de", language)
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppPreferences.Language.entries.forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (lang) {
                                AppPreferences.Language.HR -> Strings.getString("language_hr", language)
                                AppPreferences.Language.EN -> Strings.getString("language_en", language)
                                AppPreferences.Language.DE -> Strings.getString("language_de", language)
                            }
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            appPreferences.setLanguage(lang)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun WeatherDisplay(
    data: WeatherResponse,
    themeMode: AppPreferences.ThemeMode,
    language: AppPreferences.Language,
    onClick: () -> Unit,
    onAddToFavorites: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (themeMode == AppPreferences.ThemeMode.DARK) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onAddToFavorites) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = Strings.getString("add_to_favorites", language),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            val weatherIcon = data.weather.getOrNull(0)?.icon
            AsyncImage(
                model = if (weatherIcon != null) "https://openweathermap.org/img/wn/$weatherIcon@2x.png" else null,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = "${data.main.tempRounded}°C",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = Strings.getString("feels_like", language, data.main.feelsLikeRounded),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            val weatherDescription = data.weather.getOrNull(0)?.description ?: "N/A"
            Text(
                text = weatherDescription,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💧 ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Strings.getString("humidity", language, data.main.humidity),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💨 ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Strings.getString("wind", language, data.wind.speed),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = Strings.getString("sunrise", language, Instant.ofEpochSecond(data.sys.sunrise).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Strings.getString("sunset", language, Instant.ofEpochSecond(data.sys.sunset).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (data.rain?.volume != null) {
                    Text(
                        text = Strings.getString("rain", language, data.rain.volume),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (data.snow?.volume != null) {
                    Text(
                        text = Strings.getString("snow", language, data.snow.volume),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeDayHourlyForecastDisplay(threeDayHourlyForecast: List<ForecastItem>, themeMode: AppPreferences.ThemeMode, language: AppPreferences.Language) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = Strings.getString("three_day_forecast", language),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(threeDayHourlyForecast) { forecastItem ->
                ThreeDayHourlyForecastItemDisplay(
                    forecastItem = forecastItem,
                    themeMode = themeMode,
                    language = language
                )
            }
        }
    }
}

@Composable
fun ThreeDayHourlyForecastItemDisplay(forecastItem: ForecastItem, themeMode: AppPreferences.ThemeMode, language: AppPreferences.Language) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (themeMode == AppPreferences.ThemeMode.DARK) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getRelativeDateTime(forecastItem.dt, language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            val weatherIcon = forecastItem.weather.getOrNull(0)?.icon
            AsyncImage(
                model = if (weatherIcon != null) "https://openweathermap.org/img/wn/$weatherIcon.png" else null,
                contentDescription = "Hourly Weather Icon",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "${forecastItem.main.tempRounded}°C",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = Strings.getString("feels_like", language, forecastItem.main.feelsLikeRounded),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}