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
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Tool for retrieving an allocation by its unique identifier. */
class GetAllocationByIdTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition(): Triple<String, String, Tool.Input> =
        Triple(
            "get_allocation_by_id",
            "Get an allocation by its unique identifier",
            Tool.Input(
                properties =
                    buildJsonObject {
                        put(
                            "allocationId",
                            buildJsonObject {
                                put("type", "string")
                                put(
                                    "description",
                                    "The unique identifier of the allocation",
                                )
                            },
                        )
                    },
                required = listOf("allocationId"),
            ),
        )

    override fun execute(request: CallToolRequest): CallToolResult {
        val arguments = request.arguments

        val allocationId =
            arguments["allocationId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                "Missing required parameter: allocationId",
                            ),
                        ),
                    isError = true,
                )

        val allocation = runBlocking { allocationService.getAllocationByIdAsync(allocationId) }

        return if (allocation != null) {
            CallToolResult(content = listOf(TextContent(json.encodeToString(allocation))))
        } else {
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            json.encodeToString(
                                mapOf("error" to "Allocation not found"),
                            ),
                        ),
                    ),
                isError = true,
            )
        }
    }
}
