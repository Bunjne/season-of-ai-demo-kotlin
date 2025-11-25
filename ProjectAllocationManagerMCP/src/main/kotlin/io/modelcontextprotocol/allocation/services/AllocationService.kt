package io.modelcontextprotocol.allocation.services

import io.modelcontextprotocol.allocation.models.Allocation
import io.modelcontextprotocol.allocation.models.AllocationResult
import io.modelcontextprotocol.allocation.models.Engineer
import io.modelcontextprotocol.allocation.models.Project

/**
 * Interface for managing engineer allocations to projects.
 * Defines operations for CRUD and business logic for engineers, projects, and their allocations.
 */
interface AllocationService {
    /** Retrieves all engineers in the system. */
    suspend fun getEngineersAsync(): List<Engineer>

    /** Retrieves all projects in the system. */
    suspend fun getProjectsAsync(): List<Project>

    /** Retrieves all allocations in the system. */
    suspend fun getAllocationsAsync(): List<Allocation>

    /** Retrieves an engineer by their unique identifier. */
    suspend fun getEngineerByIdAsync(id: String): Engineer?

    /** Retrieves a project by its unique identifier. */
    suspend fun getProjectByIdAsync(id: String): Project?

    /** Retrieves an allocation by its unique identifier. */
    suspend fun getAllocationByIdAsync(id: String): Allocation?

    /** Retrieves all allocations for a specific engineer. */
    suspend fun getAllocationsByEngineerIdAsync(engineerId: String): List<Allocation>

    /** Retrieves all allocations for a specific project. */
    suspend fun getAllocationsByProjectIdAsync(projectId: String): List<Allocation>

    /**
     * Allocates an engineer to a project with specified percentage and date range.
     * Validates that the engineer and project exist, the allocation percentage is valid (1-100),
     * and the engineer won't be over-allocated (total allocations don't exceed 100%).
     */
    suspend fun allocateEngineerAsync(
        engineerId: String,
        projectId: String,
        allocationPercentage: Int,
        startDate: String? = null,
        endDate: String? = null,
    ): AllocationResult

    /**
     * Updates an existing allocation with new percentage and/or date range.
     * All update parameters are optional - only provided values will be updated.
     */
    suspend fun updateAllocationAsync(
        allocationId: String,
        allocationPercentage: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
    ): AllocationResult

    /** Loads engineers, projects, and allocations data from JSON files in the data folder. */
    suspend fun loadDataAsync()
}
