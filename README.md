<div align="center">
    <p>
        <a href="#"><img src="https://raw.githubusercontent.com/Vicky-cmd/spring-archaius/refs/heads/main/resources/logo-wide.jpg" height="300" alt="Spring Archaius"></a>
    </p>
    <h1>Spring Archaius</h1>
</div>

[![License][license-image]][license-url]

<hr/>

Spring Archaius is a configuration management library for Spring applications that provides type-safe configuration handling, validation, and automatic documentation generation.

## Overview

Spring Archaius offers:
- Type-safe configuration management
- Field-based configuration definitions
- Built-in validation
- Dynamic configuration updates
- Automatic documentation generation
- Maven plugin integration

## Why Spring Archaius?

### Key Advantages

1. **Type Safety and Validation**
   - Compile-time type checking prevents configuration errors
   - Built-in validators for common data types
   - Custom validation support for complex business rules
   - Early detection of configuration issues during application startup

2. **Centralized Configuration Management**
   - Single source of truth for all application configurations
   - Consistent configuration access patterns
   - Reduced configuration duplication
   - Easy configuration updates across services

3. **Self-Documenting Configurations**
   - Fields contain metadata about their purpose and constraints
   - Documentation is always in sync with code
   - Reduced maintenance overhead
   - Better developer experience

### Documentation Generator Features

The documentation generator provides several powerful capabilities:

1. **Automatic Metadata Extraction**
   ```java
   Field apiRateLimit = Field.builder()
       .name("api.rate.limit")
       .displayName("API Rate Limit")
       .desc("Maximum number of API calls per minute")
       .type(Field.Type.INT)
       .importance(Field.Importance.HIGH)
       .defaultValue(100)
       .build();
   ```
   - Extracts field names, types, and descriptions
   - Captures validation rules and constraints
   - Documents default values and importance levels

2. **Markdown Documentation Generation**
   - Generates structured, readable documentation
   - Creates tables of all configuration options
   - Includes validation rules and allowed values
   - Documents lambda expressions and custom validators

3. **Integration Options**
   - Maven plugin for build-time documentation
   - Command-line tool for manual generation
   - Flexible output formatting
   - Custom template support

### Use Cases

1. **Microservices Architecture**
   - Consistent configuration across services
   - Type-safe configuration sharing
   - Automated documentation for service configurations

2. **Enterprise Applications**
   - Complex configuration validation
   - Hierarchical configuration management
   - Configuration documentation for operations teams

3. **Development Teams**
   - Self-documenting configuration code
   - Reduced configuration errors
   - Better onboarding experience
   - Automated documentation maintenance

4. **DevOps Integration**
   - Configuration validation during deployment
   - Automated documentation generation in CI/CD
   - Clear configuration requirements for operations

### Documentation Generator Example

The documentation generator creates comprehensive markdown files. Please refer to the [documentation](./resources/archaius-config-report.md) for more details.


### Integration Example

Using the Maven plugin for documentation generation:

```xml
<plugin>
    <groupId>io.github.vickycmd</groupId>
    <artifactId>spring-archaius-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-config-docs</id>
            <phase>package</phase>
            <goals>
                <goal>generate-doc</goal>
            </goals>
            <configuration>
                <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
                <outputDirectory>${project.build.directory}/docs</outputDirectory>
                <applicationName>My Application</applicationName>
            </configuration>
        </execution>
    </executions>
</plugin>
```


## Modules

- **spring-archaius-core**: Core configuration management functionality
- **spring-archaius-docgen**: Documentation generation tool
- **spring-archaius-plugin**: Maven plugin for documentation generation

## Installation

### Using BOM (Recommended)

Add the Spring Archaius BOM to your project:

```xml
xml
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


### Core Module

Add the core dependency:

## Core Module Usage

### 1. Field Definitions

Fields are the building blocks of configuration in Spring Archaius. They provide type-safe configuration with metadata and validation.

#### Basic Field Definition

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

#### Field with Validation

```java
Field portField = Field.builder()
    .name("server.port")
    .displayName("Server Port")
    .desc("The port number for the server")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .defaultValue(8080)
    .build();
