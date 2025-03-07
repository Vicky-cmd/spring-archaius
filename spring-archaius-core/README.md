# Spring Archaius Code Module

## Overview

Spring Archaius is a powerful configuration management library that provides a flexible and robust way to handle application configurations in Spring-based applications. It extends Spring's configuration capabilities with advanced features like dynamic updates, type-safe retrieval, validation, and structured configuration metadata.

## Key Features

- **Type-safe Configuration**: Retrieve configuration values with proper type conversion
- **Dynamic Updates**: Configuration changes are automatically detected and applied without restart
- **Default Value Management**: Multiple strategies for providing fallback values
- **Field-based Configuration**: Structured way to define configuration metadata
- **Validation Rules**: Enforce configuration constraints and validate values
- **Format String Support**: Dynamic configuration key generation
- **Object Mapping**: Convert configuration values to custom Java objects
- **Map Configuration**: Support for hierarchical data structures as maps

## Getting Started

### Installation

#### Using BOM (Recommended)

Add the BOM (Bill of Materials) to manage Spring Archaius dependencies in your project:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.vickycmd</groupId>
            <artifactId>spring-archaius-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then add the core dependency:

```xml
<dependency>
    <groupId>io.github.vickycmd</groupId>
    <artifactId>spring-archaius-core</artifactId>
</dependency>
```

### Setup and Configuration

1. Enable Spring Archaius in your application by adding the `@EnableSpringArchaius` annotation to your configuration class:

```java
@Configuration
@EnableSpringArchaius
public class AppConfig {
    // Your configuration here
}
```

2. Inject and use the configuration in your components:

```java
@Service
public class MyService {
    private final Configuration configuration;
    
    @Autowired
    public MyService(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public void process() {
        int timeout = configuration.get("service.timeout", 30, Integer.class);
        // Use the configuration value
    }
}
```

For comprehensive documentation on usage patterns and advanced features, refer to the detailed documentation for [Configuration](src/main/java/io/github/vickycmd/config/README.md) and [Field](src/main/java/io/github/vickycmd/config/fields/README.md) classes.


## Core Components

### Configuration Class

The `Configuration` class is the main entry point for retrieving configuration values. It provides methods for:
- Getting configuration values with type safety
- Setting default values
- Format string support for dynamic keys
- Object and map conversion

[View detailed Configuration documentation](src/main/java/io/github/vickycmd/config/README.md)


### Field Class

The `Field` class provides a structured way to define configuration metadata with:
- Type information
- Validation rules
- Documentation
- Default values
- Importance levels

[View detailed Field documentation](src/main/java/io/github/vickycmd/config/fields/README.md)


## Advantages

### 1. Improved Code Maintainability

By centralizing configuration metadata and logic, this module makes your application more maintainable:

```java
Field databaseUrlField = Field.builder()
    .name("database.url")
    .displayName("Database URL")
    .desc("The connection URL for the database")
    .type(Field.Type.STRING)
    .required()
    .importance(Field.Importance.HIGH)
    .defaultValue("jdbc:mysql://localhost:3306/mydb")
    .build();
```

### 2. Type Safety

All configuration retrieval is type-safe, eliminating runtime errors due to type mismatches:

```java
// Type-safe retrieval
Integer port = configuration.get("server.port", Integer.class);
String name = configuration.get("app.name", String.class);
```

### 3. Runtime Validation

Configuration values can be validated at runtime, ensuring they meet requirements:

```java
Field portField = Field.builder()
    .name("server.port")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .required()
    .build();
```

### 4. Dynamic Configuration Updates

Configuration values can be updated without restarting the application, improving operational flexibility.

### 5. Comprehensive Default Value System

Multiple ways to provide default values ensure your application is resilient to missing configurations:

```java
// Method-level default
String value = configuration.get("app.name", "DefaultApp", String.class);

// With supplier
String generated = configuration.get("app.key", () -> generateKey(), String.class);

// Pre-configured defaults
configuration.withDefault("app.timeout", 5000);
```

## Use Cases

