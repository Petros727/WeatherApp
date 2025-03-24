package hr.markopetricevic.weatherapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val sys: Sys,
    val rain: Rain? = null,
    val snow: Snow? = null
)


@Serializable
data class Main(
    val temp: Float,
    val feels_like: Float,
    val humidity: Int,
    val temp_min: Float? = null,
    val temp_max: Float? = null
) {
    val tempRounded: Int
        get() = temp.roundToInt()
    val feelsLikeRounded: Int
        get() = feels_like.roundToInt()
}

@Serializable
data class Weather(
    val icon: String,
    val description: String
)

@Serializable
data class Wind(
    val speed: Float
)

@Serializable
data class Sys(
    val sunrise: Long,
    val sunset: Long
)

@Serializable
data class Rain(
    @SerialName("3h") val volume: Float? = null
)

@Serializable
data class Snow(
    @SerialName("3h") val volume: Float? = null
)

@Serializable
data class ForecastResponse(
    val list: List<ForecastItem>
)

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val rain: Rain? = null,
    val snow: Snow? = null
)