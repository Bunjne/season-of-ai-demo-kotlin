# Project Allocation Manager MCP Server (Kotlin)

A Model Context Protocol (MCP) server for managing engineer allocations to projects. This is a Kotlin conversion of the C# ProjectAllocationManagerMCP project.

## Overview

This MCP server provides tools to manage engineers, projects, and their allocations. It allows you to:

- List all engineers, projects, and allocations
- Get detailed information about specific engineers and projects
- Allocate engineers to projects with percentage-based allocation
- Update existing allocations
- Validate allocation constraints (no over-allocation, date overlap validation)

## Features

- **Engineer Management**: Track engineers with their roles and skills
- **Project Management**: Manage projects with descriptions and status
- **Allocation Management**: 
  - Percentage-based allocation (1-100%)
  - Date range support (with indefinite allocations)
  - Automatic validation to prevent over-allocation
  - Overlap detection for conflicting allocations

## Architecture

The project is structured as follows:

```
ProjectAllocationManagerMCP/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── src/
│   └── main/
│       └── kotlin/
│           └── io/modelcontextprotocol/allocation/
│               ├── Main.kt
│               ├── McpAllocationServer.kt
│               ├── models/
│               │   ├── Allocation.kt
│               │   ├── Engineer.kt
│               │   └── Project.kt
│               └── services/
│                   └── AllocationService.kt
└── data/
    ├── engineers.json
    ├── projects.json
    └── allocations.json
```

## Available MCP Tools

### 1. list_engineers
Lists all engineers in the system with their details.

**Parameters**: None

**Returns**: JSON array of engineers

### 2. list_projects
Lists all projects in the system with their details.

**Parameters**: None

**Returns**: JSON array of projects

### 3. list_allocations
Lists all allocations in the system.

**Parameters**: None

**Returns**: JSON array of allocations

### 4. get_engineer
Gets detailed information about a specific engineer.

**Parameters**:
- `engineer_id` (string, required): Engineer ID (e.g., "eng-001")

**Returns**: Engineer object or error message

### 5. get_project
Gets detailed information about a specific project.

**Parameters**:
- `project_id` (string, required): Project ID (e.g., "proj-001")

**Returns**: Project object or error message

### 6. get_engineer_allocations
Gets all project allocations for a specific engineer.

**Parameters**:
- `engineer_id` (string, required): Engineer ID

**Returns**: JSON array of allocations for the engineer

### 7. get_project_allocations
Gets all engineer allocations for a specific project.

**Parameters**:
- `project_id` (string, required): Project ID

**Returns**: JSON array of allocations for the project

### 8. allocate_engineer
Allocates an engineer to a project with specified percentage and date range.

**Parameters**:
- `engineer_id` (string, required): Engineer ID
- `project_id` (string, required): Project ID
- `allocation_percentage` (integer, required): Percentage (1-100)
- `start_date` (string, optional): Start date in YYYY-MM-DD format (defaults to today)
- `end_date` (string, optional): End date in YYYY-MM-DD format (leave empty for indefinite)

**Validations**:
- Engineer and project must exist
- Allocation percentage must be 1-100
- Total allocations for an engineer cannot exceed 100% during overlapping periods
- Cannot allocate same engineer to same project with overlapping dates

**Returns**: Success message with allocation details or error message

### 9. update_allocation
Updates an existing allocation.

**Parameters**:
- `allocation_id` (string, required): Allocation ID
- `allocation_percentage` (integer, optional): New percentage (1-100)
- `start_date` (string, optional): New start date in YYYY-MM-DD format
- `end_date` (string, optional): New end date in YYYY-MM-DD format

**Returns**: Success message with updated allocation details or error message

## Building the Project

### Prerequisites

- JDK 17 or higher
- Gradle (or use the Gradle wrapper)

### Build Commands

```bash
# Build the project
./gradlew build

# Create a fat JAR with all dependencies
./gradlew shadowJar
```

The fat JAR will be created at:
```
build/libs/project-allocation-manager-mcp-0.1.0-all.jar
```

## Running the Server

### Using Gradle

```bash
./gradlew run
```

### Using the JAR

```bash
java -jar build/libs/project-allocation-manager-mcp-0.1.0-all.jar
```

## Data Files

The server loads data from JSON files in the `data/` directory:

- `engineers.json`: List of engineers with their skills
- `projects.json`: List of projects with descriptions
- `allocations.json`: List of current allocations

### Sample Data Structure

**Engineer:**
```json
{
  "id": "eng-001",
  "name": "Alice Johnson",
  "role": "Senior Software Engineer",
  "skills": ["C#", ".NET", "React"]
}
```

**Project:**
```json
{
  "id": "proj-001",
  "name": "Project Alpha",
  "description": "E-commerce platform development",
  "status": "active"
}
```

**Allocation:**
```json
{
  "id": "alloc-001",
  "engineerId": "eng-001",
  "projectId": "proj-001",
  "allocationPercentage": 50,
  "startDate": "2025-01-01",
  "endDate": "2025-06-30"
}
```

## Technology Stack

- **Kotlin 2.2.21**: Programming language
- **MCP Kotlin SDK 0.7.4**: Model Context Protocol implementation
- **Ktor 3.2.3**: HTTP client for potential API calls
- **kotlinx.serialization**: JSON serialization/deserialization
- **Gradle**: Build tool

## Development

### Project Structure

- **models/**: Data classes for Engineer, Project, and Allocation
- **services/**: Business logic in AllocationService
- **McpAllocationServer.kt**: MCP server implementation with tool definitions
- **Main.kt**: Application entry point

### Key Components

1. **AllocationService**: Core business logic handling:
   - Data loading from JSON files
   - CRUD operations for engineers, projects, and allocations
   - Validation logic for allocations
   - Date overlap detection

2. **McpAllocationServer**: MCP server setup with:
   - Tool registration for all operations
   - Request/response handling
   - JSON serialization

## Differences from C# Version

- Uses Kotlin idioms (data classes, null safety, extension functions)
- kotlinx.serialization instead of System.Text.Json
- Coroutines instead of async/await (though most operations are synchronous)
- Date handling using java.time.LocalDate instead of DateTime
- Gradle build system instead of .NET SDK

## License

This is a demonstration project for MCP server implementation in Kotlin.

## Contributing

This project was converted from C# to Kotlin as a learning exercise. Feel free to extend it with additional features such as:

- Persistence to database
- REST API endpoints
- Authentication and authorization
- More complex allocation rules
- Reporting and analytics tools