- **Microservices Configuration**: Manage configuration for distributed systems
- **Feature Flags**: Toggle features on/off with runtime updates
- **Environment-specific Settings**: Handle different environments with the same codebase
- **Multi-tenant Applications**: Configure per-tenant settings
- **External Service Integration**: Manage connection parameters for third-party services


## Configuration Sources

Spring Archaius supports multiple configuration sources, allowing you to store and retrieve configuration data from various backends.


### Properties File Configuration (Default)

Spring Archaius uses Spring's property resolution mechanism as its default configuration source. This allows you to define configurations in various property file formats and locations.

#### Supported Formats
- `.properties` files
- `.yml` files
- Command-line arguments

#### Dynamic Configuration Updates

Spring Archaius can monitor configuration files for changes and automatically apply updates without requiring application restart. To enable this feature, configure the following property:

```properties
spring.archaius.config.source.file=path/to/your/config-file.properties
```

The specified file will be monitored for changes, and when modifications are detected, the configuration will be automatically reloaded. This is particularly useful for:
- Updating feature flags in production
- Changing log levels dynamically
- Modifying connection timeouts and limits
- Adjusting application behavior without downtime


#### Example Configuration

```properties
# Base configuration in application.properties
server.port=8080
app.name=MyApplication
feature.reporting.enabled=true

# Dynamic configuration source
spring.archaius.config.source.file=dynamic-config.properties
```

In the monitored file (`dynamic-config.properties`):

```properties
# These values can be changed at runtime
feature.reporting.enabled=false
app.timeout=30000
```

When you modify `dynamic-config.properties`, the changes will be detected and applied without restarting the application.

#### Configuration Refresh Strategy

Spring Archaius uses a polling mechanism to check for configuration changes. The polling interval is configurable using:

```properties
spring.archaius.config.refresh.interval=30
```

This property defines how often (in seconds) the system checks for configuration changes. The default value is 30 seconds.


### MongoDB Configuration Source

Spring Archaius provides built-in support for MongoDB as a configuration source, enabling you to store your application configurations in a MongoDB database.

#### Setup MongoDB Source

Spring Archaius provides built-in support for MongoDB as a configuration source, enabling you to store your application configurations in a MongoDB database.

#### Setup MongoDB Source

To use MongoDB as a configuration source, you need to add the following dependency to your project:

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-mongodb</artifactId>
</dependency>

<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
</dependency>
```

#### Configuration Properties

The following properties need to be configured to use MongoDB as a configuration source:


| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `spring.archaius.config.source` | Set to "mongo" to use MongoDB | - | Yes |
| `spring.archaius.config.mongo.uri` | MongoDB connection URI | mongodb://localhost:27017 | Yes |
| `spring.archaius.config.mongo.database` | Database name | - | Yes |
| `spring.archaius.config.mongo.collection` | Collection name | - | Yes |


#### Example Configuration

```yaml
spring:
  archaius:
    config:
      source: mongo
      mongo:
        uri: mongodb://localhost:27017
        database: config_db
        collection: application_properties
```

#### MongoDB Document Structure

The MongoDB collection should contain documents with the following structure:

```json
{
  "_id": {
    "$oid": "66d9632e902de74a8d97c3d8"
  },
  "key": "application.feature.enabled",
  "value": "true",
  "type": "BOOLEAN"
}
```

#### Example Usage

```java
@Service
public class MyService {
    private final Configuration configuration;
    
    // Field definitions
    private static final Field FEATURE_ENABLED = Field.create("application.feature.enabled")
            .displayName("Feature Enabled")
            .desc("Enable/disable the feature")
            .type(Field.Type.BOOLEAN)
            .defaultValue(false)
            .build();
    
    @Autowired
    public MyService(Configuration configuration) {
        this.configuration = configuration;
        // Validate required configurations
        Utilities.validateConfig(this.configuration, Field.Set.of(FEATURE_ENABLED), getClass(), log);
    }
    
