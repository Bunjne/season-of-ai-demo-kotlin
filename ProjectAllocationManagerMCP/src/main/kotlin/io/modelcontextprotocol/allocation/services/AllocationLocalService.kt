package io.modelcontextprotocol.allocation.services

import io.modelcontextprotocol.allocation.models.Allocation
import io.modelcontextprotocol.allocation.models.AllocationResult
import io.modelcontextprotocol.allocation.models.Engineer
import io.modelcontextprotocol.allocation.models.Project
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

/**
 * Service for managing engineer allocations to projects. Handles CRUD operations and business logic
 * for engineers, projects, and their allocations.
 */
class AllocationLocalService(
    dataFolder: String? = null,
) : AllocationService {
    private val engineers = mutableListOf<Engineer>()
    private val projects = mutableListOf<Project>()
    private val allocations = mutableListOf<Allocation>()
    private val dataFolder: String

    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    init {
        // Resolve data folder relative to the compiled classes/jar location to avoid
        // dependence on the current working directory.
        val defaultDataFolder = "data/"

        this.dataFolder = dataFolder ?: defaultDataFolder
    }

    /** Retrieves all engineers in the system. */
    override suspend fun getEngineersAsync(): List<Engineer> = engineers.toList()

    /** Retrieves all projects in the system. */
    override suspend fun getProjectsAsync(): List<Project> = projects.toList()

    /** Retrieves all allocations in the system. */
    override suspend fun getAllocationsAsync(): List<Allocation> = allocations.toList()

    /** Retrieves an engineer by their unique identifier. */
    override suspend fun getEngineerByIdAsync(id: String): Engineer? = engineers.firstOrNull { it.id == id }

    /** Retrieves a project by its unique identifier. */
    override suspend fun getProjectByIdAsync(id: String): Project? = projects.firstOrNull { it.id == id }

    /** Retrieves an allocation by its unique identifier. */
    override suspend fun getAllocationByIdAsync(id: String): Allocation? = allocations.firstOrNull { it.id == id }

    /** Retrieves all allocations for a specific engineer. */
    override suspend fun getAllocationsByEngineerIdAsync(engineerId: String): List<Allocation> =
        allocations.filter {
            it.engineerId ==
                engineerId
        }

    /** Retrieves all allocations for a specific project. */
    override suspend fun getAllocationsByProjectIdAsync(projectId: String): List<Allocation> =
        allocations.filter {
            it.projectId ==
                projectId
        }

    /**
     * Allocates an engineer to a project with specified percentage and date range. Validates that
     * the engineer and project exist, the allocation percentage is valid (1-100), and the engineer
     * won't be over-allocated (total allocations don't exceed 100%).
     */
    override suspend fun allocateEngineerAsync(
        engineerId: String,
        projectId: String,
        allocationPercentage: Int,
        startDate: String?,
        endDate: String?,
    ): AllocationResult {
        // Validation 1: Check if engineer exists
        val engineer =
            getEngineerByIdAsync(engineerId)
                ?: return AllocationResult(
                    false,
                    "Engineer with ID '$engineerId' not found.",
                    null,
                )

        // Validation 2: Check if project exists
        val project =
            getProjectByIdAsync(projectId)
                ?: return AllocationResult(
                    false,
                    "Project with ID '$projectId' not found.",
                    null,
                )

        // Validation 3: Validate allocation percentage (must be between 1 and 100)
        if (allocationPercentage !in 1..100) {
            return AllocationResult(false, "Allocation percentage must be between 1 and 100.", null)
        }

        // Validation 4: Validate and set dates
        val parsedStartDate =
            if (startDate.isNullOrBlank()) {
                LocalDate.now().toString()
            } else {
                try {
                    LocalDate.parse(startDate)
                    startDate
                } catch (e: DateTimeParseException) {
                    return AllocationResult(
                        false,
                        "Invalid start date format: '$startDate'.",
                        null,
                    )
                }
            }

        val parsedEndDate =
            if (!endDate.isNullOrBlank()) {
                try {
                    val tempEndDate = LocalDate.parse(endDate)
                    val startDateObj = LocalDate.parse(parsedStartDate)
                    if (tempEndDate <= startDateObj) {
                        return AllocationResult(
                            false,
                            "End date must be after start date.",
                            null,
                        )
                    }
                    endDate
                } catch (e: DateTimeParseException) {
                    return AllocationResult(false, "Invalid end date format: '$endDate'.", null)
                }
            } else {
                null
            }

        // Validation 5: Check for overlapping allocations and total percentage
        val existingAllocations = getAllocationsByEngineerIdAsync(engineerId)
        val overlappingAllocations =
            existingAllocations.filter { allocation ->
                checkDateOverlap(
                    parsedStartDate,
                    parsedEndDate,
                    allocation.startDate,
                    allocation.endDate,
                )
            }

        if (overlappingAllocations.isNotEmpty()) {
            val totalAllocation =
                overlappingAllocations.sumOf { it.allocationPercentage } + allocationPercentage
            if (totalAllocation > 100) {
                return AllocationResult(
                    false,
                    "Engineer '${engineer.name}' is over-allocated. " +
                        "Current allocation during this period: ${overlappingAllocations.sumOf { it.allocationPercentage }}%. " +
                        "Adding $allocationPercentage% would result in $totalAllocation% total allocation.",
                    null,
                )
            }
        }

        // Validation 6: Check if engineer is already allocated to the same project with overlapping
        // dates
        val duplicateAllocation = overlappingAllocations.firstOrNull { it.projectId == projectId }
        if (duplicateAllocation != null) {
            val endDateStr = duplicateAllocation.endDate ?: "indefinite"
            return AllocationResult(
                false,
                "Engineer '${engineer.name}' is already allocated to project '${project.name}' " +
                    "from ${duplicateAllocation.startDate} to $endDateStr.",
                null,
            )
        }

        // Create new allocation
        val newAllocation =
            Allocation(
                id = "alloc-${UUID.randomUUID().toString().take(8)}",
                engineerId = engineerId,
                projectId = projectId,
                allocationPercentage = allocationPercentage,
                startDate = parsedStartDate,
                endDate = parsedEndDate,
            )

        allocations.add(newAllocation)

        val message =
            if (parsedEndDate == null) {
                "Successfully allocated $allocationPercentage% of ${engineer.name} to ${project.name} " +
                    "starting from $parsedStartDate (indefinite)."
            } else {
                "Successfully allocated $allocationPercentage% of ${engineer.name} to ${project.name} " +
                    "from $parsedStartDate to $parsedEndDate."
            }

        return AllocationResult(true, message, newAllocation)
    }

    /**
     * Updates an existing allocation with new percentage and/or date range. All update parameters
     * are optional - only provided values will be updated.
     */
    override suspend fun updateAllocationAsync(
        allocationId: String,
        allocationPercentage: Int?,
        startDate: String?,
        endDate: String?,
    ): AllocationResult {
        // Validation 1: Find the allocation
        val allocation =
            getAllocationByIdAsync(allocationId)
                ?: return AllocationResult(
                    false,
                    "Allocation with ID '$allocationId' not found.",
                    null,
                )

        // Get engineer and project details
        val engineer = getEngineerByIdAsync(allocation.engineerId)
        val project = getProjectByIdAsync(allocation.projectId)

        if (engineer == null || project == null) {
            return AllocationResult(false, "Associated engineer or project not found.", null)
        }

        // Parse and validate new dates
        var parsedStartDate = allocation.startDate
        var parsedEndDate = allocation.endDate

        if (!startDate.isNullOrBlank()) {
            try {
                LocalDate.parse(startDate)
                parsedStartDate = startDate
            } catch (e: DateTimeParseException) {
                return AllocationResult(false, "Invalid start date format: '$startDate'.", null)
            }
        }

        if (!endDate.isNullOrBlank()) {
            try {
                LocalDate.parse(endDate)
                parsedEndDate = endDate
            } catch (e: DateTimeParseException) {
                return AllocationResult(false, "Invalid end date format: '$endDate'.", null)
            }
        }

        // Validate end date is after start date
        if (parsedEndDate != null) {
            val startDateObj = LocalDate.parse(parsedStartDate)
            val endDateObj = LocalDate.parse(parsedEndDate)
            if (endDateObj <= startDateObj) {
                return AllocationResult(false, "End date must be after start date.", null)
            }
        }

        // Validate allocation percentage if provided
        var newAllocationPercentage = allocation.allocationPercentage
        if (allocationPercentage != null) {
            if (allocationPercentage !in 1..100) {
                return AllocationResult(
                    false,
                    "Allocation percentage must be between 1 and 100.",
                    null,
                )
            }
            newAllocationPercentage = allocationPercentage
        }

        // Check for overlapping allocations (excluding the current allocation being updated)
        val existingAllocations =
            getAllocationsByEngineerIdAsync(allocation.engineerId).filter {
                it.id != allocationId
            }

        val overlappingAllocations =
            existingAllocations.filter { a ->
                checkDateOverlap(parsedStartDate, parsedEndDate, a.startDate, a.endDate)
            }

        if (overlappingAllocations.isNotEmpty()) {
            val totalAllocation =
                overlappingAllocations.sumOf { it.allocationPercentage } +
                    newAllocationPercentage
            if (totalAllocation > 100) {
                return AllocationResult(
                    false,
                    "Engineer '${engineer.name}' would be over-allocated. " +
                        "Current allocation during this period: ${overlappingAllocations.sumOf { it.allocationPercentage }}%. " +
                        "Adding $newAllocationPercentage% would result in $totalAllocation% total allocation.",
                    null,
                )
            }
        }

        // Check for duplicate allocation to the same project (excluding current allocation)
        val duplicateAllocation =
            overlappingAllocations.firstOrNull { it.projectId == allocation.projectId }
        if (duplicateAllocation != null) {
            val endDateStr = duplicateAllocation.endDate ?: "indefinite"
            return AllocationResult(
                false,
                "Engineer '${engineer.name}' is already allocated to project '${project.name}' " +
                    "from ${duplicateAllocation.startDate} to $endDateStr in allocation '${duplicateAllocation.id}'.",
                null,
            )
        }

        // Update the allocation
        val index = allocations.indexOfFirst { it.id == allocationId }
        if (index != -1) {
            allocations[index] =
                allocation.copy(
                    allocationPercentage = newAllocationPercentage,
                    startDate = parsedStartDate,
                    endDate = parsedEndDate,
                )
        }

        val updatedAllocation = allocations[index]
        val message =
            if (parsedEndDate == null) {
                "Successfully updated allocation. ${engineer.name} is now $newAllocationPercentage% allocated to ${project.name} " +
                    "starting from $parsedStartDate (indefinite)."
            } else {
                "Successfully updated allocation. ${engineer.name} is now $newAllocationPercentage% allocated to ${project.name} " +
                    "from $parsedStartDate to $parsedEndDate."
            }

        return AllocationResult(true, message, updatedAllocation)
    }

    /** Loads engineers, projects, and allocations data from JSON files in the data folder. */
    override suspend fun loadDataAsync() {
        // val dataDir = File(dataFolder)
        // if (!dataDir.exists()) {
        //     return
        // }

        val engineersJson =
            object {}.javaClass.getResource("/asset/engineers.json")?.readText()
                ?: throw IllegalArgumentException("File not found in resources")
        val loadedEngineers = json.decodeFromString<List<Engineer>>(engineersJson)
        engineers.addAll(loadedEngineers)

        // Load Projects
        val projectsJson =
            object {}.javaClass.getResource("/asset/projects.json")?.readText()
                ?: throw IllegalArgumentException("File not found in resources")
        val loadedProjects = json.decodeFromString<List<Project>>(projectsJson)
        projects.addAll(loadedProjects)

        // Load Allocations
        val allocationsJson =
            object {}.javaClass.getResource("/asset/allocations.json")?.readText()
                ?: throw IllegalArgumentException("File not found in resources")
        val loadedAllocations = json.decodeFromString<List<Allocation>>(allocationsJson)
        allocations.addAll(loadedAllocations)
    }

    /** Checks if two date ranges overlap. */
    private fun checkDateOverlap(
        start1: String,
        end1: String?,
        start2: String,
        end2: String?,
    ): Boolean {
        val startDate1 = LocalDate.parse(start1)
        val endDate1 = end1?.let { LocalDate.parse(it) }
        val startDate2 = LocalDate.parse(start2)
        val endDate2 = end2?.let { LocalDate.parse(it) }

        return when {
            // Both have end dates
            endDate1 != null && endDate2 != null -> startDate1 < endDate2 && endDate1 > startDate2

            // First is indefinite
            endDate1 == null && endDate2 != null -> startDate1 < endDate2

            // Second is indefinite
            endDate1 != null && endDate2 == null -> endDate1 > startDate2

            // Both are indefinite
            else -> true
        }
    }
}
