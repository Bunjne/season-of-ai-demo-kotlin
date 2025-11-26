package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Tool for updating an existing allocation with new percentage and/or date range. All parameters
 * except allocationId are optional.
 */
class UpdateAllocationTool(
    private val allocationService: AllocationService,
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
                                buildJsonObject {
                                    put("success", false)
                                    put("message", "Missing required parameter: allocationId")
                                }.toString(),
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
        val responseJson =
            buildJsonObject {
                put("success", result.success)
                put("message", result.message)
                if (result.success && result.allocation != null) {
                    put(
                        "allocation",
                        JsonObject(
                            mapOf(
                                "id" to kotlinx.serialization.json.JsonPrimitive(result.allocation.id),
                                "engineerId" to kotlinx.serialization.json.JsonPrimitive(result.allocation.engineerId),
                                "projectId" to kotlinx.serialization.json.JsonPrimitive(result.allocation.projectId),
                                "allocationPercentage" to kotlinx.serialization.json.JsonPrimitive(result.allocation.allocationPercentage),
                                "startDate" to kotlinx.serialization.json.JsonPrimitive(result.allocation.startDate),
                                "endDate" to
                                    (
                                        result.allocation.endDate?.let { kotlinx.serialization.json.JsonPrimitive(it) }
                                            ?: kotlinx.serialization.json.JsonNull
                                    ),
                            ),
                        ),
                    )
                }
            }

        return CallToolResult(
            content = listOf(TextContent(responseJson.toString())),
            isError = !result.success,
        )
    }
}