```

#### Field with Dynamic Default Value

```java
Field versionField = Field.builder()
    .name("app.version")
    .displayName("Application Version")
    .desc("Current application version")
    .type(Field.Type.STRING)
    .defaultValueGenerator(() -> "v" + System.currentTimeMillis())
    .build();
```

### 2. Configuration Usage

#### Basic Configuration Retrieval

```java
@Service
public class DatabaseService {
    private final Configuration configuration;
    
    @Autowired
    public DatabaseService(Configuration configuration) {
        this.configuration = configuration;
    }

    public void connect() {
        String url = configuration.get("database.url", String.class);
        int port = configuration.get("database.port", 5432, Integer.class);
        // Use the configuration values
    }
}
```

#### Type-Safe Configuration with Fields

```java
@Service
public class ApplicationService {
    private static final Field API_KEY = Field.builder()
        .name("api.key")
        .type(Field.Type.STRING)
        .required()
        .build();

    private final Configuration configuration;
    
    @Autowired
    public ApplicationService(Configuration configuration) {
        this.configuration = configuration;
        // Validate required configurations
        Utilities.validateConfig(configuration, Field.Set.of(API_KEY), getClass(), log);
    }

    public void process() {
        String apiKey = configuration.get(API_KEY, String.class);
        // Use the configuration
    }
}
```

### 3. Complex Configuration Types

#### Map Configuration

```java
Field featuresField = Field.builder()
.name("application.features")
.type(Field.Type.MAP)
.defaultValue(Map.of("feature1", true, "feature2", false))
.build();
// Usage
Map<String, Object> features = configuration.getMap(featuresField);
```

#### Object Configuration

```java
public class DatabaseConfig {
    private String url;
    private int port;
    private String username;
    // getters, setters

}

Field dbConfigField = Field.builder()
    .name("database.config")
    .type(Field.Type.OBJECT)
    .className(DatabaseConfig.class)
    .build();

// Usage
DatabaseConfig dbConfig = configuration.getObject(dbConfigField, DatabaseConfig.class);
```

### 4. Validation

#### Built-in Validators

```java
Field timeoutField = Field.builder()
    .name("connection.timeout")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .build();
```

#### Custom Validation

```java
Field portField = Field.builder()
    .name("server.port")
    .type(Field.Type.INT)
    .validator((config, field, problems) -> {
        int port = config.getInteger(field);
        if (port < 1024 || port > 65535) {
            problems.accept(field, port, "Port must be between 1024 and 65535");
            return 1;
        }
        return 0;
    })
    .build();
```

## Documentation Generation

Spring Archaius provides automatic documentation generation through:

1. Maven Plugin
2. Command-line tool

### Maven Plugin Usage

Add to your pom.xml:

```xml
<plugin>
    <groupId>io.github.vickycmd</groupId>
    <artifactId>spring-archaius-plugin</artifactId>
    <executions>
    <execution>
    <goals>
        <goal>generate-doc</goal>
    </goals>
    <phase>package</phase>
    </execution>
    </executions>
</plugin>
```


## Detailed Documentation

For more detailed information about each module, please refer to:

- [Core Module Documentation](spring-archaius-core/README.md)
- [Documentation Generator](spring-archaius-docgen/README.md)
- [Maven Plugin](spring-archaius-plugin/README.md)

## Best Practices

1. **Field Definitions**
   - Keep field definitions in dedicated configuration classes
   - Use meaningful names and descriptions
   - Set appropriate importance levels
   - Include validation rules where applicable

2. **Configuration Usage**
   - Validate configurations during service initialization
   - Use type-safe methods for configuration retrieval
   - Provide sensible default values
   - Handle configuration errors gracefully

3. **Documentation**
   - Generate and maintain up-to-date configuration documentation
   - Include the documentation generation in your build process
   - Review generated documentation for completeness

## Support

For issues and questions:
- GitHub Issues: [Spring Archaius Issues](https://github.com/vickycmd/spring-archaius/issues)
- Documentation: [Spring Archaius Docs](https://github.com/vickycmd/spring-archaius/docs)


[license-url]: LICENSE
[license-image]: https://img.shields.io/badge/License-GPLv3-green.svg?style=for-the-badge&logo=appveyor

