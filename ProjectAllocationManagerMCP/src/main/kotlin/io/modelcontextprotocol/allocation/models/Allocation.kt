package io.modelcontextprotocol.allocation.models

import kotlinx.serialization.Serializable

@Serializable
data class Allocation(
    val id: String = "",
    val engineerId: String = "",
    val projectId: String = "",
    val allocationPercentage: Int = 0,
    val startDate: String = "",
    val endDate: String? = null,
)
