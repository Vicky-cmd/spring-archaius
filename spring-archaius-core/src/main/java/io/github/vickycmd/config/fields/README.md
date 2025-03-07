# Field Configuration

The `Field` class provides a robust way to define and validate configuration fields in Java applications. It supports various data types, validation rules, and configuration options.

## Features

- Type-safe configuration fields
- Built-in validation
- Default value support
- Required field enforcement
- Allowed values restriction
- Importance levels
- Custom validation rules

## Basic Usage

Create a field using the builder pattern:
```java
Field portField = Field.builder()
    .name("server.port")
    .displayName("Server Port")
    .desc("The port number for the server")
    .type(Field.Type.INT)
    .defaultValue(8080)
    .build();
```
```java
Field portField = Field.create("server.port")
    .displayName("Server Port")
    .desc("The port number for the server")
    .type(Field.Type.INT)
    .defaultValue(8080)
    .build();
```

## Field Types

The following field types are supported:

- `BOOLEAN`: Boolean values
- `STRING`: String values
- `INT`: Integer values
- `SHORT`: Short values
- `LONG`: Long values
- `FLOAT`: Float values
- `DOUBLE`: Double values
- `LIST`: List of values
- `CLASS`: Java class names
- `PASSWORD`: Sensitive string values
- `MAP`: Key-value pairs
- `OBJECT`: Custom objects

## Field Properties

### Required Fields

Mark a field as required:
```java
Field apiKeyField = Field.builder()
    .name("api.key")
    .type(Field.Type.STRING)
    .required()
    .build();
```

### Default Values

Set static default values:
```java
Field timeoutField = Field.builder()
    .name("connection.timeout")
    .type(Field.Type.INT)
    .defaultValue(30)
    .build();
```


Or use a default value generator:
```java
Field versionField = Field.builder()
    .name("app.version")
    .type(Field.Type.STRING)
    .defaultValueGenerator(() -> "v2")
    .build();
```

### Allowed Values

Restrict values to a specific set:
```java
Field environmentField = Field.builder()
    .name("app.environment")
    .type(Field.Type.STRING)
    .allowedValues("dev", "staging", "prod")
    .build();
```


### Importance Levels

Set field importance:
```java
Field securityField = Field.builder()
    .name("security.enabled")
    .type(Field.Type.BOOLEAN)
    .importance(Field.Importance.HIGH)
    .build();
```


## Validation

### Built-in Validators

The Field class includes several built-in validators:

- `isRequiredValidator`: Ensures field value is not null or empty
- `isBooleanValidator`: Validates boolean values
- `isIntegerValidator`: Validates integer values
- `isLongValidator`: Validates long values
- `isPositiveLongValidator`: Validates positive long values
- `isNonNegativeLongValidator`: Validates non-negative long values
- `isShortValidator`: Validates short values
- `isDoubleValidator`: Validates double values
- `isPositiveIntegerValidator`: Validates positive integers
- `isNonNegativeIntegerValidator`: Validates non-negative integers

Example:
```java
Field countField = Field.builder()
    .name("items.count")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .build();
```

### Custom Validators

You can also define custom validators:
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


## Field Sets

Group related fields together:
```java
Field.Set configFields = Field.Set.of(
    Field.builder().name("db.url").type(Field.Type.STRING).required().build(),
    Field.builder().name("db.port").type(Field.Type.INT).defaultValue(5432).build(),
    Field.builder().name("db.username").type(Field.Type.STRING).required().build()
);
```

## Configuration Validation

Validate configuration fields:

```java
@Autowired
Configuration config;// your configuration instance
Field.Set fields = Utilities.validateConfig(config, fields, YourClass.class, logger);// your field set
```


## Error Handling

The validation process throws `ConfigException` when:
- Required fields are missing
- Values fail validation rules
- Values are not in the allowed set
- Type conversion fails

## Best Practices

1. Always provide descriptive names and descriptions for fields
2. Use appropriate field types for type safety
3. Set proper importance levels for configuration management
4. Group related fields into sets
5. Implement proper error handling
6. Use validation rules to ensure data integrity
7. Provide sensible default values when possible

## Example

