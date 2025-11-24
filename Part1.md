# Part 1: Building a Weather MCP Server

In this workshop, you'll build a Model Context Protocol (MCP) server that provides weather information through tools, resources, and prompts.

## Overview

You'll be working with a Kotlin MCP server that integrates with the National Weather Service API to provide weather forecasts and alerts. The `WeatherForecastService` class is already implemented and handles the API calls.

**Note:** The National Weather Service API only provides data for US locations.

## Step 1: Understand the MCP Server Structure

The Kotlin MCP server is already set up in the `WeatherMCPStdioServer` project.

**File:** `Kotlin/WeatherMCPStdioServer/src/main/kotlin/io/modelcontextprotocol/sample/server/McpWeatherServer.kt`

The server uses the Kotlin MCP SDK and is configured to:
- Communicate via standard input/output (stdio transport)
- Register tools for weather forecasts and alerts
- Provide resources with reference data
- Define prompts for common workflows

**Key components:**
- `McpServer` - The main server instance
- `StdioServerTransport` - Handles stdio communication
- Tools, Resources, and Prompts are registered in the server initialization

## Step 2: Review Weather Tools

Tools are functions that can be called by AI assistants to perform actions.

**File:** `Kotlin/WeatherMCPStdioServer/src/main/kotlin/io/modelcontextprotocol/sample/server/McpWeatherServer.kt`

The server already includes two tools:

1. **GetAlerts** tool:
```kotlin
Tool(
    name = "get_alerts",
    description = "Get weather alerts for a US state",
    inputSchema = Tool.Input(
        properties = mapOf(
            "state" to Tool.Input.Property(
                type = "string",
                description = "Two-letter US state code (e.g. CA, NY)"
            )
        ),
        required = listOf("state")
    )
)
```

2. **GetForecast** tool:
```kotlin
Tool(
    name = "get_forecast",
    description = "Get weather forecast for a location",
    inputSchema = Tool.Input(
        properties = mapOf(
            "latitude" to Tool.Input.Property(
                type = "number",
                description = "Latitude of the location"
            ),
            "longitude" to Tool.Input.Property(
                type = "number",
                description = "Longitude of the location"
            )
        ),
        required = listOf("latitude", "longitude")
    )
)
```

**What this does:**
- `Tool` - Defines an MCP tool with name, description, and input schema
- `inputSchema` - Specifies the parameters the tool accepts
- The tools call the `WeatherForecastService` to fetch data from the National Weather Service API

## Step 2.1: Build the Project

Build the WeatherMCPStdioServer project to ensure everything compiles correctly:

```bash
cd Kotlin/WeatherMCPStdioServer
./gradlew build
```

## Step 2.2: Configure the MCP Server in Windsurf/VS Code

To use your MCP server with AI assistants, you need to configure it in the MCP settings file.

1. Locate your MCP config file (usually at `~/.codeium/windsurf/mcp_config.json` or similar)
2. Add the following configuration:

```json
{
  "mcpServers": {
    "weather": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/SeasonOfAIDemo/Kotlin/WeatherMCPStdioServer/build/libs/WeatherMCPStdioServer-all.jar"
      ]
    }
  }
}
```

**What this does:**
- Defines an MCP server named "weather"
- Configures the IDE to run your server using the built JAR file
- Points to your WeatherMCPStdioServer fat JAR
- Replace `/path/to/` with your actual project path

## Step 2.3: Test Your Tools with AI Assistant

Now let's test that your tools are working!

1. **Restart the MCP server:**
   - Restart your IDE or reload the MCP configuration
   - Ensure the server is running

2. **Open AI Chat**

3. **Try these test prompts:**
   - "@weather What's the weather forecast for New York City?"
   - "@weather Are there any weather alerts in California?"
   - "@weather Get the weather forecast for Chicago"

4. **Verify the tools are being called:**
   - You should see the AI using the `get_forecast` and `get_alerts` tools
   - The responses should include actual weather data from the National Weather Service

**Troubleshooting:**
- Check that `mcp_config.json` is properly formatted
- Verify the project builds: `./gradlew build`
- Check the server logs for any errors

### Alternative: Test with MCP Inspector

You can also test your MCP server using the MCP Inspector, which provides a web-based UI to interact with your server.

1. **Run the MCP Inspector:**
   ```bash
   npx @modelcontextprotocol/inspector java -jar Kotlin/WeatherMCPStdioServer/build/libs/WeatherMCPStdioServer-all.jar
   ```

2. **Open the Inspector:**
   - The command will output a URL (typically `http://localhost:5173`)
   - Open this URL in your browser

3. **Test your tools:**
   - You'll see a visual interface showing your available tools, resources, and prompts
   - Click on the "Tools" tab to see `get_alerts` and `get_forecast`
   - Try calling the tools with different parameters
   - View the JSON responses from the National Weather Service API

**Benefits:**
- Visual debugging interface
- See exact JSON messages
- Test without client configuration

## Step 3: Review Weather Resources

Resources provide static or dynamic data that can be accessed by AI assistants.

**File:** `Kotlin/WeatherMCPStdioServer/src/main/kotlin/io/modelcontextprotocol/sample/server/McpWeatherServer.kt`

