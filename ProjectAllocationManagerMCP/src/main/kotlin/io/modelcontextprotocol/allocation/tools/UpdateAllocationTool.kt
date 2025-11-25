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

/**
 * Tool for updating an existing allocation with new percentage and/or date range. All parameters
 * except allocationId are optional.
 */
class UpdateAllocationTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "update_allocation",
            description =
                "Update an existing allocation with new percentage and/or date range. " +
                    "All parameters except allocationId are optional.",
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
                                        "The unique identifier of the allocation to update",
                                    )
                                },
                            )
                            put(
                                "allocationPercentage",
                                buildJsonObject {
                                    put("type", "integer")
                                    put(
                                        "description",
                                        "Optional new allocation percentage (1-100). Leave empty to keep existing value.",
                                    )
                                },
                            )
                            put(
                                "startDate",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "Optional new start date in YYYY-MM-DD format. Leave empty to keep existing value.",
                                    )
                                },
                            )
                            put(
                                "endDate",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "Optional new end date in YYYY-MM-DD format. Leave empty to keep existing value.",
                                    )
                                },
                            )
                        },
                    required = listOf("allocationId"),
                ),
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val arguments = request.arguments

        // Extract required parameter
        val allocationId =
            arguments["allocationId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                json.encodeToString(
                                    mapOf(
                                        "success" to false,
                                        "message" to
                                            "Missing required parameter: allocationId",
                                    ),
                                ),
                            ),
                        ),
                    isError = true,
                )

        // Extract optional parameters
        val allocationPercentage =
            arguments["allocationPercentage"]?.jsonPrimitive?.content?.toIntOrNull()
        val startDate = arguments["startDate"]?.jsonPrimitive?.content
        val endDate = arguments["endDate"]?.jsonPrimitive?.content

        // Call the service to update the allocation
        val result =
            allocationService.updateAllocationAsync(
                allocationId = allocationId,
                allocationPercentage = allocationPercentage,
                startDate = startDate,
                endDate = endDate,
            )

        // Build response
        val response =
            if (result.success) {
                mapOf(
                    "success" to true,
                    "message" to result.message,
                    "allocation" to result.allocation,
                )
            } else {
                mapOf(
                    "success" to false,
                    "message" to result.message,
                )
            }

        return CallToolResult(
            content = listOf(TextContent(json.encodeToString(response))),
            isError = !result.success,
        )
    }
}
