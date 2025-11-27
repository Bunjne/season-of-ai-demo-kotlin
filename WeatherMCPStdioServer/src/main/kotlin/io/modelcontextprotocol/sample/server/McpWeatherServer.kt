package io.modelcontextprotocol.sample.server

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.sample.server.prompt.registerWeatherPrompts
import io.modelcontextprotocol.sample.server.resource.registerWeatherResources
import io.modelcontextprotocol.sample.server.tool.registerWeatherTools
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json

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
                name = "weather", // Server name is "weather"
                version = "1.0.0", // Version of the implementation
            ),
            ServerOptions(
                capabilities =
                    ServerCapabilities(
                        tools = ServerCapabilities.Tools(listChanged = true),
                        prompts = ServerCapabilities.Prompts(listChanged = true),
                        resources = ServerCapabilities.Resources(subscribe = false, listChanged = true),
                        logging = null
                    ),
            ),
        )

    // Register weather tools (get_alerts, get_forecast)
    server.registerWeatherTools(httpClient)
    
    // Register weather prompts (weather_alerts, weather_forecast)
    server.registerWeatherPrompts()
    
    // Register weather resources (US states, major cities)
    server.registerWeatherResources()

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
