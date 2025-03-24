package hr.markopetricevic.weatherapp.data

import hr.markopetricevic.weatherapp.BuildConfig
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService
) {
    private val apiKey = BuildConfig.WEATHER_API_KEY

    suspend fun getCurrentWeather(city: String, lang: String = "en"): Result<WeatherResponse> {
        return try {
            val safeLang = validateLang(lang)
            val response = apiService.getCurrentWeather(city = city, apiKey = apiKey, lang = safeLang)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFiveDayForecast(city: String, lang: String = "en"): Result<ForecastResponse> {
        return try {
            val safeLang = validateLang(lang)
            val response = apiService.getFiveDayForecast(city = city, apiKey = apiKey, lang = safeLang)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getThreeDayHourlyForecast(city: String, lang: String = "en"): Result<List<ForecastItem>> {
        return try {
            val safeLang = validateLang(lang)
            val response = apiService.getThreeDayHourlyForecast(city = city, apiKey = apiKey, lang = safeLang)
            val threeDayHourlyForecast = response.list.take(24)
            Result.success(threeDayHourlyForecast)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateLang(lang: String): String {
        val validLangs = listOf("hr", "en", "de")
        return if (lang.isBlank() || lang !in validLangs) "en" else lang
    }
}