# Spring Archaius BOM (Bill of Materials)

## Overview

The Spring Archaius BOM (Bill of Materials) provides a centralized dependency management for all Spring Archaius modules. Using the BOM helps ensure that all Spring Archaius dependencies are at consistent versions, reducing the risk of version conflicts and incompatibilities.

## Usage

### Adding the BOM

Add the Spring Archaius BOM to your project's `pom.xml` file in the `dependencyManagement` section:

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

### Using Spring Archaius Modules

Once you've imported the BOM, you can add Spring Archaius dependencies without specifying versions:

```xml
<dependencies>
    <!-- Core Module -->
    <dependency>
        <groupId>io.github.vickycmd</groupId>
        <artifactId>spring-archaius-core</artifactId>
    </dependency>
    <!-- Documentation Generation (Optional) -->
    <dependency>
        <groupId>io.github.vickycmd</groupId>
        <artifactId>spring-archaius-docgen</artifactId>
        <optional>true</optional>
    </dependency>
    <!-- Plugin Support (Optional) -->
    <dependency>
        <groupId>io.github.vickycmd</groupId>
        <artifactId>spring-archaius-plugin</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Version Management

### Available Versions

| Version | Release Date | Spring Boot Compatibility |
|---------|--------------|---------------------------|
| 0.0.1   | 2024-XX-XX   | 2.7.x, 3.x.x              |

### Version Selection

- For production use, always use the latest stable version
- Development versions (0.x.x) may contain breaking changes
- Each version maintains compatibility with specific Spring Boot versions

## Best Practices

1. **Always Use the BOM**
   - Ensures consistent versions across modules
   - Simplifies dependency management
   - Reduces potential version conflicts

2. **Version Updates**
   - Update the BOM version rather than individual module versions
   - Test thoroughly after version updates
   - Review changelog for breaking changes

3. **Optional Dependencies**
   - Mark non-core modules as `<optional>true</optional>`
   - Only include modules you actually need
   - Consider dependency size in your final artifact

## Example Project Setup

Complete `pom.xml` example with Spring Boot and Spring Archaius:


```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>my-application</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
        <spring-archaius.version>1.0.0</spring-archaius.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <!-- Spring Archaius BOM -->
            <dependency>
                <groupId>io.github.vickycmd</groupId>
                <artifactId>spring-archaius-bom</artifactId>
                <version>${spring-archaius.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <!-- Spring Archaius Core -->
        <dependency>
            <groupId>io.github.vickycmd</groupId>
            <artifactId>spring-archaius-core</artifactId>
        </dependency>
    </dependencies>
</project>
```


## Troubleshooting

### Common Issues

1. **Version Conflicts**
   - Symptom: ClassNotFoundException or NoSuchMethodError
   - Solution: Ensure no explicit versions override BOM versions
   - Check for transitive dependencies with `mvn dependency:tree`

2. **Missing Dependencies**
   - Symptom: ClassNotFoundException for optional features
   - Solution: Add required optional dependencies
   - Verify scope and optional flags

3. **Incompatible Versions**
   - Symptom: Runtime errors or Spring context failures
   - Solution: Verify Spring Boot compatibility
   - Update to compatible versions

### Support

For issues and questions:
- GitHub Issues: [Spring Archaius Issues](https://github.com/vickycmd/spring-archaius/issues)
- Documentation: [Spring Archaius Docs](https://github.com/vickycmd/spring-archaius/docs)

