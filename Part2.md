# Part 2: Building a Project Allocation Manager MCP Server

## Introduction

Imagine asking your AI assistant, "Which engineers with frontend skills are available next month?" and getting real-time answers from your actual project management system - not guesses, but live data from your organization's databases.

That's the power of the Model Context Protocol (MCP). MCP servers act as secure bridges, giving AI assistants direct access to your internal systems - databases, APIs, file systems, and more.

In this workshop, you'll build an MCP server for a project allocation system. While we're using JSON files for simplicity, the same patterns apply to any data source: SQL databases, REST APIs etc...

## Demo Video

[Watch this preview to see the Project Allocation Manager MCP in action](allocation-mcp-preview.mp4)

## Overview

**What's already provided:**
- `AllocationService` - Complete service with all business logic, validation and KDoc documentation
- `Allocation`, `Engineer`, and `Project` models
- Sample data in JSON files in `src/main/resources/asset/`
- Basic tool implementations: `ListEngineersTool`, `ListProjectsTool`

**What you'll build:**
- Tools to get individual records by ID
- Tool to allocate and update allocations
- Resources for commonly used data
- Prompts for common allocation tasks

## Getting Started

### Step 1: Understand the Existing Code

1. **Review the models** in `Kotlin/ProjectAllocationManagerMCP/src/main/kotlin/io/modelcontextprotocol/allocation/models/`
2. **Check the sample data** in `Kotlin/ProjectAllocationManagerMCP/src/main/resources/asset/`
3. **Study the service** in `Kotlin/ProjectAllocationManagerMCP/src/main/kotlin/io/modelcontextprotocol/allocation/services/AllocationService.kt`
   - Read the KDoc documentation on each method
   - Understand the available methods you can use
4. **Examine existing tools** in `Kotlin/ProjectAllocationManagerMCP/src/main/kotlin/io/modelcontextprotocol/allocation/tools/`

### Step 2: Build the project

Build the ProjectAllocationManagerMCP project to ensure everything compiles correctly:

```bash
cd Kotlin/ProjectAllocationManagerMCP
./gradlew build
```

### Step 3: Configure MCP Server

To use your MCP server with AI assistants, configure it in the MCP settings file:

1. Locate your MCP config file (usually at `~/.codeium/windsurf/mcp_config.json` or similar)
2. Add the following configuration:

```json
{
  "mcpServers": {
    "project-allocation-manager": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/SeasonOfAIDemo/Kotlin/ProjectAllocationManagerMCP/build/libs/ProjectAllocationManagerMCP-all.jar"
      ]
    }
  }
}
```

**What this does:**
- Defines an MCP server named "project-allocation-manager"
- Configures the IDE to run your server using the built JAR file
- Points to your ProjectAllocationManagerMCP fat JAR
- Replace `/path/to/` with your actual project path

### Step 4: Test Existing Tools

Test the provided tools :
- Try listing all engineers
- Try listing all projects

## Exercise Time 🚀

### Task 1: Create tool to retrieve all allocations
**Service method to use:** `getAllocations()`

**Example tool structure:**
```kotlin
class GetAllocationsTool(private val allocationService: AllocationService) {
    suspend fun execute(): List<Allocation> {
        return allocationService.getAllocations()
    }
}
```

### Task 2: Create tool to retrieve engineers by id
**Service method to use:** `getEngineerById(id: String)`

### Task 3: Create tool to retrieve projects by id
**Service method to use:** `getProjectById(id: String)`

### Task 4: Create tool to retrieve allocations by id
**Service method to use:** `getAllocationById(id: String)`

### Task 5: Create tool to allocate an engineer
**Service method to use:** `allocateEngineer(engineerId: String, projectId: String, allocationPercentage: Int, startDate: String? = null, endDate: String? = null)`

### Task 6: Create tool to update allocation of an engineer
**Service method to use:** `updateAllocation(allocationId: String, allocationPercentage: Int? = null, startDate: String? = null, endDate: String? = null)`

### Task 7: Add Reference Data Resources

Create resources that provide reference data to AI assistants:

**File to create:**
- `Kotlin/ProjectAllocationManagerMCP/src/main/kotlin/io/modelcontextprotocol/allocation/resources/AllocationResources.kt`

**Resources to implement:**
1. `allocation://engineers` - List all engineers with details
2. `allocation://projects` - List all projects with details

**Example resource structure:**
```kotlin
val engineersResource = Resource(
    uri = "allocation://engineers",
    name = "Engineers List",
    description = "List of all engineers with their skills",
    mimeType = "application/json"
)
```

### Task 8: Add Workflow Prompts

Create prompts that guide users through common tasks:

**File to create:**
- `Kotlin/ProjectAllocationManagerMCP/src/main/kotlin/io/modelcontextprotocol/allocation/prompts/AllocationPrompts.kt`

**Prompts to implement:**
1. **AllocateEngineerPrompt** - Guide users to allocate an engineer accepting name, project and start and end date
2. **MoveEngineerToBenchPrompt** - Guide users to move an engineer to the bench (unallocate from current projects)

**Example prompt structure:**
```kotlin
val allocateEngineerPrompt = Prompt(
    name = "AllocateEngineer",
    description = "Guide to allocate an engineer to a project",
    arguments = listOf(
        Prompt.Argument(
            name = "engineerName",
            description = "Name of the engineer",
            required = true
        ),
        Prompt.Argument(
            name = "projectName",
            description = "Name of the project",
            required = true
        )
    )
)
```

## Summary

By the end of this workshop, you'll have built:
- **6 Tools** for querying and updating allocations
- **2 Resources** providing reference data
- **2 Prompts** guiding common workflows
