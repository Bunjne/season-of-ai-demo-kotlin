package io.modelcontextprotocol.sample.server.extension

import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Extension functions for cleaner resource data building.
 */

/**
 * Adds a JSON object with the given properties to a JSON array.
 */
fun JsonArrayBuilder.addObject(vararg properties: Pair<String, Any>) {
    add(buildJsonObject {
        properties.forEach { (key, value) ->
            when (value) {
                is String -> put(key, value)
                is Number -> put(key, value)
                is Boolean -> put(key, value)
                else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
            }
        }
    })
}
