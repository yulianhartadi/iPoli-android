package io.ipoli.android.planday.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.awareness.state.Weather as AndroidWeather

data class Temperature(val celsiusValue: Float, val fahrenheitValue: Float)

data class Weather(
    val conditions: Set<Condition>,
    val temperature: Temperature,
    val feelsLikeTemperature: Temperature
) {
    enum class Condition {
        UNKNOWN, CLEAR, CLOUDY, FOGGY, HAZY, ICY, RAINY, SNOWY, STORMY, WINDY
    }
}

interface WeatherRepository {
    fun getCurrentWeather(): Weather
}

class AndroidWeatherRepository(val context: Context) : WeatherRepository {

    @SuppressLint("MissingPermission")
    override fun getCurrentWeather() =
        Tasks.await(Awareness.getSnapshotClient(context).weather).let {
            val w = it.weather
            val conditions = w.conditions.map {
                when (it) {
                    AndroidWeather.CONDITION_CLEAR -> Weather.Condition.CLEAR
                    AndroidWeather.CONDITION_CLOUDY -> Weather.Condition.CLOUDY
                    AndroidWeather.CONDITION_FOGGY -> Weather.Condition.FOGGY
                    AndroidWeather.CONDITION_HAZY -> Weather.Condition.HAZY
                    AndroidWeather.CONDITION_ICY -> Weather.Condition.ICY
                    AndroidWeather.CONDITION_RAINY -> Weather.Condition.RAINY
                    AndroidWeather.CONDITION_SNOWY -> Weather.Condition.SNOWY
                    AndroidWeather.CONDITION_STORMY -> Weather.Condition.STORMY
                    AndroidWeather.CONDITION_WINDY -> Weather.Condition.WINDY
                    else -> Weather.Condition.UNKNOWN
                }
            }
            Weather(
                conditions = conditions.toSet(),
                temperature = Temperature(
                    w.getTemperature(AndroidWeather.CELSIUS),
                    w.getTemperature(AndroidWeather.FAHRENHEIT)
                ),
                feelsLikeTemperature = Temperature(
                    w.getFeelsLikeTemperature(AndroidWeather.CELSIUS),
                    w.getFeelsLikeTemperature(AndroidWeather.FAHRENHEIT)
                )
            )
        }

}