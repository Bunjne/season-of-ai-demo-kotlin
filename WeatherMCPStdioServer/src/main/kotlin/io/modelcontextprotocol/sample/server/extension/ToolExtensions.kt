package io.modelcontextprotocol.sample.server.extension

import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Extension functions for cleaner tool schema creation and parameter extraction.
 */

/**
 * Creates a string parameter schema for tool input.
 */
fun buildStringParameter(description: String): JsonElement = buildJsonObject {
    put("type", "string")
    put("description", description)
}

/**
 * Creates a number parameter schema for tool input.
 */
fun buildNumberParameter(description: String): JsonElement = buildJsonObject {
    put("type", "number")
    put("description", description)
}

/**
 * Creates a tool input schema with the given properties and required fields.
 */
fun toolInput(
    vararg properties: Pair<String, JsonElement>,
    required: List<String> = emptyList()
): Tool.Input = Tool.Input(
    properties = buildJsonObject {
        properties.forEach { (key, value) ->
            put(key, value)
        }
    },
    required = required
)

/**
 * Safely extracts a string parameter from tool request arguments.
 */
fun Map<String, JsonElement>.getStringOrNull(key: String): String? =
    this[key]?.jsonPrimitive?.content

/**
 * Safely extracts a double parameter from tool request arguments.
 */
fun Map<String, JsonElement>.getDoubleOrNull(key: String): Double? =
    this[key]?.jsonPrimitive?.doubleOrNull
