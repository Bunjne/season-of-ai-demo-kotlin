using System.Text.Json;

namespace WeatherMCP;

public class WeatherForecastService
{
    private readonly HttpClient _httpClient;

    public WeatherForecastService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<string> GetAlerts(string state)
    {
        try
        {
            var response = await _httpClient.GetAsync($"https://api.weather.gov/alerts/active?area={state}");
            response.EnsureSuccessStatusCode();
            var content = await response.Content.ReadAsStringAsync();
            
            using var doc = JsonDocument.Parse(content);
            var features = doc.RootElement.GetProperty("features");
            
            if (features.GetArrayLength() == 0)
            {
                return $"No active weather alerts for {state}";
            }

            var alerts = new System.Text.StringBuilder();
            alerts.AppendLine($"Active weather alerts for {state}:");
            
            foreach (var feature in features.EnumerateArray())
            {
                var properties = feature.GetProperty("properties");
                var eventName = properties.GetProperty("event").GetString();
                var headline = properties.GetProperty("headline").GetString();
                alerts.AppendLine($"- {eventName}: {headline}");
            }
            
            return alerts.ToString();
        }
        catch (Exception ex)
        {
            return $"Error retrieving alerts: {ex.Message}. Try using a valid US state code (e.g., CA, NY, TX).";
        }
    }

    public async Task<string> GetForecast(double latitude, double longitude)
    {
        try
        {
            var pointResponse = await _httpClient.GetAsync($"https://api.weather.gov/points/{latitude},{longitude}");
            pointResponse.EnsureSuccessStatusCode();
            var pointContent = await pointResponse.Content.ReadAsStringAsync();
            
            using var pointDoc = JsonDocument.Parse(pointContent);
            var forecastUrl = pointDoc.RootElement
                .GetProperty("properties")
                .GetProperty("forecast")
                .GetString();

            var forecastResponse = await _httpClient.GetAsync(forecastUrl);
            forecastResponse.EnsureSuccessStatusCode();
            var forecastContent = await forecastResponse.Content.ReadAsStringAsync();
            
            using var forecastDoc = JsonDocument.Parse(forecastContent);
            var periods = forecastDoc.RootElement
                .GetProperty("properties")
                .GetProperty("periods");

            var forecast = new System.Text.StringBuilder();
            forecast.AppendLine($"Weather forecast for {latitude}, {longitude}:");
            
            foreach (var period in periods.EnumerateArray().Take(3))
            {
                var name = period.GetProperty("name").GetString();
                var temperature = period.GetProperty("temperature").GetInt32();
                var temperatureUnit = period.GetProperty("temperatureUnit").GetString();
                var shortForecast = period.GetProperty("shortForecast").GetString();
                
                forecast.AppendLine($"{name}: {temperature}°{temperatureUnit}, {shortForecast}");
            }
            
            return forecast.ToString();
        }
        catch (Exception ex)
        {
            return $"Error retrieving forecast: {ex.Message}";
        }
    }

    public static async Task<JsonDocument> ReadJsonDocumentAsync(HttpClient client, string requestUri)
    {
        using var response = await client.GetAsync(requestUri);
        response.EnsureSuccessStatusCode();
        return await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
    }
}