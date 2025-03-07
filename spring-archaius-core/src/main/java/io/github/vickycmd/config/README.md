# Configuration Management

## Overview

The Configuration class provides a flexible and robust way to manage application configurations in Spring-based applications. It supports various data types, default values, validation rules, and dynamic configuration updates.

## Features

- Supports various data types: String, Integer, Boolean, List, Map, etc.
- Provides default values for configuration properties.
- Allows validation of configuration values.
- Supports dynamic configuration updates.
- Type-safe configuration retrieval
- Default value support
- Field-based configuration management
- Dynamic configuration updates
- Validation rules
- Format string support for dynamic keys
- Object mapping capabilities

## Basic Usage


### Simple Configuration Retrieval

```java
// Get a string configuration value
String value = configuration.get("application.property", String.class);

// Get with default value
Integer port = configuration.get("server.port", 8080, Integer.class);

// Get with default value supplier
String version = configuration.get("app.version", () -> "v1.0", String.class);
```

### Field-Based Configuration

Reference to field configuration example:
```java
Field portField = Field.builder()
    .name("server.port")
    .displayName("Server Port")
    .desc("The port number for the server")
    .type(Field.Type.INT)
    .importance(Field.Importance.HIGH)
    .defaultValue(8080)
    .required()
    .build();

Integer port = configuration.get(portField, Integer.class);
```

## Configuration Types

The Configuration class supports multiple data types:
- Primitive types (String, Boolean, Integer, Long, etc.)
- Collections (List, Map)
- Complex objects (via ObjectMapper)
- Maps
- Custom objects

## Type-Safe Configuration

The Configuration class provides type-safe methods for retrieving configuration values.

## Features in Detail

### 1. Default Values
There are three ways to specify default values:
a. Direct default value:

```java
String value = configuration.get("app.name", "DefaultApp", String.class);
```

b. Default value supplier:

```java
String value = configuration.get("app.name", () -> "DefaultApp", String.class);
```

c. Pre-configured defaults:

```java
configuration.withDefault("app.timeout", 5000)
            .withDefault("app.retries", 3);
int value = configuration.get("app.retries", String.class);
```
*Note:* The default values are applied only if the configuration value is not found in the configuration source and are applicable only for the current instance of the Configuration class.

### 2. Field-Based Configuration

Fields provide a structured way to define configurations with metadata:

```java
Field portField = Field.builder()
    .name("server.port")
    .displayName("Server Port")
    .desc("The port number for the server")
    .type(Field.Type.INT)
    .defaultValue(8080)
    .required()
    .build();

Integer port = configuration.get(portField, Integer.class);
```

### 3. Format String Support

The Configuration class provides a powerful feature for dynamic configuration key generation using format strings. This allows you to create flexible configuration patterns where parts of the key can be substituted at runtime.


#### Basic Usage

```java
// Get a configuration value with a dynamic key part
String value = configuration.get("app.%s.config", String.class, "feature1");

// This is equivalent to:
String value = configuration.get("app.feature1.config", String.class);
```

#### Multiple Substitutions

You can use multiple format specifiers in a single key:

```java
// Multiple substitutions
String value = configuration.get("app.%s.%s.config", String.class, "feature1", "enabled");

// This is equivalent to:
String value = configuration.get("app.feature1.enabled.config", String.class);
```

#### With Field Objects

Format strings also work with Field objects:

```java
Field configField = Field.builder()
    .name("service.%s.timeout")
    .displayName("Service Timeout")
    .desc("Timeout for the specified service")
    .type(Field.Type.INT)
    .defaultValue(5000)
    .build();

// Get configuration for a specific service
Integer userServiceTimeout = configuration.get(configField, Integer.class, "user");
Integer orderServiceTimeout = configuration.get(configField, Integer.class, "order");
```
#### How It Works
When you call a method with format string arguments:
1. The system formats the key string using String.format(property, args)
2. The formatted key is then used to look up the configuration value
3. All other processing (default values, validation, etc.) works as normal

This feature is particularly useful for:
 - Service-specific configurations
 - Feature toggles for different components
 - Environment-specific settings
 - Multi-tenant applications


### 4. Object Mapping Support

Support for complex object mapping:

