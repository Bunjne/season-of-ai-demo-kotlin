package io.modelcontextprotocol.allocation.models

data class AllocationResult(
    val success: Boolean,
    val message: String,
    val allocation: Allocation?,
)
