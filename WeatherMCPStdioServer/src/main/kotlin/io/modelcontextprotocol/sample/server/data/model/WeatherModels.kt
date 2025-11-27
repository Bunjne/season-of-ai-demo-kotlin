package io.modelcontextprotocol.sample.server.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Points(val properties: Properties) {
    @Serializable
    data class Properties(val forecast: String)
}

@Serializable
data class Forecast(val properties: Properties) {
    @Serializable
    data class Properties(val periods: List<Period>)

    @Serializable
    data class Period(
        val number: Int,
        val name: String,
        val startTime: String,
        val endTime: String,
        val isDaytime: Boolean,
        val temperature: Int,
        val temperatureUnit: String,
        val temperatureTrend: String,
        val probabilityOfPrecipitation: JsonObject,
        val windSpeed: String,
        val windDirection: String,
        val shortForecast: String,
        val detailedForecast: String
    )
}

@Serializable
data class Alert(val features: List<Feature>) {
    @Serializable
    data class Feature(val properties: Properties)

    @Serializable
    data class Properties(
        val event: String,
        val areaDesc: String,
        val severity: String,
        val description: String,
        val instruction: String?
    )
}