#### Basic Usage:

```java
// Define your configuration class
public class DatabaseConfig {
    private String url;
    private int port;
    private String username;
    // getters, setters
}

// Get configuration as object
DatabaseConfig config = configuration.getObject("database", DatabaseConfig.class);
```

#### Setup Example:

1. *Define Your Custom Object*
```java
public class OffersFeaturesDto {
    private V1FeaturesDto v1Features;
    private boolean displayFeatures;
    private String displayText;
    
    // Getters, setters, and builder pattern
    
    public static class V1FeaturesDto {
        private boolean enabled;
        // Getters, setters
    }
}
```
2. *Create Field Definition*
```java
Field offerFeaturesField = Field.builder()
    .name("application.offers.features")
    .displayName("Offer Features")
    .desc("The map containing configuration related to different offer features")
    .type(Field.Type.OBJECT)
    .className(OffersFeaturesDto.class) // The type of the object to be mapped
    .defaultValue(defaultValue)  // Optional default value
    .build();
```

#### Retrieval Methods
1. *Simple Object Retrieval*
```java
OffersFeaturesDto config = configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
```

2. *With Default Value*
```java
OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
    .v1Features(new V1FeaturesDto(false))
    .displayFeatures(false)
    .displayText("Default Features")
    .build();

OffersFeaturesDto config = configuration.getObject(
    offerFeaturesField, 
    defaultValue, 
    OffersFeaturesDto.class
);
```

3. *With Default Value Supplier*
```java
Supplier<OffersFeaturesDto> defaultSupplier = () -> OffersFeaturesDto.builder()
    .v1Features(new V1FeaturesDto(false))
    .displayFeatures(false)
    .build();

OffersFeaturesDto config = configuration.getObject(
    offerFeaturesField, 
    defaultSupplier, 
    OffersFeaturesDto.class
);
```

### 5. Map Configuration Support

The Configuration class provides robust support for map-based configurations, allowing you to store and retrieve hierarchical data structures as key-value pairs.

#### Basic Usage:

```java
// Get configuration as map
Map<String, Object> features = configuration.getMap("application.features");

// With default value
Map<String, Object> defaultFeatures = Map.of("feature1", true, "feature2", false);
Map<String, Object> features = configuration.getMap("application.features", defaultFeatures);
```

#### Retrieval Methods
1. *Simple Map Retrieval*
```java
Map<String, Object> features = configuration.getMap("application.features");
```

2. *With Default Value*
```java
Map<String, Object> defaultFeatures = Map.of("feature1", true, "feature2", false);
Map<String, Object> features = configuration.getMap("application.features", defaultFeatures);
```

3. *With Default Value Supplier*
```java
Supplier<Map<String, Object>> defaultSupplier = () -> Map.of("feature1", true, "feature2", false);
Map<String, Object> features = configuration.getMap("application.features", defaultSupplier);
```

#### Using Field Objects
Maps can be defined and retrieved using Field objects for better type safety and documentation:

```java
Field featureMapField = Field.builder()
    .name("application.features")
    .displayName("Application Features")
    .desc("Map of application feature toggles")
    .type(Field.Type.MAP)
    .defaultValue(Map.of("feature1", false, "feature2", false))
    .build();

// Retrieve the map
Map<String, Object> features = configuration.getMap(featureMapField);
```

With explicit default value:

```java
Map<String, Object> defaultFeatures = Map.of("feature1", true, "feature2", true);
Map<String, Object> features = configuration.getMap(featureMapField, defaultFeatures);
```

#### Nested Map Structures

The Configuration system supports complex nested map structures:

```java
// Configuration value: {"auth": {"timeout": 30, "retries": 3}, "features": {"logging": true}}
Map<String, Object> config = configuration.getMap("application.config");

// Access nested values
Map<String, Object> auth = (Map<String, Object>) config.get("auth");
Integer timeout = (Integer) auth.get("timeout"); // 30
```

#### JSON to Map Conversion


Map configurations are typically stored as JSON strings and automatically converted:

```java
// JSON configuration: {"v1": {"enabled": true}, "displayFeatures": true, "displayText": "Features"}
Map<String, Object> features = configuration.getMap("application.features");

// Access values
Map<String, Object> v1 = (Map<String, Object>) features.get("v1");
boolean enabled = Boolean.parseBoolean((String) v1.get("enabled"));
```

