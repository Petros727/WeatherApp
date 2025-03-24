package hr.markopetricevic.weatherapp.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.markopetricevic.weatherapp.data.AppPreferences
import hr.markopetricevic.weatherapp.data.ForecastItem
import hr.markopetricevic.weatherapp.data.ForecastResponse
import hr.markopetricevic.weatherapp.data.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

sealed class ForecastState {
    data object Idle : ForecastState()
    data object Loading : ForecastState()
    data class Success(
        val forecast: ForecastResponse,
        val dailyForecast: List<DailyForecast>
    ) : ForecastState()
    data class Error(val message: String) : ForecastState()
}

data class DailyForecast(
    val date: String,
    val minTemp: Int,
    val maxTemp: Int,
    val representativeItem: ForecastItem
)

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {
    var forecastState = mutableStateOf<ForecastState>(ForecastState.Idle)

    private fun isValidCity(city: String): Boolean {
        val trimmedCity = city.trim()
        return trimmedCity.isNotBlank()
    }

    fun fetchFiveDayForecast(city: String) {
        if (!isValidCity(city)) {
            forecastState.value = ForecastState.Error("Invalid city name")
            return
        }

        viewModelScope.launch {
            forecastState.value = ForecastState.Loading
            try {
                val lang = appPreferences.language.first().code
                val forecastResult = repository.getFiveDayForecast(city, lang)
                if (forecastResult.isSuccess) {
                    val forecast = forecastResult.getOrThrow()
                    Log.d("ForecastViewModel", "Forecast List Size: ${forecast.list.size}")
                    if (forecast.list.isEmpty()) {
                        forecastState.value = ForecastState.Error("No forecast data available")
                        return@launch
                    }

                    forecast.list.forEachIndexed { index, item ->
                        val date = Instant.ofEpochSecond(item.dt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        Log.d("ForecastViewModel", "Forecast[$index] - Date: $date, Temp: ${item.main.temp}, Min: ${item.main.temp_min}, Max: ${item.main.temp_max}")
                    }

                    val groupedByDay = forecast.list.groupBy {
                        Instant.ofEpochSecond(it.dt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()
                    }

                    val dailyForecast = groupedByDay.mapNotNull { (date, items) ->
                        if (items.isEmpty()) return@mapNotNull null

                        val minTemp = items.minOfOrNull { it.main.temp_min ?: it.main.temp }?.roundToInt() ?: 0
                        val maxTemp = items.maxOfOrNull { it.main.temp_max ?: it.main.temp }?.roundToInt() ?: 0

                        val noonTimestamp = LocalDate.parse(date)
                            .atTime(12, 0)
                            .atZone(ZoneId.systemDefault())
                            .toEpochSecond()

                        val representativeItem = items.minByOrNull { item ->
                            abs(item.dt - noonTimestamp)
                        } ?: items.first()

                        DailyForecast(date, minTemp, maxTemp, representativeItem)
                    }.take(5)

                    if (dailyForecast.isEmpty()) {
                        forecastState.value = ForecastState.Error("No daily forecast data available")
                        return@launch
                    }

                    dailyForecast.forEach {
                        Log.d("ForecastViewModel", "Daily Forecast - Date: ${it.date}, Min: ${it.minTemp}, Max: ${it.maxTemp}, Description: ${it.representativeItem.weather.getOrNull(0)?.description ?: "N/A"}")
                    }

                    forecastState.value = ForecastState.Success(
                        forecast = forecast,
                        dailyForecast = dailyForecast
                    )
                } else {
                    val error = forecastResult.exceptionOrNull()
                    forecastState.value = ForecastState.Error(
                        when {
                            error?.message?.contains("404") == true -> "City not found"
                            error?.message?.contains("401") == true -> "Invalid API key"
                            else -> "Failed to load forecast: ${error?.message}"
                        }
                    )
                }
            } catch (e: Exception) {
                forecastState.value = ForecastState.Error("Failed to load forecast: ${e.message}")
            }
        }
    }
}