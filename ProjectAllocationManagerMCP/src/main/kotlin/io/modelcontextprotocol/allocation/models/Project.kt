package io.modelcontextprotocol.allocation.models

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val status: String = "",
)
