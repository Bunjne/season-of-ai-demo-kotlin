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
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Tool for allocating an engineer to a project with specified percentage and date range. */
class AllocateEngineerTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition(): Triple<String, String, Tool.Input> =
        Triple(
            "allocate_engineer",
            "Allocate an engineer to a project with specified percentage and date range",
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

    override fun execute(request: CallToolRequest): CallToolResult {
        val arguments = request.arguments

        // Extract required parameters
        val engineerId =
            arguments["engineerId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                json.encodeToString(
                                    mapOf(
                                        "success" to false,
                                        "message" to
                                            "Missing required parameter: engineerId",
                                    ),
                                ),
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
                                json.encodeToString(
                                    mapOf(
                                        "success" to false,
                                        "message" to
                                            "Missing required parameter: projectId",
                                    ),
                                ),
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
                                json.encodeToString(
                                    mapOf(
                                        "success" to false,
                                        "message" to
                                            "Missing required parameter: allocationPercentage",
                                    ),
                                ),
                            ),
                        ),
                    isError = true,
                )

        // Extract optional parameters
        val startDate = arguments["startDate"]?.jsonPrimitive?.content
        val endDate = arguments["endDate"]?.jsonPrimitive?.content

        // Call the service to allocate the engineer
        val result =
            runBlocking {
                allocationService.allocateEngineerAsync(
                    engineerId = engineerId,
                    projectId = projectId,
                    allocationPercentage = allocationPercentage,
                    startDate = startDate,
                    endDate = endDate,
                )
            }

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
