using ModelContextProtocol.Server;
using ProjectAllocationManagerMCP.Models;
using ProjectAllocationManagerMCP.Services;
using System.ComponentModel;

namespace ProjectAllocationManagerMCP.Tools
{
    [McpServerToolType]
    public class ListEngineersTool
    {
        [McpServerTool, Description("List all engineers in the system")]
        public static async Task<List<Engineer>> ListEngineers(AllocationService allocationService)
        {
            return await allocationService.GetEngineersAsync();
        }
    }
}