#### How It Works

When retrieving a map configuration:
1. The system retrieves the configuration value as a string
2. The string is parsed as a JSON object
3. The JSON object is converted to a Map<String, Object>
4. Default values are applied if the configuration doesn't exist
5. The map is returned, allowing direct access to properties


### 6. Field Validation

Fields can include validation rules:

```java
Field portField = Field.builder()
    .name("server.port")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .required()
    .build();
```

### 7. Allowed Values

Restrict configuration values to a specific set:

```java
Field environmentField = Field.builder()
    .name("app.environment")
    .type(Field.Type.STRING)
    .allowedValues("dev", "staging", "prod")
    .build();
```

## Default Value Precedence

The system follows this order when determining the value to return:
1. Actual configuration value if present in the configuration source
2. Default value provided in the method call
3. Default value defined in the Field object
4. Null if no defaults are specified

For reference to the implementation, see:
```java
    @ValidateField
    public <T> T getObject(Field property, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType), targetType);
    }

    @ValidateField
    public <T> T getObject(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), defaultValueSupplier.get(), targetType), targetType);
    }

    @ValidateField
    public <T> T getObject(Field property, T defaultValue, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), defaultValue, targetType), targetType);
    }
```

## Best Practices

1. **Use Field Definitions**: Define configurations using Field objects for better maintainability and documentation.

2. **Validate Configurations**: Always validate critical configurations during startup.

3. **Provide Default Values**: Use default values to ensure application resilience.

4. **Use Type-Safe Methods**: Always use the appropriate type-safe methods for configuration retrieval.

5. **Handle Exceptions**: Properly handle ConfigException for configuration errors.


## Error Handling

The Configuration class throws `ConfigException` in the following cases:
- Required field is missing
- Invalid data type
- Validation failure
- Configuration parsing errors


Example error handling:

```java
try {
    String value = configuration.get(requiredField, String.class);
} catch (ConfigException e) {
    // Handle configuration error
    logger.error("Configuration error: {}", e.getMessage());
}
```

## Real-World Example

Here's a complete example showing various features:

```java
Field databaseConfig = Field.builder()
    .name("database.url")
    .displayName("Database URL")
    .desc("The connection URL for the database")
    .type(Field.Type.STRING)
    .required()
    .importance(Field.Importance.HIGH)
    .defaultValue("jdbc:mysql://localhost:3306/mydb")
    .build();

// Usage in your service
@Service
public class DatabaseService {
    private final Configuration configuration;
    
    @Autowired
    public DatabaseService(Configuration configuration) {
        this.configuration = configuration;
        // Validate configuration at startup
        Utilities.validateConfig(this.configuration, Field.Set.of(databaseConfig), getClass(), log);
    }

    public void execurte () {
        // ...
        String databaseUrl = this.configuration.get(databaseConfig, String.class);
        // ...
    }
}
```

For more examples of field configurations, see:
```java
    private final Field MONGO_URI_CONFIG = Field.create("spring.archaius.config.mongo.uri")
            .displayName("Mongo Config Source URI")
            .desc("The URI for the mongo Database to be used by the application")
            .defaultValue("mongodb://localhost:27017")
            .importance(Field.Importance.HIGH)
            .type(Field.Type.STRING)
            .required()
            .build();

    static final Field DATABASE_FIELD = Field.create("spring.archaius.config.mongo.database")
            .displayName("Mongo Config Source Database")
            .desc("The database containing the Collection with the Application configurations")
            .type(Field.Type.STRING)
            .importance(Field.Importance.HIGH)
            .required()
            .build();
```

## Integration with Spring

The Configuration class is designed to work seamlessly with Spring:

```java
@Autowired
private Configuration configuration;
```

## Performance Considerations
- Configuration values are cached internally
- Default values are stored in a HashMap for quick access
- Object mapping is performed using Jackson ObjectMapper
- Validation is performed only when required

## Thread Safety

The Configuration class is designed to be thread-safe and can be safely used in concurrent environments. It is marked with `@Scope("prototype")` to ensure proper instance management in Spring applications.