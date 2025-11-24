package io.modelcontextprotocol.allocation

import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.allocation.tools.ListEngineersTool
import io.modelcontextprotocol.allocation.tools.ListProjectsTool
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json

/**
 * Starts an MCP server that provides project allocation management tools. Supports listing
 * engineers, projects, allocations, and managing allocations.
 */
fun runMcpServer() {
    // Create the allocation service
    val allocationService = AllocationService()

    // Load data from JSON files
    runBlocking { allocationService.loadDataAsync() }

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
                    ),
            ),
        )

    // Initialize all tools
    val listEngineersTool = ListEngineersTool(allocationService, json)
    val listProjectsTool = ListProjectsTool(allocationService, json)

    // Register all tools
    listOf(
        listEngineersTool,
        listProjectsTool,
    ).forEach { tool ->
        val (name, description, inputSchema) = tool.getToolDefinition()
        server.addTool(
            name = name,
            description = description,
            inputSchema = inputSchema,
        ) { request -> tool.execute(request) }
    }

    // Create a transport using standard IO for server communication
    val transport =
        StdioServerTransport(
            System.`in`.asInput(),
            System.out.asSink().buffered(),
        )

    runBlocking {
        val session = server.connect(transport)
        val done = Job()
        session.onClose { done.complete() }
        done.join()
    }
}
