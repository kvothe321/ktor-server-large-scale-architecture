package com.tlpcraft.ktor.template.feature.weather.domain

/**
 * Coarse weather condition and the WMO weather interpretation codes it covers.
 * Code table: https://open-meteo.com/en/docs
 */
enum class WeatherCondition(private val wmoCodes: Set<Int>) {
    CLEAR(setOf(0)),
    PARTLY_CLOUDY(setOf(1, 2)),
    OVERCAST(setOf(3)),
    FOG(setOf(45, 48)),
    DRIZZLE((51..57).toSet()),
    RAIN((61..67).toSet() + (80..82)),
    SNOW((71..77).toSet() + setOf(85, 86)),
    THUNDERSTORM((95..99).toSet()),
    UNKNOWN(emptySet());

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = entries.firstOrNull { code in it.wmoCodes } ?: UNKNOWN
    }
}
