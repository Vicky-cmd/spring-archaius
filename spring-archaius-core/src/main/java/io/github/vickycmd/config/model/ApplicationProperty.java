package io.github.vickycmd.config.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a configuration property within an application.
 *
 * <p>This class encapsulates the details of a configuration property,
 * including its unique identifier, key, value, and an optional description.
 * It provides constructors for creating instances with or without a description.
 *
 * <p>Annotations from Lombok are used to automatically generate
 * getter, setter, toString, and no-argument constructor methods.
 *
 * <p>Usage example:
 * <pre>
 *     ApplicationProperty property = new ApplicationProperty("1", "configKey", "configValue");
 *     ApplicationProperty detailedProperty = new ApplicationProperty("2", "anotherKey", "anotherValue", "This is a description");
 * </pre>
 *
 * <p>Fields:
 * <ul>
 *   <li>id - Unique identifier for the property.</li>
 *   <li>key - The key associated with the property.</li>
 *   <li>value - The value of the property.</li>
 *   <li>description - An optional description of the property.</li>
 * </ul>
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class ApplicationProperty {
    private String id;
    private String key;
    private String value;
    private String description;

    /**
     * Constructs an ApplicationProperty with the specified id, key, and value.
     *
     * @param id    the unique identifier for the property
     * @param key   the key associated with the property
     * @param value the value of the property
     */
    public ApplicationProperty(String id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    /**
     * Constructs an ApplicationProperty with the specified id, key, value, and description.
     *
     * @param id          the unique identifier for the property
     * @param key         the key associated with the property
     * @param value       the value of the property
     * @param description an optional description of the property
     */
    public ApplicationProperty(String id, String key, String value, String description) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.description = description;
    }
}
