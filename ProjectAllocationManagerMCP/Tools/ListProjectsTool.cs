using ModelContextProtocol.Server;
using ProjectAllocationManagerMCP.Models;
using ProjectAllocationManagerMCP.Services;
using System.ComponentModel;

namespace ProjectAllocationManagerMCP.Tools
{
    [McpServerToolType]
    public class ListProjectsTool
    {
        [McpServerTool, Description("List all projects in the system")]
        public static async Task<List<Project>> ListProjects(AllocationService allocationService)
        {
            return await allocationService.GetProjectsAsync();
        }
    }
}
