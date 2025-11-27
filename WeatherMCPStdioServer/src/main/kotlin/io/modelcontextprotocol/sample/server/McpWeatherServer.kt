package io.modelcontextprotocol.sample.server

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Starts an MCP server that provides weather-related tools for fetching active weather alerts by
 * state and weather forecasts by latitude/longitude.
 */
fun runMcpServer() {
    // Base URL for the Weather API
    val baseUrl = "https://api.weather.gov"

    // Create an HTTP client with a default request configuration and JSON content negotiation
    val httpClient = HttpClient {
        defaultRequest {
            url(baseUrl)
            headers {
                append("Accept", "application/geo+json")
                append("User-Agent", "WeatherApiClient/1.0")
            }
            contentType(ContentType.Application.Json)
        }
        // Install content negotiation plugin for JSON serialization/deserialization
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                },
            )
        }
    }

    // Create the MCP Server instance with a basic implementation
    val server =
        Server(
            Implementation(
                name = "weather", // Tool name is "weather"
                version = "1.0.0", // Version of the implementation
            ),
            ServerOptions(
                capabilities =
                    ServerCapabilities(
                        tools = ServerCapabilities.Tools(listChanged = true),
                        logging = null
                    ),
            ),
        )

    /**
     * TODO: Register a tool to fetch weather alerts by state
     * 
     * Tool name: "get_alerts"
     * Description: "Get weather alerts for a US state"
     * 
     * Input schema should have:
     * - Parameter "state" (type: string, required)
     * - Description: "Two-letter US state code (e.g. CA, NY)"
     * 
     * Implementation should:
     * 1. Extract the "state" parameter from request.arguments
     * 2. Validate that the state parameter is provided
     * 3. Call httpClient.getAlerts(state) to fetch alerts
     * 4. Return CallToolResult with the alerts as TextContent
     */
    // server.addTool(...) { request ->
    //     TODO("Implement get_alerts tool")
    // }

    /**
     * TODO: Register a tool to fetch weather forecast by latitude and longitude
     * 
     * Tool name: "get_forecast"
     * Description: "Get weather forecast for a location"
     * 
     * Input schema should have:
     * - Parameter "latitude" (type: number, required)
     * - Parameter "longitude" (type: number, required)
     * 
     * Implementation should:
     * 1. Extract the "latitude" and "longitude" parameters from request.arguments
     * 2. Validate that both parameters are provided and are valid numbers
     * 3. Call httpClient.getForecast(latitude, longitude) to fetch forecast
     * 4. Return CallToolResult with the forecast as TextContent
     */
    // server.addTool(...) { request ->
    //     TODO("Implement get_forecast tool")
    // }

    // Create a transport using standard IO for server communication
    val transport =
        StdioServerTransport(
            System.`in`.asInput(),
            System.out.asSink().buffered(),
        )

    runBlocking {
        val session = server.connect(transport)
        val done = Job()
        session.onClose { done.complete() }
        done.join()
    }
}
