using ModelContextProtocol.Server;
using System.ComponentModel;

namespace QuickstartWeatherServer.Tools;

[McpServerPromptType]
public class WeatherPrompts
{
    [McpServerPrompt(Name = "NewYorkWeather"), Description("Get weather forecast and alerts for New York City")]
    public static string NewYorkWeather()
    {
        return "Get the weather forecast for New York City (latitude: 40.7128, longitude: -74.0060) and check for any weather alerts in New York state.";
    }

    [McpServerPrompt(Name = "LosAngelesWeather"), Description("Get weather forecast and alerts for Los Angeles")]
    public static string LosAngelesWeather()
    {
        return "Get the weather forecast for Los Angeles (latitude: 34.0522, longitude: -118.2437) and check for any weather alerts in California.";
    }

    [McpServerPrompt, Description("Get weather forecast for a given city outside of US, by searching the internet")]
    public static string CityWeather(string city)
    {
        return $"Get the weather forecast for {city} by searching the internet.";
    }
}
