package io.modelcontextprotocol.sample.server.tool

import io.ktor.client.HttpClient
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.sample.server.data.service.getAlerts
import io.modelcontextprotocol.sample.server.data.service.getForecast
import io.modelcontextprotocol.sample.server.extension.buildNumberParameter
import io.modelcontextprotocol.sample.server.extension.buildStringParameter
import io.modelcontextprotocol.sample.server.extension.getDoubleOrNull
import io.modelcontextprotocol.sample.server.extension.getStringOrNull
import io.modelcontextprotocol.sample.server.extension.toolInput

fun Server.registerWeatherTools(httpClient: HttpClient) {
    registerGetAlertsTool(httpClient)
    registerGetForecastTool(httpClient)
}

private fun Server.registerGetAlertsTool(httpClient: HttpClient) {
    addTool(
        name = "get_alerts",
        description = "Get weather alerts for a US state",
        inputSchema = toolInput(
            "state" to buildStringParameter("The US state to get alerts for (e.g., CA, NY, TX)."),
            required = listOf("state")
        )
    ) { request ->
        val state = request.arguments.getStringOrNull("state")
            ?: return@addTool CallToolResult(
                content = listOf(TextContent("The 'state' parameter is required."))
            )

        val alerts = httpClient.getAlerts(state)
        CallToolResult(content = alerts.map { TextContent(it) })
    }
}

private fun Server.registerGetForecastTool(httpClient: HttpClient) {
    addTool(
        name = "get_forecast",
        description = "Get weather forecast for a location.",
        inputSchema = toolInput(
            "latitude" to buildNumberParameter("Latitude of the location."),
            "longitude" to buildNumberParameter("Longitude of the location."),
            required = listOf("latitude", "longitude")
        )
    ) { request ->
        val latitude = request.arguments.getDoubleOrNull("latitude")
        val longitude = request.arguments.getDoubleOrNull("longitude")

        if (latitude == null || longitude == null) {
            return@addTool CallToolResult(
                content = listOf(
                    TextContent("The 'latitude' and 'longitude' parameters are required.")
                )
            )
        }

        val forecast = httpClient.getForecast(latitude, longitude)
        CallToolResult(content = forecast.map { TextContent(it) })
    }
}
