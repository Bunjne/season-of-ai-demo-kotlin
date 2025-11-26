package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.Json

class ListEngineersTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "list_engineers",
            description = "List all engineers in the system with their details including ID, name, role, and skills.",
            inputSchema = Tool.Input(),
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val engineers = allocationService.getEngineersAsync()
        val engineersJson = json.encodeToString(engineers)
        return CallToolResult(content = listOf(TextContent(engineersJson)))
    }
}
