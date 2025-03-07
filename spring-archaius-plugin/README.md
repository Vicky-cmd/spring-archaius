# Spring Archaius Maven Plugin

## Overview

The Spring Archaius Maven Plugin automates the generation of configuration documentation for Spring Archaius-based applications. It scans your project's source code for configuration field definitions and generates comprehensive Markdown documentation detailing all configuration properties, their types, validation rules, and default values.

## Features

- Automatic configuration metadata extraction during build
- Maven lifecycle integration
- Customizable source and output directories
- Support for multi-module projects
- Application name customization
- Markdown documentation generation
- Detailed field metadata documentation including:
  - Display names and descriptions
  - Types and validation rules
  - Default values and allowed values
  - Importance levels
  - Required/Optional status

## Installation

### Using BOM (Recommended)

First, add the Spring Archaius BOM to your project:

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


### Plugin Configuration

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
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
            <configuration>
                <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
                <sourceSubDirectory>com/example/config</sourceSubDirectory>
                <outputDirectory>${project.build.directory}/docs</outputDirectory>
                <applicationName>My Application</applicationName>
            </configuration>
        </plugin>
    </plugins>
</build>
```


## Plugin Goals

### generate-doc

Generates documentation for configuration fields.

#### Configuration Parameters

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| sourceDirectory | Yes | ${project.basedir} | Base directory containing source files |
| sourceSubDirectory | No | "" | Subdirectory within source directory |
| outputDirectory | No | Auto-detected | Output directory for generated documentation |
| applicationName | No | ${project.name} | Custom application name for documentation |

## Usage Examples

### Basic Usage

Minimal configuration using defaults:
```xml
<plugin>
    <groupId>io.github.vickycmd</groupId>
    <artifactId>spring-archaius-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-doc</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Custom Configuration

Full configuration with all options:

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
                <sourceSubDirectory>com/example/config</sourceSubDirectory>
                <outputDirectory>${project.build.directory}/docs</outputDirectory>
                <applicationName>My Custom Application</applicationName>
            </configuration>
        </execution>
    </executions>
</plugin>
```


### Multi-Module Project Example

For a multi-module project, you might want to generate documentation for specific modules:

```xml
<!-- Parent pom.xml -->
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>io.github.vickycmd</groupId>
                <artifactId>spring-archaius-plugin</artifactId>
                <version>1.0.0</version>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
<!-- Module pom.xml -->
<build>
    <plugins>
        <plugin>
            <groupId>io.github.vickycmd</groupId>
            <artifactId>spring-archaius-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>generate-doc</goal>
                    </goals>
                    <configuration>
                        <sourceSubDirectory>config</sourceSubDirectory>
                        <outputDirectory>${project.build.directory}/config-docs</outputDirectory>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```


## Example Configuration Fields

The plugin will document configuration fields defined using the Field builder pattern:

```java
java
Field databaseConfig = Field.builder()
    .name("database.url")
    .displayName("Database URL")
    .desc("The connection URL for the database")
    .type(Field.Type.STRING)
    .required()
    .importance(Field.Importance.HIGH)
    .defaultValue("jdbc:mysql://localhost:3306/mydb")
    .build();

Field featureToggle = Field.builder()
    .name("feature.enabled")
    .displayName("Feature Toggle")
    .desc("Enable/disable the feature")
    .type(Field.Type.BOOLEAN)
    .defaultValue(false)
    .importance(Field.Importance.MEDIUM)
    .build();
```


## Generated Documentation

The plugin generates a Markdown file (`archaius-config-report.md`) containing:

1. Application Overview
2. Field Configuration Metadata Table
3. Lambda Expressions (if any)
4. Validation Rules (if any)

Example output:

```markdown
# My Application

This document provides a structured reference for managing configuration metadata...

## Field Configuration Metadata
| Name | Display Name | Description | Required | Type | Importance | Validated Field | Default Value | Allowed Values |
|------|--------------|-------------|----------|------|------------|-----------------|---------------|----------------|
| database.url | Database URL | The connection URL... | Yes | STRING | HIGH | No | jdbc:mysql://... | |
| feature.enabled | Feature Toggle | Enable/disable... | No | BOOLEAN | MEDIUM | No | false | |
```

## Command Line Usage

You can also run the plugin directly from the command line. For the detailed documentation, please refer to the [spring-archaius-docgen](https://github.com/vickycmd/spring-archaius/tree/main/spring-archaius-docgen) [README](https://github.com/vickycmd/spring-archaius/tree/main/spring-archaius-docgen/README.md).


## Best Practices

### 1. Source Organization
- Keep configuration fields in dedicated classes
- Use consistent naming patterns
- Group related configurations together

### 2. Documentation Quality
- Provide clear descriptions for all fields
- Set appropriate importance levels
- Include validation rules where applicable
- Document allowed values when restricted

### 3. Build Integration
- Run documentation generation as part of your build process
- Version control your configuration documentation
- Review generated documentation for completeness

## Troubleshooting

### Common Issues

1. **No Fields Found**
   - Verify source directory configuration
   - Ensure fields are defined using Field.builder()
   - Check file extensions (.java)

2. **Missing Metadata**
   - Add display names and descriptions
   - Set importance levels
   - Include validation rules

3. **Output Directory Issues**
   - Verify write permissions
   - Check directory exists
   - Validate path separators for OS

## Support

For issues and questions:
- GitHub Issues: [Spring Archaius Issues](https://github.com/vickycmd/spring-archaius/issues)
- Documentation: [Spring Archaius Docs](https://github.com/vickycmd/spring-archaius/docs)