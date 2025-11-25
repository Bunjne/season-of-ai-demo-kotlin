# Kotlin MCP Weather STDIO Server

This project demonstrates how to build a Model Context Protocol (MCP) server in Kotlin that provides weather-related
tools by consuming the National Weather Service (weather.gov) API. The server uses STDIO as the transport layer and
leverages the Kotlin MCP SDK to expose weather forecast and alert tools.

For more information about the MCP SDK and protocol, please refer to
the [MCP documentation](https://modelcontextprotocol.io/introduction).

## Prerequisites

- Java 17 or later
- Gradle (or the Gradle wrapper provided with the project)
- Basic understanding of MCP concepts
- Basic understanding of Kotlin and Kotlin ecosystems (sush as kotlinx-serialization, coroutines, ktor)

## MCP Weather Server

The project provides:

- A lightweight MCP server built with Kotlin.
- STDIO transport layer implementation for server-client communication.
- Two weather tools:
    - **Weather Forecast Tool** — returns details such as temperature, wind information, and a detailed forecast for a
      given latitude/longitude.
    - **Weather Alerts Tool** — returns active weather alerts for a given US state.

## Building and running

Use the Gradle wrapper to build the application. In a terminal run:

```shell
./gradlew clean build
```

To run the server:

```shell
java -jar build/libs/weather-stdio-server-0.1.0-all.jar
```

> [!NOTE]
> The server uses STDIO transport, so it is typically launched in an environment where the client connects via standard
> input/output.

## Tool Implementation

The project registers two MCP tools using the Kotlin MCP SDK. Below is an overview of the core tool implementations:

### 1. Weather Forecast Tool

This tool fetches the weather forecast for a specific latitude and longitude using the `weather.gov` API.

Example tool registration in Kotlin:

```kotlin
server.addTool(
    name = "get_forecast",
    description = """
            Get weather forecast for a specific latitude/longitude
        """.trimIndent(),
    inputSchema = Tool.Input(
        properties = JsonObject(
            mapOf(
                "latitude" to JsonObject(mapOf("type" to JsonPrimitive("number"))),
                "longitude" to JsonObject(mapOf("type" to JsonPrimitive("number"))),
            )
        ),
        required = listOf("latitude", "longitude")
    )
) { request ->
    // Implementation tool
}
```

### 2. Weather Alerts Tool

This tool retrieves active weather alerts for a US state.

Example tool registration in Kotlin:

```kotlin
server.addTool(
    name = "get_alerts",
    description = """
        Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)
    """.trimIndent(),
    inputSchema = Tool.Input(
        properties = JsonObject(
            mapOf(
                "state" to JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("string"),
                        "description" to JsonPrimitive("Two-letter US state code (e.g. CA, NY)")
                    )
                ),
            )
        ),
        required = listOf("state")
    )
) { request ->
    // Implementation tool
}
```

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