Complete example showing various features:
```java
Field.Set databaseConfig = Field.Set.of(
Field.builder()
    .name("database.url")
    .displayName("Database URL")
    .desc("The connection URL for the database")
    .type(Field.Type.STRING)
    .required()
    .importance(Field.Importance.HIGH)
    .build(),
Field.builder()
    .name("database.port")
    .displayName("Database Port")
    .desc("The port number for the database connection")
    .type(Field.Type.INT)
    .defaultValue(5432)
    .validator(Field.isPositiveIntegerValidator)
    .importance(Field.Importance.MEDIUM)
    .build(),
Field.builder()
    .name("database.timeout")
    .displayName("Connection Timeout")
    .desc("Database connection timeout in seconds")
    .type(Field.Type.INT)
    .defaultValue(30)
    .validator(Field.isNonNegativeIntegerValidator)
    .importance(Field.Importance.LOW)
    .build()
);
```

## Field Validation Framework

### Overview
The Field Validation Framework implements validation through predefined validators that can be used with Field definitions. The framework provides built-in validators for common use cases and allows custom validation implementation.

### Built-in Validators

The framework includes several predefined validators that can be referenced directly in Field definitions:

#### Integer Validators

| Validator | Description | Usage |
|-----------|-------------|-------|
| `isPositiveIntegerValidator` | Validates that value is a positive integer (>0) | `.validator(Field.isPositiveIntegerValidator)` |
| `isNonNegativeIntegerValidator` | Validates that value is non-negative (>=0) | `.validator(Field.isNonNegativeIntegerValidator)` |

#### Long Validators

| Validator | Description | Usage |
|-----------|-------------|-------|
| `isLongValidator` | Validates value can be parsed as Long | `.validator(Field.isLongValidator)` |
| `isPositiveLongValidator` | Validates value is a positive Long | `.validator(Field.isPositiveLongValidator)` |

#### Other Type Validators

| Validator | Description | Usage |
|-----------|-------------|-------|
| `isShortValidator` | Validates Short type values | `.validator(Field.isShortValidator)` |
| `isDoubleValidator` | Validates Double type values | `.validator(Field.isDoubleValidator)` |
| `isRequiredValidator` | Ensures field is not null/empty | `.validator(Field.isRequiredValidator)` |

### Usage Examples

#### Basic Validator Usage
```java
Field DB_PORT_FIELD = Field.builder()
    .name("database.port")
    .type(Field.Type.INT)
    .validator(Field.isPositiveIntegerValidator)
    .build();
```

#### Required Field with Validation
```java
Field DB_TIMEOUT_FIELD = Field.builder()
    .name("database.timeout")
    .type(Field.Type.INT)
    .required()
    .validator(Field.isNonNegativeIntegerValidator)
    .build();
```

### Custom Validation
#### Creating a Custom Validator

To implement custom validation logic, use the `createValidator` method:
```java
Validator customValidator = Field.createValidator(
    new Tuple<>(
        // Startup validation
        (config, field, problems) -> {
            // Validation logic here
            return 0; // Return error count
        },
        // Runtime validation
        value -> {
            // Validation logic here
            return true; // Return validation result
        }
    )
);
```

#### Validator Components
1. **Startup Validation**: 
   * Executed during configuration initialization
   * Returns count of validation errors
   * Access to full configuration context
2. **Runtime Validation**: 
   * Executed during runtime configuration changes
   * Returns boolean validation result
   * Focused on single value validation

### Best Practices
#### Validation Implementation
* Handle null values explicitly
* Provide clear error messages
* Use appropriate type checking

#### Error Handling
* Return accurate error counts 
* Include descriptive error messages 
* Consider validation context

#### Type Safety
* Use type-specific validators
* Implement proper type conversion
* Handle parsing errors gracefully
* Validate within type constraints

### Example Implementation
```java
Field RETRY_COUNT_FIELD = Field.builder()
    .name("connection.retry.count")
    .displayName("Connection Retry Count")
    .desc("Number of connection retry attempts")
    .type(Field.Type.INT)
    .defaultValue(3)
    .validator(Field.isPositiveIntegerValidator)
    .importance(Field.Importance.MEDIUM)
    .build();
```

### Notes
* Validators return `true` for null values by default (except `isRequiredValidator`)
* Combine validators for complex validation rules
* Consider validation performance for frequently changing values