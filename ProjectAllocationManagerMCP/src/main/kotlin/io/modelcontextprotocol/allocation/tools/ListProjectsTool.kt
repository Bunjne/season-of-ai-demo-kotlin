package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.Json

class ListProjectsTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "list_projects",
            description = "List all projects in the system with their details including ID, name, description, and status.",
            inputSchema =
                Tool.Input(),
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val projects = allocationService.getProjectsAsync()
        val projectsJson = json.encodeToString(projects)
        return CallToolResult(content = listOf(TextContent(projectsJson)))
    }
}
