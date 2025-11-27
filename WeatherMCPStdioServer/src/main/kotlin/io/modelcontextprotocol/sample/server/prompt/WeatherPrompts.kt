package io.modelcontextprotocol.sample.server.prompt

import io.modelcontextprotocol.kotlin.sdk.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.Role
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server

fun Server.registerWeatherPrompts() {
    registerNewYorkWeatherPrompt()
    registerLosAngelesWeatherPrompt()
}

private fun Server.registerNewYorkWeatherPrompt() {
    addPrompt(
        name = "NewYorkWeather",
        description = "Get weather forecast and alerts for New York City"
    ) {
        GetPromptResult(
            description = "Weather forecast and alerts for New York City",
            messages = listOf(
                PromptMessage(
                    role = Role.user,
                    content = TextContent(
                        "Get the weather forecast for New York City (latitude: 40.7128, longitude: -74.0060) " +
                                "and check for any weather alerts in New York state."
                    )
                )
            )
        )
    }
}

private fun Server.registerLosAngelesWeatherPrompt() {
    addPrompt(
        name = "LosAngelesWeather",
        description = "Get weather forecast and alerts for Los Angeles"
    ) {
        GetPromptResult(
            description = "Weather forecast and alerts for Los Angeles",
            messages = listOf(
                PromptMessage(
                    role = Role.user,
                    content = TextContent(
                        "Get the weather forecast for Los Angeles (latitude: 34.0522, longitude: -118.2437) " +
                                "and check for any weather alerts in California."
                    )
                )
            )
        )
    }
}
