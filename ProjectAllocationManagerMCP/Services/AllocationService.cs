using ProjectAllocationManagerMCP.Models;
using System.Text.Json;

namespace ProjectAllocationManagerMCP.Services
{
    public class AllocationService
    {
        private readonly List<Engineer> _engineers = new();
        private readonly List<Project> _projects = new();
        private readonly List<Allocation> _allocations = new();
        private readonly string _dataFolder;

        /// <summary>
        /// Initializes a new instance of the AllocationService with an optional custom data folder path.
        /// </summary>
        /// <param name="dataFolder">Optional path to the data folder. If not provided, defaults to 'data' folder in the application base directory.</param>
        public AllocationService(string? dataFolder = null)
        {
            var baseDirectory = AppContext.BaseDirectory;
            _dataFolder = dataFolder ?? Path.Combine(baseDirectory, "data");
        }

        /// <summary>
        /// Retrieves all engineers in the system.
        /// </summary>
        /// <returns>A list of all engineers.</returns>
        public Task<List<Engineer>> GetEngineersAsync()
        {
            return Task.FromResult(_engineers);
        }

        /// <summary>
        /// Retrieves all projects in the system.
        /// </summary>
        /// <returns>A list of all projects.</returns>
        public Task<List<Project>> GetProjectsAsync()
        {
            return Task.FromResult(_projects);
        }

        /// <summary>
        /// Retrieves all allocations in the system.
        /// </summary>
        /// <returns>A list of all allocations.</returns>
        public Task<List<Allocation>> GetAllocationsAsync()
        {
            return Task.FromResult(_allocations);
        }

        /// <summary>
        /// Retrieves an engineer by their unique identifier.
        /// </summary>
        /// <param name="id">The unique identifier of the engineer.</param>
        /// <returns>The engineer if found, otherwise null.</returns>
        public Task<Engineer?> GetEngineerByIdAsync(string id)
        {
            return Task.FromResult(_engineers.FirstOrDefault(e => e.Id == id));
        }

        /// <summary>
        /// Retrieves a project by its unique identifier.
        /// </summary>
        /// <param name="id">The unique identifier of the project.</param>
        /// <returns>The project if found, otherwise null.</returns>
        public Task<Project?> GetProjectByIdAsync(string id)
        {
            return Task.FromResult(_projects.FirstOrDefault(p => p.Id == id));
        }

        /// <summary>
        /// Retrieves an allocation by its unique identifier.
        /// </summary>
        /// <param name="id">The unique identifier of the allocation.</param>
        /// <returns>The allocation if found, otherwise null.</returns>
        public Task<Allocation?> GetAllocationByIdAsync(string id)
        {
            return Task.FromResult(_allocations.FirstOrDefault(a => a.Id == id));
        }

        /// <summary>
        /// Retrieves all allocations for a specific engineer.
        /// </summary>
        /// <param name="engineerId">The unique identifier of the engineer.</param>
        /// <returns>A list of allocations for the specified engineer.</returns>
        public Task<List<Allocation>> GetAllocationsByEngineerIdAsync(string engineerId)
        {
            return Task.FromResult(_allocations.Where(a => a.EngineerId == engineerId).ToList());
        }

        /// <summary>
        /// Retrieves all allocations for a specific project.
        /// </summary>
        /// <param name="projectId">The unique identifier of the project.</param>
        /// <returns>A list of allocations for the specified project.</returns>
        public Task<List<Allocation>> GetAllocationsByProjectIdAsync(string projectId)
        {
            return Task.FromResult(_allocations.Where(a => a.ProjectId == projectId).ToList());
        }

