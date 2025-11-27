# Kotlin MCP Weather STDIO Server - Student Template

This is a template project for building a Model Context Protocol (MCP) server in Kotlin that provides weather-related
tools by consuming the National Weather Service (weather.gov) API. The server uses STDIO as the transport layer and
leverages the Kotlin MCP SDK to expose weather forecast and alert tools.

**This is a learning exercise.** You will need to implement the core functionality yourself by following the TODO comments in the code.

For more information about the MCP SDK and protocol, please refer to
the [MCP documentation](https://modelcontextprotocol.io/introduction).

## Prerequisites

- Java 17 or later
- Gradle (or the Gradle wrapper provided with the project)
- Basic understanding of MCP concepts
- Basic understanding of Kotlin and Kotlin ecosystems (such as kotlinx-serialization, coroutines, ktor)

## Learning Objectives

By completing this project, you will learn how to:

- Build an MCP server using the Kotlin MCP SDK
- Implement STDIO transport for server-client communication
- Create MCP tools with proper input schemas
- Make HTTP requests using Ktor client
- Parse JSON responses using kotlinx-serialization
- Handle errors and validate parameters

## Your Task

You need to implement two MCP tools by registering them in the server:

- **Weather Forecast Tool (`get_forecast`)** — Should return details such as temperature, wind information, and a detailed forecast for a given latitude/longitude.
- **Weather Alerts Tool (`get_alerts`)** — Should return active weather alerts for a given US state.

## What's Provided

**`WeatherApi.kt`** - ✅ Already implemented with HTTP client extension functions:
- `HttpClient.getForecast(latitude, longitude)` - Fetches weather forecast data
- `HttpClient.getAlerts(state)` - Fetches weather alerts data

These functions are ready to use! You just need to register the MCP tools that call them.

## What You Need to Implement

**`McpWeatherServer.kt`** - Register the MCP tools:
- Register `get_alerts` tool with proper input schema
- Register `get_forecast` tool with proper input schema

Look for `TODO` comments in `McpWeatherServer.kt` for detailed implementation instructions.

## Building and Running

The project will build successfully, but the tools won't be registered until you uncomment and complete the implementation.

Use the Gradle wrapper to build the application. In a terminal run:

```shell
./gradlew clean build
```

To run the server (after implementation):

```shell
java -jar build/libs/weather-stdio-server-0.1.0-all.jar
```

> [!NOTE]
> The server uses STDIO transport, so it is typically launched in an environment where the client connects via standard
> input/output.

## Implementation Guide

### Implement Tool Registration in `McpWeatherServer.kt`

#### `get_alerts` tool:
```kotlin
server.addTool(
    name = "get_alerts",
    description = "Get weather alerts for a US state",
    inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("state") {
                put("type", "string")
                put("description", "Two-letter US state code (e.g. CA, NY)")
            }
        },
        required = listOf("state")
    )
) { request ->
    // Extract state parameter
    // Validate it's not null
    // Call httpClient.getAlerts(state)
    // Return CallToolResult with TextContent
}
```

#### `get_forecast` tool:
```kotlin
server.addTool(
    // Implement tool registration
) { request ->
    // Extract latitude and longitude parameters
    // Validate they are not null and are valid numbers
    // Call httpClient.getForecast(latitude, longitude)
    // Return CallToolResult with TextContent
}
```

## API Reference

### Weather.gov API Endpoints

The National Weather Service API provides the following endpoints:

1. **Points Endpoint**: `/points/{latitude},{longitude}`
   - Returns metadata for a location, including the forecast URL
   - Response includes a `properties.forecast` field with the forecast endpoint URL

2. **Forecast Endpoint**: URL from points response
   - Returns detailed weather forecast with multiple periods
   - Each period includes temperature, wind, and forecast details

3. **Alerts Endpoint**: `/alerts/active/area/{state}`
   - Returns active weather alerts for a given state
   - State should be a two-letter code (e.g., "CA", "NY")

### Data Models

The project includes pre-defined data classes for parsing API responses:

- **`Points`** - Response from the points endpoint
- **`Forecast`** - Response from the forecast endpoint with periods
- **`Alert`** - Response from the alerts endpoint with features

These are already defined in `WeatherApi.kt` and ready to use with kotlinx-serialization.

## Client Integration

### Kotlin Client Example

Since the server uses STDIO for transport, the client typically connects via standard input/output streams. A sample
client implementation can be found in the tests, demonstrating how to send tool requests and process responses.

### Claude for Desktop

To integrate with Claude Desktop, add the following configuration to your Claude Desktop settings:

```json
{
  "mcpServers": {
    "weather": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/<your-jar-name>.jar"
      ]
    }
  }
}
```

> [!NOTE]
> Replace `/absolute/path/to/<your-jar-name>.jar` with the actual absolute path to your built jar file.

## Debugging with MCP Inspector

The [MCP Inspector](https://github.com/modelcontextprotocol/inspector) is a visual testing tool that helps you debug and test your MCP server interactively.

### Prerequisites

- Node.js and npm installed
- Your MCP server configured in `mcp_config.json`

### Steps to Debug

1. **Run the MCP Inspector with your server configuration:**

   ```bash
   npx @modelcontextprotocol/inspector --config <ABSOLUTE_PATH_TO_MCP_CONFIG> --server <server_name>
   ```

   Replace `<ABSOLUTE_PATH_TO_MCP_CONFIG>` with the absolute path to your `mcp_config.json` file. 
   
   Replace `<server_name>` with the name of your server. 
   
   For example:
   ```bash
   npx @modelcontextprotocol/inspector --config /Users/username/.codeium/windsurf/mcp_config.json --server weather
   ```

2. **Open the Inspector in your browser:**

   After running the command, you'll see output with a URL and authentication token. Open your browser and navigate to:
   ```
   http://localhost:6274/?MCP_PROXY_AUTH_TOKEN=<YOUR_TOKEN>
   ```

   Replace `<YOUR_TOKEN>` with the token displayed in the terminal output.

3. **Add Authentication Header:**

   In the Inspector UI, add the authentication header:
   - Header: `Authorization`
   - Value: `Bearer <YOUR_TOKEN>`

4. **Test Your Tools:**

   You can now interactively test your MCP tools:
   - View available tools and their schemas
   - Execute tools with custom parameters
   - See real-time responses and errors
   - Debug tool behavior

### Example mcp_config.json Entry

```json
{
  "mcpServers": {
    "weather": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/weather-stdio-server-0.1.0-all.jar"
      ]
    }
  }
}
```

## Additional Resources

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [MCP Inspector](https://github.com/modelcontextprotocol/inspector)
- [Kotlin MCP SDK](https://github.com/modelcontextprotocol/kotlin-sdk)
- [Ktor Client Documentation](https://ktor.io/docs/welcome.html)
- [Kotlinx Serialization](https://kotlinlang.org/docs/serialization.html)

