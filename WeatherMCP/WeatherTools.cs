using ModelContextProtocol.Server;
using System.ComponentModel;

namespace WeatherMCP;

[McpServerToolType]
public static class WeatherTools
{
    // Add tools to that return weather alerts for a US state and weather forecast for a location
    // Each tool uses the WeatherForecastService to perform the actual API calls

    [McpServerTool, Description("Get weather alerts for a US state")]
    public static async Task<string> GetAlerts(
        WeatherForecastService service,
        [Description("The US state to get alerts for (e.g., CA, NY, TX).")] string state)
    {
        return await service.GetAlerts(state);
    }

    [McpServerTool, Description("Get weather forecast for a location.")]
    public static async Task<string> GetForecast(
        WeatherForecastService service,
        [Description("Latitude of the location.")] double latitude,
        [Description("Longitude of the location.")] double longitude)
    {
        return await service.GetForecast(latitude, longitude);
    }
}