    public void process() {
        if (configuration.get(FEATURE_ENABLED, Boolean.class)) {
            // Feature is enabled
        }
    }
}
```


## Multiple Configuration Sources

You can combine the file-based configuration with MongoDB or other sources. In this case, the most recent update from any source will take precedence:

```properties
# Use MongoDB as primary source
spring.archaius.config.source=mongo
spring.archaius.config.mongo.uri=mongodb://localhost:27017
spring.archaius.config.mongo.database=config_db
spring.archaius.config.mongo.collection=application_properties

# With dynamic file updates
spring.archaius.config.source.file=override-config.properties
```

When both sources are configured, the application will load configuration from MongoDB and then watch for changes in the specified file. If a property is updated in the file, it will override the value from MongoDB until MongoDB is updated with a newer value.


## Supported Configuration Types

Spring Archaius supports a wide range of configuration types to meet various application needs:

### Basic Types

| Type | Java Type | Description | Example |
|------|-----------|-------------|---------|
| `STRING` | String | Text values | `configuration.get("app.name", String.class)` |
| `BOOLEAN` | Boolean | True/false values | `configuration.get("feature.enabled", Boolean.class)` |
| `INT` | Integer | Integer values | `configuration.get("server.port", Integer.class)` |
| `LONG` | Long | Long integer values | `configuration.get("timeout.ms", Long.class)` |
| `FLOAT` | Float | Floating-point values | `configuration.get("rate.limit", Float.class)` |
| `DOUBLE` | Double | Double-precision values | `configuration.get("threshold", Double.class)` |

### Complex Types

| Type | Java Type | Description | Example |
|------|-----------|-------------|---------|
| `MAP` | Map<String, Object> | Key-value pairs | `configuration.get("app.properties", Map.class)` |
| `LIST` | List<Object> | Ordered list | `configuration.get("server.urls", List.class)` |
| `OBJECT` | Custom Object | Complex object | `configuration.get("app.config", AppConfig.class)` |

### Special Types

| Type | Java Type | Description | Example |
|------|-----------|-------------|---------|
| `PASSWORD` | String | Sensitive information | `configuration.get("db.password", String.class)` |


### Usage Examples

#### List Configuration

```java
// Define field for a list
Field allowedIpsField = Field.builder()
    .name("allowed.ips")
    .displayName("Allowed IP Addresses")
    .desc("List of IP addresses allowed to access the API")
    .type(Field.Type.LIST)
    .defaultValue(List.of("127.0.0.1"))
    .build();

// Get list of values
List<String> allowedIps = configuration.getList(allowedIpsField, String.class);
```

#### Object Configuration

```java
// Custom configuration class
public class DatabaseConfig {
    private String url;
    private int port;
    private String username;
    // getters, setters
}

// Define field for object
Field dbConfigField = Field.builder()
    .name("database.config")
    .displayName("Database Configuration")
    .desc("Configuration for database connection")
    .type(Field.Type.OBJECT)
    .build();

// Get object configuration
DatabaseConfig dbConfig = configuration.getObject(dbConfigField, DatabaseConfig.class);
```

#### Password Configuration

```java
Field passwordField = Field.builder()
    .name("api.secret")
    .displayName("API Secret")
    .desc("Secret key for API authentication")
    .type(Field.Type.PASSWORD)
    .required()
    .build();

// Get password value
String apiSecret = configuration.get(passwordField, String.class);
```

## Configuration Validation

Spring Archaius provides robust validation capabilities:

### Built-in Validators

- Required field validation
- Type validation
- Range validation
- Regular expression validation
- Custom validator


### Examples

```java
// Positive integer validation
Field portField = Field.builder()
    .name("server.port")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .required()
    .build();

// Allowed values validation
Field envField = Field.builder()
    .name("app.environment")
    .type(Field.Type.STRING)
    .allowedValues("dev", "staging", "prod")
    .build();

// Custom validation
Field urlField = Field.builder()
    .name("service.url")
    .type(Field.Type.STRING)
    .validator(value -> {
        try {
            new URL(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    })
    .build();
```
