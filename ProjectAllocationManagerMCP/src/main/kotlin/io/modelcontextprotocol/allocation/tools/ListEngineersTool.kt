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

class ListEngineersTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition(): Triple<String, String, Tool.Input> =
        Triple(
            "list_engineers",
            "List all engineers in the system with their details including ID, name, role, and skills.",
            Tool.Input(
                properties = buildJsonObject {},
                required = emptyList(),
            ),
        )

    override fun execute(request: CallToolRequest): CallToolResult {
        val engineers = runBlocking { allocationService.getEngineersAsync() }
        val engineersJson = json.encodeToString(engineers)
        return CallToolResult(content = listOf(TextContent(engineersJson)))
    }
}