        /// <summary>
        /// Allocates an engineer to a project with specified percentage and date range.
        /// Validates that the engineer and project exist, the allocation percentage is valid (1-100),
        /// and the engineer won't be over-allocated (total allocations don't exceed 100%).
        /// </summary>
        /// <param name="engineerId">The unique identifier of the engineer to allocate.</param>
        /// <param name="projectId">The unique identifier of the project.</param>
        /// <param name="allocationPercentage">The percentage of time allocated (1-100).</param>
        /// <param name="startDate">Optional start date in YYYY-MM-DD format. Defaults to today if not provided.</param>
        /// <param name="endDate">Optional end date in YYYY-MM-DD format. Leave empty for indefinite allocation.</param>
        /// <returns>A tuple containing success status, message, and the created allocation (if successful).</returns>
        public async Task<(bool Success, string Message, Allocation? Allocation)> AllocateEngineerAsync(
            string engineerId,
            string projectId,
            int allocationPercentage,
            string? startDate = null,
            string? endDate = null)
        {
            // Validation 1: Check if engineer exists
            var engineer = await GetEngineerByIdAsync(engineerId);
            if (engineer == null)
            {
                return (false, $"Engineer with ID '{engineerId}' not found.", null);
            }

            // Validation 2: Check if project exists
            var project = await GetProjectByIdAsync(projectId);
            if (project == null)
            {
                return (false, $"Project with ID '{projectId}' not found.", null);
            }

            // Validation 3: Validate allocation percentage (must be between 1 and 100)
            if (allocationPercentage < 1 || allocationPercentage > 100)
            {
                return (false, "Allocation percentage must be between 1 and 100.", null);
            }

            // Validation 4: Validate and set dates
            // Use current date if start date is not provided
            DateTime parsedStartDate;
            if (string.IsNullOrWhiteSpace(startDate))
            {
                parsedStartDate = DateTime.Today;
            }
            else
            {
                if (!DateTime.TryParse(startDate, out parsedStartDate))
                {
                    return (false, $"Invalid start date format: '{startDate}'.", null);
                }
            }

            // End date is optional for indefinite assignments
            DateTime? parsedEndDate = null;
            if (!string.IsNullOrWhiteSpace(endDate))
            {
                if (!DateTime.TryParse(endDate, out DateTime tempEndDate))
                {
                    return (false, $"Invalid end date format: '{endDate}'.", null);
                }

                parsedEndDate = tempEndDate;

                if (parsedEndDate <= parsedStartDate)
                {
                    return (false, "End date must be after start date.", null);
                }
            }

            // Validation 5: Check for overlapping allocations and total percentage
            var existingAllocations = await GetAllocationsByEngineerIdAsync(engineerId);
            var overlappingAllocations = existingAllocations.Where(a =>
            {
                DateTime existingStart = a.StartDate;
                DateTime? existingEnd = a.EndDate;

                // Check if date ranges overlap
                // Case 1: Both have end dates
                if (parsedEndDate.HasValue && existingEnd.HasValue)
                {
                    return parsedStartDate < existingEnd && parsedEndDate > existingStart;
                }
                // Case 2: New allocation is indefinite
                else if (!parsedEndDate.HasValue && existingEnd.HasValue)
                {
                    return parsedStartDate < existingEnd;
                }
                // Case 3: Existing allocation is indefinite
                else if (parsedEndDate.HasValue && !existingEnd.HasValue)
                {
                    return parsedEndDate > existingStart;
                }
                // Case 4: Both are indefinite
                else
                {
                    return true; // Always overlaps
                }
            }).ToList();

            if (overlappingAllocations.Count > 0)
            {
                var totalAllocation = overlappingAllocations.Sum(a => a.AllocationPercentage) + allocationPercentage;
                if (totalAllocation > 100)
                {
                    return (false,
                        $"Engineer '{engineer.Name}' is over-allocated. " +
                        $"Current allocation during this period: {overlappingAllocations.Sum(a => a.AllocationPercentage)}%. " +
                        $"Adding {allocationPercentage}% would result in {totalAllocation}% total allocation.",
                        null);
                }
            }

            // Validation 6: Check if engineer is already allocated to the same project with overlapping dates
            var duplicateAllocation = overlappingAllocations.FirstOrDefault(a => a.ProjectId == projectId);
            if (duplicateAllocation != null)
            {
                var endDateStr = duplicateAllocation.EndDate.HasValue
                    ? duplicateAllocation.EndDate.Value.ToString("yyyy-MM-dd")
                    : "indefinite";
                return (false,
                    $"Engineer '{engineer.Name}' is already allocated to project '{project.Name}' " +
                    $"from {duplicateAllocation.StartDate:yyyy-MM-dd} to {endDateStr}.",
                    null);
            }

            // Create new allocation
            var newAllocation = new Allocation
            {
                Id = $"alloc-{Guid.NewGuid().ToString()[..8]}",
                EngineerId = engineerId,
                ProjectId = projectId,
                AllocationPercentage = allocationPercentage,
                StartDate = parsedStartDate,
                EndDate = parsedEndDate
            };

            _allocations.Add(newAllocation);

            var message = !parsedEndDate.HasValue
                ? $"Successfully allocated {allocationPercentage}% of {engineer.Name} to {project.Name} " +
                  $"starting from {parsedStartDate:yyyy-MM-dd} (indefinite)."
                : $"Successfully allocated {allocationPercentage}% of {engineer.Name} to {project.Name} " +
                  $"from {parsedStartDate:yyyy-MM-dd} to {parsedEndDate.Value:yyyy-MM-dd}.";

            return (true, message, newAllocation);
        }

