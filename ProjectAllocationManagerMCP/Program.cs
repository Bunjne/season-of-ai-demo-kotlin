using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using ProjectAllocationManagerMCP.Services;

var builder = Host.CreateEmptyApplicationBuilder(settings: null);
builder.Services.AddSingleton<AllocationService>();

builder.Services.AddMcpServer()
    .WithStdioServerTransport()
    .WithToolsFromAssembly()
    .WithResourcesFromAssembly()
    .WithPromptsFromAssembly();

var app = builder.Build();

// Verify the singleton instance
var allocationService = app.Services.GetRequiredService<AllocationService>();
await allocationService.LoadDataAsync();

await app.RunAsync();