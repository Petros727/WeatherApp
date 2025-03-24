package hr.markopetricevic.weatherapp.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.markopetricevic.weatherapp.data.AppPreferences
import hr.markopetricevic.weatherapp.data.ForecastItem
import hr.markopetricevic.weatherapp.data.WeatherRepository
import hr.markopetricevic.weatherapp.data.WeatherResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _weatherState = mutableStateOf<CurrentWeatherState>(CurrentWeatherState.Initial)
    val weatherState: State<CurrentWeatherState> get() = _weatherState

    private fun isValidCity(city: String): Boolean {
        val trimmedCity = city.trim()
        return trimmedCity.isNotBlank()
    }

    fun fetchCurrentWeather(city: String) {
        if (!isValidCity(city)) {
            _weatherState.value = CurrentWeatherState.Error("Invalid city name")
            return
        }

        _weatherState.value = CurrentWeatherState.Loading

        viewModelScope.launch {
            try {
                val lang = appPreferences.language.first().code
                val currentWeatherResult = weatherRepository.getCurrentWeather(city, lang)
                if (currentWeatherResult.isFailure) {
                    throw currentWeatherResult.exceptionOrNull() ?: Exception("Failed to fetch current weather")
                }
                val currentWeather = currentWeatherResult.getOrThrow()

                val threeDayHourlyForecastResult = weatherRepository.getThreeDayHourlyForecast(city, lang)
                if (threeDayHourlyForecastResult.isFailure) {
                    throw threeDayHourlyForecastResult.exceptionOrNull() ?: Exception("Failed to fetch forecast")
                }
                val threeDayHourlyForecast = threeDayHourlyForecastResult.getOrThrow()

                _weatherState.value = CurrentWeatherState.Success(currentWeather, threeDayHourlyForecast)
            } catch (e: Exception) {
                _weatherState.value = CurrentWeatherState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    sealed class CurrentWeatherState {
        data object Initial : CurrentWeatherState()
        data object Loading : CurrentWeatherState()
        data class Success(val current: WeatherResponse, val threeDayHourlyForecast: List<ForecastItem>) : CurrentWeatherState()
        data class Error(val message: String) : CurrentWeatherState()
    }
}