        /// <summary>
        /// Updates an existing allocation with new percentage and/or date range.
        /// All update parameters are optional - only provided values will be updated.
        /// Validates that the allocation exists, dates are valid, and the engineer won't be over-allocated.
        /// </summary>
        /// <param name="allocationId">The unique identifier of the allocation to update.</param>
        /// <param name="allocationPercentage">Optional new allocation percentage (1-100). If not provided, keeps existing value.</param>
        /// <param name="startDate">Optional new start date in YYYY-MM-DD format. If not provided, keeps existing value.</param>
        /// <param name="endDate">Optional new end date in YYYY-MM-DD format. If not provided, keeps existing value.</param>
        /// <returns>A tuple containing success status, message, and the updated allocation (if successful).</returns>
        public async Task<(bool Success, string Message, Allocation? Allocation)> UpdateAllocationAsync(
            string allocationId,
            int? allocationPercentage = null,
            string? startDate = null,
            string? endDate = null)
        {
            // Validation 1: Find the allocation
            var allocation = await GetAllocationByIdAsync(allocationId);
            if (allocation == null)
            {
                return (false, $"Allocation with ID '{allocationId}' not found.", null);
            }

            // Get engineer and project details for validation and messaging
            var engineer = await GetEngineerByIdAsync(allocation.EngineerId);
            var project = await GetProjectByIdAsync(allocation.ProjectId);

            if (engineer == null || project == null)
            {
                return (false, "Associated engineer or project not found.", null);
            }

            // Parse and validate new dates
            DateTime parsedStartDate = allocation.StartDate;
            DateTime? parsedEndDate = allocation.EndDate;

            if (!string.IsNullOrWhiteSpace(startDate))
            {
                if (!DateTime.TryParse(startDate, out parsedStartDate))
                {
                    return (false, $"Invalid start date format: '{startDate}'.", null);
                }
            }

            if (!string.IsNullOrWhiteSpace(endDate))
            {
                if (!DateTime.TryParse(endDate, out DateTime tempEndDate))
                {
                    return (false, $"Invalid end date format: '{endDate}'.", null);
                }
                parsedEndDate = tempEndDate;
            }

            // Validate end date is after start date
            if (parsedEndDate.HasValue && parsedEndDate <= parsedStartDate)
            {
                return (false, "End date must be after start date.", null);
            }

            // Validate allocation percentage if provided
            int newAllocationPercentage = allocation.AllocationPercentage;
            if (allocationPercentage.HasValue)
            {
                if (allocationPercentage.Value < 1 || allocationPercentage.Value > 100)
                {
                    return (false, "Allocation percentage must be between 1 and 100.", null);
                }
                newAllocationPercentage = allocationPercentage.Value;
            }

            // Check for overlapping allocations (excluding the current allocation being updated)
            var existingAllocations = (await GetAllocationsByEngineerIdAsync(allocation.EngineerId))
                .Where(a => a.Id != allocationId)
                .ToList();

            var overlappingAllocations = existingAllocations.Where(a =>
            {
                DateTime existingStart = a.StartDate;
                DateTime? existingEnd = a.EndDate;

                // Check if date ranges overlap
                if (parsedEndDate.HasValue && existingEnd.HasValue)
                {
                    return parsedStartDate < existingEnd && parsedEndDate > existingStart;
                }
                else if (!parsedEndDate.HasValue && existingEnd.HasValue)
                {
                    return parsedStartDate < existingEnd;
                }
                else if (parsedEndDate.HasValue && !existingEnd.HasValue)
                {
                    return parsedEndDate > existingStart;
                }
                else
                {
                    return true;
                }
            }).ToList();

            if (overlappingAllocations.Count > 0)
            {
                var totalAllocation = overlappingAllocations.Sum(a => a.AllocationPercentage) + newAllocationPercentage;
                if (totalAllocation > 100)
                {
                    return (false,
                        $"Engineer '{engineer.Name}' would be over-allocated. " +
                        $"Current allocation during this period: {overlappingAllocations.Sum(a => a.AllocationPercentage)}%. " +
                        $"Adding {newAllocationPercentage}% would result in {totalAllocation}% total allocation.",
                        null);
                }
            }

            // Check for duplicate allocation to the same project (excluding current allocation)
            var duplicateAllocation = overlappingAllocations.FirstOrDefault(a => a.ProjectId == allocation.ProjectId);
            if (duplicateAllocation != null)
            {
                var endDateStr = duplicateAllocation.EndDate.HasValue
                    ? duplicateAllocation.EndDate.Value.ToString("yyyy-MM-dd")
                    : "indefinite";
                return (false,
                    $"Engineer '{engineer.Name}' is already allocated to project '{project.Name}' " +
                    $"from {duplicateAllocation.StartDate:yyyy-MM-dd} to {endDateStr} in allocation '{duplicateAllocation.Id}'.",
                    null);
            }

            // Update the allocation
            allocation.AllocationPercentage = newAllocationPercentage;
            allocation.StartDate = parsedStartDate;
            allocation.EndDate = parsedEndDate;

            var message = !parsedEndDate.HasValue
                ? $"Successfully updated allocation. {engineer.Name} is now {newAllocationPercentage}% allocated to {project.Name} " +
                  $"starting from {parsedStartDate:yyyy-MM-dd} (indefinite)."
                : $"Successfully updated allocation. {engineer.Name} is now {newAllocationPercentage}% allocated to {project.Name} " +
                  $"from {parsedStartDate:yyyy-MM-dd} to {parsedEndDate.Value:yyyy-MM-dd}.";

            return (true, message, allocation);
        }

