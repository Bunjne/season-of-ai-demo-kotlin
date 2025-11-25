package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Tool for retrieving an allocation by its unique identifier. */
class GetAllocationByIdTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "get_allocation_by_id",
            description = "Get an allocation by its unique identifier",
            inputSchema =
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

    override suspend fun execute(request: CallToolRequest): CallToolResult {
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

        val allocation = allocationService.getAllocationByIdAsync(allocationId)

        return if (allocation != null) {
            CallToolResult(content = listOf(TextContent(json.encodeToString(allocation))))
        } else {
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            buildJsonObject {
                                put("error", "Allocation not found")
                            }.toString(),
                        ),
                    ),
                isError = true,
            )
        }
    }
}
