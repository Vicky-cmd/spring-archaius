## TestRunner

This document provides a structured reference for managing configuration metadata in Spring Archaius-based applications. It outlines key field configurations, their default values, validation rules, and lambda-based default generators used within the system. The document helps developers and administrators understand how different application settings are structured, validated, and dynamically generated.

Each configuration entry includes details such as display name, description, type, importance, and validation rules, ensuring clarity in how these configurations impact the application. Additionally, lambda expressions are used for dynamic default values, while validator expressions ensure data integrity.
## Application Details
**Application Name**: TestRunner

Project Maven coordinates for the application are as follows:

**Group Id**: `io.github.vicky-cmd`

**Artifact Id**: `TestRunner`

**Version**: `0.0.1-SNAPSHOT`


## Field Configuration Metadata

| Name | Display Name | Description | Required | Type | Importance | Validated Field | Default Value | Allowed Values |
|------|--------------|-------------|----------|------|------------|-----------------|---------------|----------------|
| application.offers.features | Offer Features | The map containing configuration related to different offer features | Yes | OBJECT | HIGH | No |  |  |
| application.offers.version | Offers Version | Application Offers Version | No | STRING |  | No | Lambda Expression | v1, v2, v3 |
| application.offers.count | Offers Count | Total Offers Count in the Application | Yes | LONG |  | Yes | supplier | new HashSet<>(List.of(45L, 2L, 74L, 14L, 74L)) |
| application.offers.count1 | Offers Count | Total Offers Count in the Application | Yes | LONG |  | Yes | Lambda Expression | new HashSet<>(List.of(45L, 2L, 74L, 14L, 74L)) |
| application.offers.active.codes | Active Offers Codes | The list of offer codes for the active offers in the system | No | STRING |  | No | code1,code2,code3 |  |
| event.manager.controls | Event Manager Controls | The controls for managing the eventmanager application. | Yes | MAP |  | No | Collections::emptyMap | new HashSet<>(List.of(45L, 2L, 74L, 14L, 74L)) |
| version | App Version | The application version | No | STRING |  | No |  |  |


## Lambda Expressions

### Field: application.offers.version

```java
() -> "v2"
```
### Field: application.offers.count1

```java
{
    if (Math.random() > 0.5)
        return 2L;
    return 45L;
}
```


## Validator Expressions

### Field: application.offers.count

#### isLongValidator

Validates if the value is a long. Access via getLong() or Long.class in Configuration.

```java
isLongValidator
```
### Field: application.offers.count1

#### isLongValidator

Validates if the value is a long. Access via getLong() or Long.class in Configuration.

#### isPositiveLongValidator

Validates if the value is a positive long. Access via getLong() or Long.class in Configuration.

```java
isLongValidator, isPositiveLongValidator
```