        /// <summary>
        /// Loads engineers, projects, and allocations data from JSON files in the data folder.
        /// If the data folder doesn't exist, it will be created. Uses case-insensitive property matching for deserialization.
        /// </summary>
        /// <returns>A task representing the asynchronous load operation.</returns>
        public async Task LoadDataAsync()
        {
            if (!Directory.Exists(_dataFolder))
            {
                Directory.CreateDirectory(_dataFolder);
                return;
            }

            var jsonOptions = new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true
            };

            // Load Engineers
            var engineersPath = Path.Combine(_dataFolder, "engineers.json");
            if (File.Exists(engineersPath))
            {
                var engineersJson = await File.ReadAllTextAsync(engineersPath);
                var engineers = JsonSerializer.Deserialize<List<Engineer>>(engineersJson, jsonOptions);
                if (engineers != null)
                {
                    _engineers.AddRange(engineers);
                }
            }

            // Load Projects
            var projectsPath = Path.Combine(_dataFolder, "projects.json");
            if (File.Exists(projectsPath))
            {
                var projectsJson = await File.ReadAllTextAsync(projectsPath);
                var projects = JsonSerializer.Deserialize<List<Project>>(projectsJson, jsonOptions);
                if (projects != null)
                {
                    _projects.AddRange(projects);
                }
            }

            // Load Allocations
            var allocationsPath = Path.Combine(_dataFolder, "allocations.json");
            if (File.Exists(allocationsPath))
            {
                var allocationsJson = await File.ReadAllTextAsync(allocationsPath);
                var allocations = JsonSerializer.Deserialize<List<Allocation>>(allocationsJson, jsonOptions);
                if (allocations != null)
                {
                    _allocations.AddRange(allocations);
                }
            }
        }
    }
}