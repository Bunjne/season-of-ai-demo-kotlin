package io.modelcontextprotocol.allocation

import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.allocation.services.AllocationLocalService
import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.allocation.tools.AllocateEngineerTool
import io.modelcontextprotocol.allocation.tools.GetAllocationByIdTool
import io.modelcontextprotocol.allocation.tools.GetEngineerByIdTool
import io.modelcontextprotocol.allocation.tools.GetProjectByIdTool
import io.modelcontextprotocol.allocation.tools.ListAllocationsTool
import io.modelcontextprotocol.allocation.tools.ListEngineersTool
import io.modelcontextprotocol.allocation.tools.ListProjectsTool
import io.modelcontextprotocol.allocation.tools.UpdateAllocationTool
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json

/**
 * Starts an MCP server that provides project allocation management tools. Supports listing
 * engineers, projects, allocations, and managing allocations.
 */
suspend fun runMcpServer() {
    // Create the allocation service
    val allocationService: AllocationService = AllocationLocalService()

    // Load data from JSON files
    allocationService.loadDataAsync()

    val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    // Create the MCP Server instance
    val server =
        Server(
            Implementation(
                name = "project-allocation-manager",
                version = "1.0.0",
            ),
            ServerOptions(
                capabilities =
                    ServerCapabilities(
                        tools =
                            ServerCapabilities.Tools(
                                listChanged = true,
                            ),
                        logging = null,
                    ),
            ),
        )

    // Initialize all tools
    val listEngineersTool = ListEngineersTool(allocationService, json)
    val listProjectsTool = ListProjectsTool(allocationService, json)
    val listAllocationsTool = ListAllocationsTool(allocationService, json)
    val allocateEngineerTool = AllocateEngineerTool(allocationService, json)
    val updateAllocationTool = UpdateAllocationTool(allocationService)
    val getEngineerByIdTool = GetEngineerByIdTool(allocationService, json)
    val getProjectByIdTool = GetProjectByIdTool(allocationService, json)
    val getAllocationByIdTool = GetAllocationByIdTool(allocationService, json)

    // Register all tools
    listOf(
        listEngineersTool,
        listProjectsTool,
        listAllocationsTool,
        allocateEngineerTool,
        updateAllocationTool,
        getEngineerByIdTool,
        getProjectByIdTool,
        getAllocationByIdTool,
    ).forEach { tool ->
        val definition = tool.getToolDefinition()
        server.addTool(
            name = definition.name,
            description = definition.description,
            inputSchema = definition.inputSchema,
        ) { request -> tool.execute(request) }
    }

    // Create a transport using standard IO for server communication
    val transport =
        StdioServerTransport(
            System.`in`.asInput(),
            System.out.asSink().buffered(),
        )
    val session = server.connect(transport)
    val done = Job()
    session.onClose { done.complete() }
    done.join()
}
