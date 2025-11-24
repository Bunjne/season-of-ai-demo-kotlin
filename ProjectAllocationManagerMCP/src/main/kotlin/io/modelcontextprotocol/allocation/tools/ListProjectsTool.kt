package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject

class ListProjectsTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition(): Triple<String, String, Tool.Input> =
        Triple(
            "list_projects",
            "List all projects in the system with their details including ID, name, description, and status.",
            Tool.Input(
                properties = buildJsonObject {},
                required = emptyList(),
            ),
        )

    override fun execute(request: CallToolRequest): CallToolResult {
        val projects = runBlocking { allocationService.getProjectsAsync() }
        val projectsJson = json.encodeToString(projects)
        return CallToolResult(content = listOf(TextContent(projectsJson)))
    }
}
