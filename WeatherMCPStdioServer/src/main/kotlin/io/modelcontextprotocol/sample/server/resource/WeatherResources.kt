package io.modelcontextprotocol.sample.server.resource

import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.sample.server.extension.addObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray

fun Server.registerWeatherResources() {
    registerUsStatesResource()
    registerMajorCitiesResource()
}

private fun Server.registerUsStatesResource() {
    addResource(
        uri = "weather://state-codes",
        name = "State Codes",
        description = "List of US state codes and names for weather alerts",
        mimeType = "application/json"
    ) {
        val statesJson = buildJsonArray {
            addObject("code" to "AL", "name" to "Alabama")
            addObject("code" to "AK", "name" to "Alaska")
            addObject("code" to "CA", "name" to "California")
            addObject("code" to "NY", "name" to "New York")
        }

        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = "weather://state-codes",
                    mimeType = "application/json",
                    text = Json.encodeToString(
                        JsonArray.serializer(),
                        statesJson
                    )
                )
            )
        )
    }
}

private fun Server.registerMajorCitiesResource() {
    addResource(
        uri = "weather://majorcities-coords",
        name = "Major Cities Coordinates",
        description = "Coordinates for major US cities to use with weather forecast",
        mimeType = "application/json"
    ) {
        val citiesJson = buildJsonArray {
            // Simplified list matching C# implementation
            addObject("name" to "New York, NY", "latitude" to 40.7128, "longitude" to -74.0060)
            addObject("name" to "Los Angeles, CA", "latitude" to 34.0522, "longitude" to -118.2437)
            addObject("name" to "Chicago, IL", "latitude" to 41.8781, "longitude" to -87.6298)
            addObject("name" to "Houston, TX", "latitude" to 29.7604, "longitude" to -95.3698)
        }

        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = "weather://majorcities-coords",
                    mimeType = "application/json",
                    text = Json.encodeToString(
                        JsonArray.serializer(),
                        citiesJson
                    )
                )
            )
        )
    }
}
