using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System.Net.Http.Headers;
using WeatherMCP;

var builder = Host.CreateEmptyApplicationBuilder(settings: null);

builder.Services.AddSingleton<WeatherForecastService>();
builder.Services.AddSingleton(_ =>
{
    var client = new HttpClient() { BaseAddress = new Uri("https://api.weather.gov") };
    client.DefaultRequestHeaders.UserAgent.Add(new ProductInfoHeaderValue("weather-tool", "1.0"));
    return client;
});

// Register MCP server and tools
builder.Services.AddMcpServer()
    .WithStdioServerTransport()
    .WithToolsFromAssembly()
    .WithResourcesFromAssembly()
    .WithPromptsFromAssembly();
    
var app = builder.Build();

await app.RunAsync();