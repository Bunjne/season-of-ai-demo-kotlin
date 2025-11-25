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
import java.time.LocalDate

/**
 * Tool for listing all active allocations in the system (allocations that are currently ongoing).
 */
class ListAllocationsTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition(): Triple<String, String, Tool.Input> =
        Triple(
            "list_allocations",
            "List all active allocations in the system (allocations that are currently ongoing)",
            Tool.Input(
                properties = buildJsonObject {},
                required = emptyList(),
            ),
        )

    override fun execute(request: CallToolRequest): CallToolResult {
        val allAllocations = runBlocking { allocationService.getAllocationsAsync() }
        val today = LocalDate.now()

        // Filter to only active allocations (where today is between start and end dates)
        val activeAllocations =
            allAllocations.filter { allocation ->
                val startDate = LocalDate.parse(allocation.startDate)

                // Check if allocation has started
                if (startDate > today) {
                    return@filter false
                }

                // Check if allocation has ended (null endDate means indefinite)
                if (allocation.endDate != null) {
                    val endDate = LocalDate.parse(allocation.endDate)
                    if (endDate < today) {
                        return@filter false
                    }
                }

                true
            }

        val allocationsJson = json.encodeToString(activeAllocations)
        return CallToolResult(content = listOf(TextContent(allocationsJson)))
    }
}
