# Aerospike Application Demo

This is a Spring Boot application that demonstrates integration with Aerospike, a high-performance, distributed NoSQL database. The application provides RESTful APIs for basic CRUD operations on user data stored in an Aerospike cluster.

## Table of Contents

- [Overview](#overview)
- [Aerospike Cluster Setup](#aerospike-cluster-setup)
- [Application Architecture](#application-architecture)
- [Code Insights](#code-insights)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Testing](#testing)

## Overview

This demo application showcases how to:
- Set up a multi-node Aerospike cluster using Docker Compose
- Configure Spring Boot to connect to Aerospike
- Implement basic CRUD operations using the Aerospike Java client
- Structure a Spring Boot application with proper separation of concerns

The application stores user information (ID, name, age) in Aerospike and provides REST endpoints to manage this data.

## Aerospike Cluster Setup

The Aerospike cluster is configured using Docker Compose with three nodes for high availability and data replication.

### Cluster Configuration

Navigate to the `aerospike-cluster` directory:

```bash
cd ../aerospike-cluster
```

The cluster consists of:
- **aerospike1**: Primary node (port 3000 exposed to host)
- **aerospike2**: Secondary node
- **aerospike3**: Tertiary node

### Starting the Cluster

1. Ensure Docker and Docker Compose are installed
2. From the `aerospike-cluster` directory, run:

```bash
docker-compose up -d
```

This will start three Aerospike server containers. The cluster will automatically form and replicate data across nodes.

### Verifying Cluster Health

Check cluster status:

```bash
docker-compose ps
```

Connect to the cluster using Aerospike tools:

```bash
docker exec -it aerospike1 aql
```

In the AQL shell, you can run:
```sql
SHOW NAMESPACES
SHOW SETS
```

### Stopping the Cluster

```bash
docker-compose down
```

## Application Architecture

The application follows a standard Spring Boot layered architecture:

```
aerospikeapp/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic layer
└── AerospikeappApplication.java  # Main application class
```

## Code Insights

### Configuration Layer

**AerospikeProperties.java**
- Uses Spring Boot's `@ConfigurationProperties` to bind Aerospike settings from `application.yml`
- Defines host configurations, namespace, and set name
- Leverages Lombok's `@Data` for boilerplate reduction

**AerospikeConfig.java**
- Creates and configures the AerospikeClient bean
- Converts property configurations to Aerospike Host objects
- Uses Spring's dependency injection for clean configuration management

### Service Layer

**AeropsikeService.java** (Note: There's a typo in the class name - should be "AerospikeService")
- Contains business logic for Aerospike operations
- Methods for CRUD operations: `saveUser`, `getUser`, `existsUser`, `deleteUser`
- Uses Aerospike's Key, Bin, and Record classes for data manipulation
- Demonstrates proper resource management with null policies

Key concepts demonstrated:
- **Key**: Unique identifier for records (namespace, set, userKey)
- **Bin**: Data containers within records (name, age)
- **Record**: Complete data object retrieved from Aerospike

### Controller Layer

**AerospikeController.java**
- RESTful endpoints for user management
- Uses Spring Web annotations (`@RestController`, `@RequestMapping`, etc.)
- Demonstrates parameter binding and path variables
- Returns Aerospike Record objects directly (for simplicity in demo)

### Dependencies

- **Spring Boot Starter Web**: For REST API capabilities
- **Aerospike Java Client (v7.2.0)**: Official client library for Aerospike
- **Lombok**: Reduces boilerplate code
- **Spring Boot Starter Test**: For unit testing

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Basic understanding of NoSQL databases and REST APIs

## Running the Application

1. **Start Aerospike Cluster** (from `../aerospike-cluster`):
   ```bash
   docker-compose up -d
   ```

2. **Build the Application**:
   ```bash
   mvn clean compile
   ```

3. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### Save User
```http
POST /users/save?id={id}&name={name}&age={age}
```

Example:
```bash
curl -X POST "http://localhost:8080/users/save?id=user1&name=John&age=30"
```

### Get User
```http
GET /users/{id}
```

Example:
```bash
curl http://localhost:8080/users/user1
```

### Delete User
```http
DELETE /users/{id}
```

Example:
```bash
curl -X DELETE http://localhost:8080/users/user1
```

## Configuration

The application configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: aerospikeapp

aerospike:
  hosts:
    - host: localhost
      port: 3000
  namespace: test
  set: users
```

- **hosts**: List of Aerospike cluster nodes
- **namespace**: Aerospike namespace (similar to database)
- **set**: Aerospike set (similar to table)

## Testing

Run the tests:

```bash
mvn test
```

The application includes basic Spring Boot test setup. For integration testing with Aerospike, you would need to:

1. Start the Aerospike cluster
2. Configure test properties to point to test namespace
3. Use `@SpringBootTest` with Aerospike client injection

## Key Aerospike Concepts Demonstrated

1. **High Performance**: Aerospike's in-memory + SSD architecture
2. **Horizontal Scalability**: Multi-node cluster with automatic data distribution
3. **Data Modeling**: Key-Value store with bins for structured data
4. **Client Operations**: Put, Get, Delete operations
5. **Configuration Management**: Cluster discovery and connection management

## Troubleshooting

- **Connection Issues**: Ensure Aerospike cluster is running and accessible on port 3000
- **Data Not Found**: Check namespace and set names match configuration
- **Cluster Not Forming**: Verify Docker network connectivity between containers

## Next Steps

To extend this demo:
- Add more complex data models with multiple bins
- Implement batch operations
- Add secondary indexes for queries
- Implement connection pooling and retry logic
- Add comprehensive error handling and logging