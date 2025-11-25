package io.modelcontextprotocol.allocation.tools

import io.modelcontextprotocol.allocation.services.AllocationService
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Tool for allocating an engineer to a project with specified percentage and date range. */
class AllocateEngineerTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "allocate_engineer",
            description = "Allocate an engineer to a project with specified percentage and date range",
            inputSchema =
                Tool.Input(
                    properties =
                        buildJsonObject {
                            put(
                                "engineerId",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "The unique identifier of the engineer to allocate",
                                    )
                                },
                            )
                            put(
                                "projectId",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "The unique identifier of the project to allocate to",
                                    )
                                },
                            )
                            put(
                                "allocationPercentage",
                                buildJsonObject {
                                    put("type", "integer")
                                    put(
                                        "description",
                                        "Percentage of engineer's time to allocate (1-100)",
                                    )
                                },
                            )
                            put(
                                "startDate",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "Start date in YYYY-MM-DD format (optional, defaults to today)",
                                    )
                                },
                            )
                            put(
                                "endDate",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "End date in YYYY-MM-DD format (optional, leave empty for indefinite)",
                                    )
                                },
                            )
                        },
                    required = listOf("engineerId", "projectId", "allocationPercentage"),
                ),
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val arguments = request.arguments

        // Extract required parameters
        val engineerId =
            arguments["engineerId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                buildJsonObject {
                                    put("success", false)
                                    put("message", "Missing required parameter: engineerId")
                                }.toString(),
                            ),
                        ),
                    isError = true,
                )

        val projectId =
            arguments["projectId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                buildJsonObject {
                                    put("success", false)
                                    put("message", "Missing required parameter: projectId")
                                }.toString(),
                            ),
                        ),
                    isError = true,
                )

        val allocationPercentage =
            arguments["allocationPercentage"]?.jsonPrimitive?.int
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                buildJsonObject {
                                    put("success", false)
                                    put("message", "Missing required parameter: allocationPercentage")
                                }.toString(),
                            ),
                        ),
                    isError = true,
                )

        // Extract optional parameters
        val startDate = arguments["startDate"]?.jsonPrimitive?.content
        val endDate = arguments["endDate"]?.jsonPrimitive?.content

        // Call the service to allocate the engineer
        val result =
            allocationService.allocateEngineerAsync(
                engineerId = engineerId,
                projectId = projectId,
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
