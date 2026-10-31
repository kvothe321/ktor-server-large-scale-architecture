package com.tlpcraft.ktor.template.feature.weather.domain

import com.tlpcraft.ktor.template.core.domain.DomainError
import com.tlpcraft.ktor.template.core.domain.ValidationError

sealed interface WeatherError : DomainError {
    data class Invalid(val error: ValidationError) : WeatherError {
        override val message: String get() = error.message
    }

    /** The provider failed and nothing usable was cached. */
    data class Unavailable(val reason: String, override val cause: Throwable? = null) : WeatherError {
        override val message: String get() = "Weather unavailable: $reason"
    }
}
