package io.modelcontextprotocol.allocation.models

import kotlinx.serialization.Serializable

@Serializable
data class Engineer(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val skills: List<String> = emptyList(),
)
