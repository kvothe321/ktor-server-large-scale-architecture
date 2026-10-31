package com.tlpcraft.ktor.template.feature.weather.presentation

import com.tlpcraft.ktor.template.core.domain.Outcome
import com.tlpcraft.ktor.template.core.domain.ValidationError
import com.tlpcraft.ktor.template.core.domain.flatMap
import com.tlpcraft.ktor.template.core.domain.mapError
import com.tlpcraft.ktor.template.core.model.FieldViolation
import com.tlpcraft.ktor.template.core.server.ApiProblem
import com.tlpcraft.ktor.template.core.server.problem
import com.tlpcraft.ktor.template.core.server.respondOutcome
import com.tlpcraft.ktor.template.feature.weather.domain.GetCurrentWeatherUseCase
import com.tlpcraft.ktor.template.feature.weather.domain.WeatherError
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe

/** HTTP endpoints of the weather feature, mounted at `/weather`. */
class WeatherRoutes(private val getCurrentWeather: GetCurrentWeatherUseCase) {
    fun register(route: Route) {
        route.get("/weather/current") {
            val report = call.coordinateParameters()
                .mapError(WeatherError::Invalid)
                .flatMap { (latitude, longitude) -> getCurrentWeather(latitude, longitude) }
            call.respondOutcome(report, toProblem = WeatherError::toProblem) { it.toResponse() }
        }.describe {
            tag("Weather")
            summary = "Current weather at a location"
            description = "Served from a local cache when fresh; falls back to a stale reading if the provider is down."
            parameters {
                query("latitude") {
                    description = "Decimal degrees, -90 to 90"
                    required = true
                }
                query("longitude") {
                    description = "Decimal degrees, -180 to 180"
                    required = true
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Current conditions"
                    schema = jsonSchema<WeatherResponse>()
                }
                problem(HttpStatusCode.BadRequest, "Missing or invalid coordinates")
                problem(HttpStatusCode.ServiceUnavailable, "Provider down and nothing cached")
            }
        }
    }
}

private fun ApplicationCall.coordinateParameters(): Outcome<Pair<Double, Double>, ValidationError> {
    val latitude = request.queryParameters["latitude"]?.toDoubleOrNull()
    val longitude = request.queryParameters["longitude"]?.toDoubleOrNull()
    if (latitude != null && longitude != null) return Outcome.Success(latitude to longitude)
    val violations = listOfNotNull(
        FieldViolation("latitude", NUMBER_REQUIRED).takeIf { latitude == null },
        FieldViolation("longitude", NUMBER_REQUIRED).takeIf { longitude == null }
    )
    return Outcome.Failure(ValidationError(violations))
}

private const val NUMBER_REQUIRED = "is required and must be a number"

private fun WeatherError.toProblem(): ApiProblem = when (this) {
    is WeatherError.Invalid -> ApiProblem.validation(error)
    is WeatherError.Unavailable -> ApiProblem.serviceUnavailable("Weather data is temporarily unavailable.")
}