The server already includes resources:

1. **State Codes Resource:**
```kotlin
Resource(
    uri = "weather://state-codes",
    name = "US State Codes",
    description = "List of US state codes for weather alerts",
    mimeType = "application/json"
)
```

Returns JSON data with state codes like:
```json
{
  "description": "US State codes for use with get_alerts tool",
  "states": [
    {"code": "AL", "name": "Alabama"},
    {"code": "CA", "name": "California"},
    {"code": "NY", "name": "New York"}
  ]
}
```

2. **Major Cities Resource:**
```kotlin
Resource(
    uri = "weather://majorcities-coords",
    name = "Major US Cities",
    description = "Coordinates for major US cities",
    mimeType = "application/json"
)
```

Returns JSON data with city coordinates like:
```json
{
  "description": "Pre-defined coordinates for major US cities",
  "cities": [
    {"name": "New York, NY", "latitude": 40.7128, "longitude": -74.0060},
    {"name": "Los Angeles, CA", "latitude": 34.0522, "longitude": -118.2437}
  ]
}
```

**What this does:**
- `Resource` - Defines an MCP resource with URI, name, and description
- Resources return JSON data that AI assistants can reference
- URIs uniquely identify each resource

## Step 3.1: Test Resources with AI Assistant

1. **Restart the MCP server:**
   - Restart your IDE or reload the MCP configuration

2. **Open AI Chat**

3. **Reference resources in your prompts:**
   - "@weather://state-codes What state codes are available?"
   - "@weather://majorcities-coords Show me the coordinates for major US cities"
   - "@weather What's the weather in one of the major cities?"

4. **Try questions that use the resources:**
   - "What state codes are available for weather alerts?"
   - "Show me the coordinates for major US cities"
   - "What's the weather in one of the major cities you know about?"

5. **Observe:**
   - The AI can reference the resource data
   - It knows the pre-defined state codes and city coordinates

## Step 4: Review Weather Prompts

Prompts are pre-defined templates that help AI assistants perform common tasks.

**File:** `Kotlin/WeatherMCPStdioServer/src/main/kotlin/io/modelcontextprotocol/sample/server/McpWeatherServer.kt`

The server already includes prompts:

1. **NewYorkWeather** prompt:
```kotlin
Prompt(
    name = "NewYorkWeather",
    description = "Get weather forecast and alerts for New York City",
    arguments = emptyList()
)
```
Returns: "Get the weather forecast for New York City (latitude: 40.7128, longitude: -74.0060) and check for any weather alerts in New York state."

2. **LosAngelesWeather** prompt:
```kotlin
Prompt(
    name = "LosAngelesWeather",
    description = "Get weather forecast and alerts for Los Angeles",
    arguments = emptyList()
)
```
Returns: "Get the weather forecast for Los Angeles (latitude: 34.0522, longitude: -118.2437) and check for any weather alerts in California."

3. **CityWeather** prompt (with parameter):
```kotlin
Prompt(
    name = "CityWeather",
    description = "Get weather forecast for any city",
    arguments = listOf(
        Prompt.Argument(
            name = "city",
            description = "Name of the city",
            required = true
        )
    )
)
```

**What this does:**
- `Prompt` - Defines an MCP prompt with name, description, and optional arguments
- Prompts return instructions that guide AI assistants
- Prompts can be parameterized (like `CityWeather`) to make them reusable

## Step 4.1: Test Prompts with AI Assistant

1. **Restart the MCP server:**
   - Restart your IDE or reload the MCP configuration

2. **Open AI Chat**

3. **Use the prompts:**
   - Reference prompts in your chat
   - Available prompts:
     - `NewYorkWeather` - Quick weather check for New York
     - `LosAngelesWeather` - Quick weather check for Los Angeles
     - `CityWeather` - Get weather for any city

4. **Try the prompts:**
   - "@weather Use NewYorkWeather prompt"
   - "@weather Use LosAngelesWeather prompt"
   - "@weather Use CityWeather prompt for Paris"

5. **Verify:**
   - The prompts automatically construct the right queries
   - The AI uses your tools (`get_forecast` and `get_alerts`) based on the prompt instructions
   - You get comprehensive weather information without typing detailed requests

**What's happening:**
- Prompts provide pre-written instructions that guide the AI
- They combine with your tools and resources to create powerful, reusable workflows

## Additional Testing Options

Beyond your IDE's AI assistant, you can test your MCP server in other ways:

1. **MCP Inspector** (covered earlier) - Visual debugging interface
2. **Claude Desktop** - Configure the server in Claude's settings
3. **Other MCP-compatible clients** - Any client that supports the MCP protocol

## Summary

You've successfully reviewed a Kotlin MCP server with:
- **2 Tools**: `get_alerts` and `get_forecast`
- **2 Resources**: State codes and major cities
- **3 Prompts**: New York weather, Los Angeles weather, and generic city weather

These components work together to provide a comprehensive weather information service that AI assistants can use to help users get weather data.

Now you're ready to move on to Part 2 where you'll implement your own MCP server for project allocation management!
