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

/** Tool for retrieving an engineer by their unique identifier. */
class GetEngineerByIdTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition(): Triple<String, String, Tool.Input> =
        Triple(
            "get_engineer_by_id",
            "Get an engineer by their unique identifier",
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

    override fun execute(request: CallToolRequest): CallToolResult {
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

        val engineer = runBlocking { allocationService.getEngineerByIdAsync(engineerId) }

        return if (engineer != null) {
            CallToolResult(content = listOf(TextContent(json.encodeToString(engineer))))
        } else {
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            json.encodeToString(
                                mapOf("error" to "Engineer not found"),
                            ),
                        ),
                    ),
                isError = true,
            )
        }
    }
}
