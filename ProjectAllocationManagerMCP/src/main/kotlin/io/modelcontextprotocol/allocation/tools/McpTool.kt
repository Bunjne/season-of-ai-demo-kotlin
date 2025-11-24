package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool

interface McpTool {
    fun getToolDefinition(): Triple<String, String, Tool.Input>

    fun execute(request: CallToolRequest): CallToolResult
}
