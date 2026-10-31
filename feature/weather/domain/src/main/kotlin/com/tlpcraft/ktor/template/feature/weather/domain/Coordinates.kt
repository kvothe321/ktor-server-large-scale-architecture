package com.tlpcraft.ktor.template.feature.weather.domain

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.ValidationError
import com.tlpcraft.ktor.template.core.domain.validated

/** A point on Earth in decimal degrees (WGS84). Build from untrusted input with [of]. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        private val LATITUDE_RANGE = -90.0..90.0
        private val LONGITUDE_RANGE = -180.0..180.0

        fun of(latitude: Double, longitude: Double): Outcome<Coordinates, ValidationError> = validated(
            rules = {
                check("latitude", latitude in LATITUDE_RANGE) { "must be between -90 and 90" }
                check("longitude", longitude in LONGITUDE_RANGE) { "must be between -180 and 180" }
            },
            create = { Coordinates(latitude, longitude) }
        )
    }
}
