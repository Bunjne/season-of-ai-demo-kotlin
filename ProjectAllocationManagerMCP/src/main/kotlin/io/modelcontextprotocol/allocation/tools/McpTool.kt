package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool

data class ToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: Tool.Input,
)

interface McpTool {
    fun getToolDefinition(): Triple<String, String, Tool.Input>
    fun getToolDefinition(): ToolDefinition

    suspend fun execute(request: CallToolRequest): CallToolResult
}
