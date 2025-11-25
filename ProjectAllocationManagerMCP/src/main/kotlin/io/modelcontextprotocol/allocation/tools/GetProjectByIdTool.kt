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

/** Tool for retrieving a project by its unique identifier. */
class GetProjectByIdTool(
    private val allocationService: AllocationService,
    private val json: Json,
) : McpTool {
    override fun getToolDefinition() =
        ToolDefinition(
            name = "get_project_by_id",
            description = "Get a project by its unique identifier",
            inputSchema =
                Tool.Input(
                    properties =
                        buildJsonObject {
                            put(
                                "projectId",
                                buildJsonObject {
                                    put("type", "string")
                                    put(
                                        "description",
                                        "The unique identifier of the project",
                                    )
                                },
                            )
                        },
                    required = listOf("projectId"),
                ),
        )

    override suspend fun execute(request: CallToolRequest): CallToolResult {
        val arguments = request.arguments

        val projectId =
            arguments["projectId"]?.jsonPrimitive?.content
                ?: return CallToolResult(
                    content =
                        listOf(
                            TextContent("Missing required parameter: projectId"),
                        ),
                    isError = true,
                )

        val project = allocationService.getProjectByIdAsync(projectId)

        return if (project != null) {
            CallToolResult(content = listOf(TextContent(json.encodeToString(project))))
        } else {
            CallToolResult(
                content =
                    listOf(
                        TextContent(
                            json.encodeToString(
                                mapOf("error" to "Project not found"),
                            ),
                        ),
                    ),
                isError = true,
            )
        }
    }
}
