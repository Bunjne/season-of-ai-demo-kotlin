package io.modelcontextprotocol.sample.server.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.modelcontextprotocol.sample.server.data.model.Alert
import io.modelcontextprotocol.sample.server.data.model.Forecast
import io.modelcontextprotocol.sample.server.data.model.Points

/**
 * Weather API client extensions.
 * Provides clean extension functions for fetching weather data from weather.gov API.
 */

/**
 * Fetches weather forecast for the given coordinates.
 * @param latitude Latitude coordinate
 * @param longitude Longitude coordinate
 * @return List of formatted forecast strings for each period
 */
suspend fun HttpClient.getForecast(latitude: Double, longitude: Double): List<String> {
    val points = get("/points/$latitude,$longitude").body<Points>()
    val forecast = get(points.properties.forecast).body<Forecast>()

    return forecast.properties.periods.map { it.toFormattedString() }
}

/**
 * Fetches active weather alerts for the given US state.
 * @param state Two-letter US state code (e.g., CA, NY)
 * @return List of formatted alert strings
 */
suspend fun HttpClient.getAlerts(state: String): List<String> {
    val alerts = get("/alerts/active/area/$state").body<Alert>()
    return alerts.features.map { it.toFormattedString() }
}

private fun Forecast.Period.toFormattedString(): String = """
    $name:
    Temperature: $temperature $temperatureUnit
    Wind: $windSpeed $windDirection
    Forecast: $detailedForecast
""".trimIndent()

private fun Alert.Feature.toFormattedString(): String = """
    Event: ${properties.event}
    Area: ${properties.areaDesc}
    Severity: ${properties.severity}
    Description: ${properties.description}
    Instruction: ${properties.instruction ?: "None"}
""".trimIndent()
