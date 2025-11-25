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

/** Tool for retrieving an engineer by their unique identifier. */
class GetEngineerByIdTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "get_engineer_by_id",
            description = "Get an engineer by their unique identifier",
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
                                        "The unique identifier of the engineer",
                                    )
                                },
                            )
                        },
                    required = listOf("engineerId"),
                ),
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val arguments = request.arguments

        val engineerId =
            arguments["engineerId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                "Missing required parameter: engineerId",
                            ),
                        ),
                    isError = true,
                )

        val engineer = allocationService.getEngineerByIdAsync(engineerId)

        return if (engineer != null) {
            CallToolResult(content = listOf(TextContent(json.encodeToString(engineer))))
        } else {
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            buildJsonObject {
                                put("error", "Engineer not found")
                            }.toString(),
                        ),
                    ),
                isError = true,
            )
        }
    }
}
