# Spring Archaius Documentation Generator

## Overview

Spring Archaius Documentation Generator is a command-line tool that automatically generates documentation for Spring Archaius configuration fields. It analyzes your codebase to extract configuration metadata and produces a structured Markdown document detailing all configuration properties, their types, validation rules, and default values.

## Features

- Automatic configuration metadata extraction
- Markdown documentation generation
- Support for multiple source directories
- Custom output directory configuration
- Application name customization
- Maven plugin integration
- Command-line interface
- Field metadata documentation including:
  - Display names
  - Descriptions
  - Types
  - Default values
  - Validation rules
  - Importance levels
  - Required/Optional status
  - Allowed values

## Installation

Download the latest release from the [releases page](https://github.com/vickycmd/spring-archaius/releases).

## Usage

### Command-Line Interface

#### Basic Usage
```bash
archaius-docgen.bat --source-dir ./application1-location 
```

#### Windows
```bash
archaius-docgen.bat \
    --source-dir ./application1-location \
    --output-dir docs \
    --application-name "MyApplication"
```

#### Linux
```sh
archaius-docgen.sh \
    --source-dir ./application1-location \
    --output-dir docs \
    --application-name "MyApplication"
```

### Command Line Arguments

#### Command Options

| Option | Alias | Required | Description |
|--------|-------|----------|-------------|
| `-source` | `-s` | Yes | Source directory containing configuration files |
| `-subdir` | `-sd` | No | Subdirectory within source directory |
| `-appName` | - | No | Custom application name for documentation |
| `-output` | `-o` | No | Output directory for generated documentation |

#### Output Directory Structure

The documentation generator follows these rules for output directory selection:

1. If -output is specified, uses that directory
2. Otherwise, looks for directories in this order:
    - {sourceDir}/src/main/resources
    - {sourceDir}/src
    - {sourceDir}
    - User's home directory
    - Current directory
The generated file is always named archaius-config-report.md.

## Best Practices

### Source Organization

- Keep configuration fields in dedicated classes
- Use consistent naming patterns
- Group related configurations together

### Documentation Quality
- Provide clear descriptions for all fields
- Set appropriate importance levels
- Include validation rules where applicable
- Document allowed values when restricted

### Generation Process
- Run documentation generation as part of your build process
- Version control your configuration documentation
- Review generated documentation for completeness

## Troubleshooting

### Common Issues
- No Fields Found
- Ensure source directory is correct
- Check if fields are properly defined using Field.builder()
- Verify file extensions (.java)

### Missing Metadata
- Add display names and descriptions to fields
- Set importance levels for critical configurations
- Include validation rules where appropriate

### Output Directory Issues
- Ensure write permissions
- Verify directory exists
- Check path separators for your OS

## Contributing

We welcome contributions! Please fork the repository and submit a pull request.

