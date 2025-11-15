using ModelContextProtocol.Server;
using System.ComponentModel;
using System.Text.Json;

namespace WeatherMCP;

[McpServerResourceType]
public class WeatherResources
{
    [McpServerResource(UriTemplate = "weather://state-codes")]
    [Description("List of US state codes and names for weather alerts")]
    public static async Task<string> GetStateCodesResource()
    {
        return JsonSerializer.Serialize(new
        {
            description = "US State codes for use with GetAlerts tool",
            states = new[]
            {
                new { code = "AL", name = "Alabama" },
                new { code = "AK", name = "Alaska" },
                new { code = "CA", name = "California" },
                new { code = "NY", name = "New York" },
            }
        });
    }

    [McpServerResource(UriTemplate = "weather://majorcities-coords")]
    [Description("Coordinates for major US cities to use with weather forecast")]
    public static async Task<string> GetMajorCitiesResource()
    {
        return JsonSerializer.Serialize(new
        {
            description = "Pre-defined coordinates for major US cities",
            cities = new[]
            {
                new { name = "New York, NY", latitude = 40.7128, longitude = -74.0060 },
                new { name = "Los Angeles, CA", latitude = 34.0522, longitude = -118.2437 },
                new { name = "Chicago, IL", latitude = 41.8781, longitude = -87.6298 },
                new { name = "Houston, TX", latitude = 29.7604, longitude = -95.3698 }
            }
        });
    }
}
