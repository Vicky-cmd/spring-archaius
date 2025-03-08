package io.github.vickycmd.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.fields.Field;
import io.vavr.control.Try;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.*;

/**
 * The Configuration class provides a mechanism to manage application configuration properties.
 * It supports retrieving configuration values with optional default values and type safety.
 * The class integrates with the Spring Environment to fetch properties and uses Jackson's
 * ObjectMapper for JSON processing.
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Retrieving properties with specified types, including String, Integer, Boolean, etc.</li>
 *   <li>Support for default values using {@link java.util.function.Supplier}.</li>
 *   <li>Validation of fields using the {@link io.github.vickycmd.config.ValidateField} annotation.</li>
 *   <li>Handling of complex types like {@link java.util.List} and {@link java.util.Map} with JSON conversion.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Configuration config = new Configuration(environment);
 * String value = config.getString("property.name");
 * Integer number = config.getInteger("property.number", 42);
 * }
 * </pre>
 *
 * <p>This class works in conjunction with:</p>
 * <ul>
 *   <li>{@link io.github.vickycmd.config.fields.Field} - For field definitions and validation</li>
 *   <li>{@link io.github.vickycmd.config.errors.ConfigException} - For configuration related errors</li>
 * </ul>
 *
 * <p>Note: This class is annotated with @Component and @Scope("prototype") for Spring integration.</p>
 *
 * @author Vicky CMD
 * @version 1.0
 * @see org.springframework.core.env.Environment
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see io.github.vickycmd.config.fields.Field
 * @since 1.0
 */
@Component
@Scope("prototype")
public class Configuration {


   private final Environment environment;

    private final HashMap<String, Object> defaultsMap;

    private final ObjectMapper mapper;

    /**
     * Constructs a Configuration instance with the specified Spring Environment.
     * Initializes the defaults map and configures the ObjectMapper to ignore unknown properties
     * during JSON deserialization.
     *
     * @param environment the Spring Environment used to fetch configuration properties
     */
    @Autowired
    public Configuration(Environment environment) {
        this.environment = environment;
        this.defaultsMap = new HashMap<>();
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Adds a default value for the specified property name.
     *
     * @param name the property name
     * @param value the default value
     * @return the Configuration instance
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment)
     *     .withDefault("property.name", "default value")
     *     .withDefault("property.number", 42)
     *     .withDefault("property.list", Arrays.asList("a", "b", "c"));
     * }
     * </pre>
     */
    public Configuration withDefault(String name, Object value) {
        this.defaultsMap.put(name, value);
        return this;
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method.
     *
     * @param property   the name of the configuration property
     * @param targetType the expected type of the property value
     * @param <T>        the type of the property value
     * @return the value of the property, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * String value = config.get("property.name", String.class);
     * Integer number = config.get("property.number", Integer.class);
     * }
     * </pre>
     */
    public <T> T get(String property, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> this.getDefaultValue(property, targetType), targetType);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the expected type of the property value
     * @param <T>         the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * String value = config.get("property.name", "default value", String.class);
     * Integer number = config.get("property.number", 42, Integer.class);
     * }
     * </pre>
     */
    public <T> T get(String property, T defaultValue, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param targetType         the expected type of the property value
     * @param <T>                the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * String value = config.get("property.name", () -> "default value", String.class);
     * Integer number = config.get("property.number", () -> 42, Integer.class);
     * }
     * </pre>
     */
    public <T> T get(String property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.environment.getProperty(property, targetType, defaultValueSupplier.get());
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, a default value is supplied if configured for the
     * property using the withDefault method or from the Field.
     *
     * @param property   the {@link Field} of the configuration property
     * @param targetType the expected type of the property value
     * @param <T>        the type of the property value
     * @return the value of the property, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field propertyField = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .build();
     * String value = config.get(propertyField, String.class);
     * Integer number = config.get(propertyField, Integer.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> T get(Field property, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType), targetType);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the expected type of the property value
     * @param <T>         the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field propertyField = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .build();
     * String value = config.get(propertyField, "default value", String.class);
     * Integer number = config.get(propertyField, 42, Integer.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> T get(Field property, T defaultValue, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param targetType         the expected type of the property value
     * @param <T>                the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field propertyField = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .build();
     * String value = config.get(propertyField, () -> "default value", String.class);
     * Integer number = config.get(propertyField, () -> 42, Integer.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> T get(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.get(property.name(),
                defaultValueSupplier.get(), targetType);
    }

    /**
     * Retrieves the value of a configuration property as an Optional.
     * If the property is not found, an empty Optional is returned.
     *
     * @param property   the name of the configuration property
     * @param targetType the expected type of the property value
     * @param <T>        the type of the property value
     * @return an {@link Optional} containing the value of the property, or an empty Optional if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Optional<String> value = config.getOptional("property.name", String.class);
     * Optional<Integer> number = config.getOptional("property.number", Integer.class);
     * }
     * </pre>
     */
    public <T> Optional<T> getOptional(String property, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, targetType));
    }

    /**
     * Retrieves the value of a configuration property as an Optional.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the expected type of the property value
     * @param <T>         the type of the property value
     * @return an {@link Optional} containing the value of the property, or an empty Optional if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Optional<String> value = config.getOptional("property.name", "default value", String.class);
     * Optional<Integer> number = config.getOptional("property.number", 42, Integer.class);
     * }
     * </pre>
     */
    public <T> Optional<T> getOptional(String property, T defaultValue, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValue, targetType));
    }

    /**
     * Retrieves the value of a configuration property as an Optional.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param targetType         the expected type of the property value
     * @param <T>                the type of the property value
     * @return an {@link Optional} containing the value of the property, or an empty Optional if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Optional<String> value = config.getOptional("property.name", () -> "default value", String.class);
     * Optional<Integer> number = config.getOptional("property.number", () -> 42, Integer.class);
     * }
     * </pre>
     */
    public <T> Optional<T> getOptional(String property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValueSupplier, targetType));
    }

    /**
     * Retrieves the value of a configuration property as an Optional.
     * If the property is not found, an empty Optional is returned.
     *
     * @param property   the {@link Field} of the configuration property
     * @param targetType the expected type of the property value
     * @param <T>        the type of the property value
     * @return an {@link Optional} containing the value of the property, or an empty Optional if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field propertyField = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .build();
     * Optional<String> value = config.getOptional(propertyField, String.class);
     * Optional<Integer> number = config.getOptional(propertyField, Integer.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> Optional<T> getOptional(Field property, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, targetType));
    }

    /**
     * Retrieves the value of a configuration property as an Optional.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the expected type of the property value
     * @param <T>         the type of the property value
     * @return an {@link Optional} containing the value of the property, or an empty Optional if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field propertyField = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .build();
     * Optional<String> value = config.getOptional(propertyField, "default value", String.class);
     * Optional<Integer> number = config.getOptional(propertyField, 42, Integer.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> Optional<T> getOptional(Field property, T defaultValue, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValue, targetType));
    }

    /**
     * Retrieves the value of a configuration property as an Optional.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param targetType         the expected type of the property value
     * @param <T>                the type of the property value
     * @return an {@link Optional} containing the value of the property, or an empty Optional if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field propertyField = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .build();
     * Optional<String> value = config.getOptional(propertyField, () -> "default value", String.class);
     * Optional<Integer> number = config.getOptional(propertyField, () -> 42, Integer.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> Optional<T> getOptional(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValueSupplier, targetType));
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValueWithArgs method.
     *
     * @param property   the name of the configuration property
     * @param targetType the expected type of the property value
     * @param args       optional arguments used to format the property name
     *                   using String.format. These arguments help in generating
     *                   dynamic property keys.
     * @param <T>        the type of the property value
     * @return the value of the property, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming a property key pattern like "server.port.%s"
     * Integer port = config.get("server.port.%s", Integer.class, "dev");
     * // This will look for the property "server.port.dev"
     * }
     * </pre>
     */
    public <T> T get(String property, Class<T> targetType, String ...args) {
        return this.get(property, (Supplier<T>) () -> this.getDefaultValueWithArgs(property, targetType, args), targetType, args);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the expected type of the property value
     * @param args        optional arguments used to format the property name
     *                    using String.format. These arguments help in generating
     *                    dynamic property keys.
     * @param <T>         the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming a property key pattern like "server.port.%s"
     * Integer port = config.get("server.port.%s", 10, Integer.class, "dev");
     * // This will look for the property "server.port.dev"
     * }
     * </pre>
     */
    public <T> T get(String property, T defaultValue, Class<T> targetType, String ...args) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType, args);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param targetType         the expected type of the property value
     * @param args               optional arguments used to format the property name
     *                           using String.format. These arguments help in generating
     *                           dynamic property keys.
     * @param <T>                the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming a property key pattern like "server.port.%s"
     * Integer port = config.get("server.port.%s", () -> 10, Integer.class, "dev");
     * // This will look for the property "server.port.dev"
     * }
     * </pre>
     */
    public <T> T get(String property, Supplier<T> defaultValueSupplier, Class<T> targetType, String ...args) {
        return this.get(String.format(property, (Object[]) args), defaultValueSupplier, targetType);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValueWithArgs method.
     *
     * @param property   the {@link Field} of the configuration property
     * @param targetType the expected type of the property value
     * @param args       optional arguments used to format the property name
     *                   using String.format. These arguments help in generating
     *                   dynamic property keys.
     * @param <T>        the type of the property value
     * @return the value of the property, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     * <pre>
     * {@code
     * // Assuming a property key pattern like "server.port.%s"
     * Field serverPortField = Field.builder()
     *      .name("server.port.%s")
     *      .type(Field.Type.INTEGER)
     *      .build()
     * Integer port = config.get(serverPortField, Integer.class, "dev");
     * // This will look for the property "server.port.dev"
     * }
     * </pre>
     */
    @ValidateField
    public <T> T get(Field property, Class<T> targetType, String ...args) {
        return this.get(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType, args), targetType, args);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the expected type of the property value
     * @param args        optional arguments used to format the property name
     *                    using String.format. These arguments help in generating
     *                    dynamic property keys.
     * @param <T>         the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <pre>
     * {@code
     * // Assuming a property key pattern like "server.port.%s"
     * Field serverPortField = Field.builder()
     *      .name("server.port.%s")
     *      .type(Field.Type.INTEGER)
     *      .build()
     * Integer port = config.get(serverPortField, 10, Integer.class, "dev");
     * // This will look for the property "server.port.dev"
     * }
     * </pre>
     */
    @ValidateField
    public <T> T get(Field property, T defaultValue, Class<T> targetType, String ...args) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType, args);
    }

    /**
     * Retrieves the value of a configuration property with the specified type.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param targetType         the expected type of the property value
     * @param args               optional arguments used to format the property name
     *                           using String.format. These arguments help in generating
     *                           dynamic property keys.
     * @param <T>                the type of the property value
     * @return the value of the property, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <pre>
     * {@code
     * // Assuming a property key pattern like "server.port.%s"
     * Field serverPortField = Field.builder()
     *      .name("server.port.%s")
     *      .type(Field.Type.INTEGER)
     *      .build()
     * Integer port = config.get(serverPortField, () -> 10, Integer.class, "dev");
     * // This will look for the property "server.port.dev"
     * }
     * </pre>
     */
    @ValidateField
    public <T> T get(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType, String ...args) {
        return this.get(property.name(),
                defaultValueSupplier.get(), targetType, args);
    }

    /**
     * Retrieves the value of a configuration property as a String.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the String type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a String, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String value = config.getString("property.name");
     * }
     * </pre>
     */
    public String getString(String property) {
        return this.getString(property, () -> this.getDefaultValue(property, String.class));
    }

    /**
     * Retrieves the value of a configuration property as a String.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a String, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String value = config.getString("property.name", "default value");
     * }
     * </pre>
     */
    public String getString(String property, String defaultValue) {
        return this.getString(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a String.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a String, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String value = config.getString("property.name", () -> "default value");
     * }
     * </pre>
     */
    public String getString(String property, Supplier<String> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, String.class);
    }

    /**
     * Retrieves the value of a configuration property as a String.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the String type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a String, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.STRING)
     *      .build()
     * String value = config.getString(property);
     * }
     * </pre>
     */
    @ValidateField
    public String getString(Field property) {
        return this.getString(property, () -> this.getDefaultValue(property.name(), property.defaultValueAsString(), String.class));
    }

    /**
     * Retrieves the value of a configuration property as a String.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a String, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.STRING)
     *      .build()
     * String value = config.getString(property, "default value");
     * }
     * </pre>
     */
    @ValidateField
    public String getString(Field property, String defaultValue) {
        return this.getString(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a String.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a String, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.STRING)
     *      .build()
     * String value = config.getString(property, () -> "default value");
     * }
     * </pre>
     */
    @ValidateField
    public String getString(Field property, Supplier<String> defaultValueSupplier) {
        return this.getString(property.name(), defaultValueSupplier.get());
    }

    /**
     * Retrieves the value of a configuration property as an Integer.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Integer type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as an Integer, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Integer value = config.getInteger("property.name");
     * }
     * </pre>
     */
    public Integer getInteger(String property) {
        return this.getInteger(property, () -> this.getDefaultValue(property, Integer.class));
    }

    /**
     * Retrieves the value of a configuration property as an Integer.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as an Integer, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Integer value = config.getInteger("property.name", 10);
     * }
     * </pre>
     */
    public Integer getInteger(String property, Integer defaultValue) {
        return this.getInteger(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as an Integer.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as an Integer, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Integer value = config.getInteger("property.name", () -> 10);
     * }
     * </pre>
     */
    public Integer getInteger(String property, Supplier<Integer> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Integer.class);
    }

    /**
     * Retrieves the value of a configuration property as an Integer.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Integer type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as an Integer, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.INTEGER)
     *      .build()
     * Integer value = config.getInteger(property);
     * }
     * </pre>
     */
    @ValidateField
    public Integer getInteger(Field property) {
        return this.getInteger(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Integer.class));
    }

    /**
     * Retrieves the value of a configuration property as an Integer.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as an Integer, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.INTEGER)
     *      .build()
     * Integer value = config.getInteger(property, 10);
     * }
     * </pre>
     */
    @ValidateField
    public Integer getInteger(Field property, Integer defaultValue) {
        return this.getInteger(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as an Integer.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as an Integer, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.INTEGER)
     *      .build()
     * Integer value = config.getInteger(property, () -> 10);
     * }
     * </pre>
     */
    @ValidateField
    public Integer getInteger(Field property, IntSupplier defaultValueSupplier) {
        return this.getInteger(property.name(),
                        defaultValueSupplier.getAsInt());
    }

    /**
     * Retrieves the value of a configuration property as a Short.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Short type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a Short, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Short value = config.getShort("property.name");
     * }
     * </pre>
     */
    public Short getShort(String property) {
        return this.getShort(property, () -> this.getDefaultValue(property, Short.class));
    }

    /**
     * Retrieves the value of a configuration property as a Short.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Short, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Short value = config.getShort("property.name", (short) 10);
     * }
     * </pre>
     */
    public Short getShort(String property, Short defaultValue) {
        return this.getShort(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Short.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Short, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Short value = config.getShort("property.name", () -> (short) 10);
     * }
     * </pre>
     */
    public Short getShort(String property, Supplier<Short> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Short.class);
    }

    /**
     * Retrieves the value of a configuration property as a Short.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Short type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a Short, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.SHORT)
     *      .build()
     * Short value = config.getShort(property);
     * }
     * </pre>
     */
    @ValidateField
    public Short getShort(Field property) {
        return this.getShort(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Short.class));
    }

    /**
     * Retrieves the value of a configuration property as a Short.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Short, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.SHORT)
     *      .build()
     * Short value = config.getShort(property, (short) 10);
     * }
     * </pre>
     */
    @ValidateField
    public Short getShort(Field property, Short defaultValue) {
        return this.getShort(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Short.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Short, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.SHORT)
     *      .build()
     * Short value = config.getShort(property, () -> (short) 10);
     * }
     * </pre>
     */
    @ValidateField
    public Short getShort(Field property, Supplier<Short> defaultValueSupplier) {
        return this.getShort(property.name(),
                defaultValueSupplier.get());
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Long type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a Long, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Long value = config.getLong("property.name");
     * }
     * </pre>
     */
    public Boolean getBoolean(String property) {
        return this.getBoolean(property, () -> this.getDefaultValue(property, Boolean.class));
    }

    /**
     * Retrieves the value of a configuration property as a Boolean.
     * If the property is not found, the default value provided is returned.
     *
     * @param property the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Boolean, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Boolean value = config.getBoolean("property.name", false);
     * }
     * </pre>
     */
    public Boolean getBoolean(String property, Boolean defaultValue) {
        return this.getBoolean(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Boolean.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Boolean, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Boolean value = config.getBoolean("property.name", () -> false);
     * }
     * </pre>
     */
    public Boolean getBoolean(String property, Supplier<Boolean> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Boolean.class);
    }

    /**
     * Retrieves the value of a configuration property as a Boolean.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Boolean type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a Boolean, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.BOOLEAN)
     *      .build()
     * Boolean value = config.getBoolean(property);
     * }
     * </pre>
     */
    @ValidateField
    public Boolean getBoolean(Field property) {
        return this.getBoolean(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Boolean.class));
    }

    /**
     * Retrieves the value of a configuration property as a Boolean.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Boolean, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.BOOLEAN)
     *      .build()
     * Boolean value = config.getBoolean(property, false);
     * }
     * </pre>
     */
    @ValidateField
    public Boolean getBoolean(Field property, Boolean defaultValue) {
        return this.getBoolean(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Boolean.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Boolean, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.BOOLEAN)
     *      .build()
     * Boolean value = config.getBoolean(property, () -> false);
     * }
     * </pre>
     */
    @ValidateField
    public Boolean getBoolean(Field property, BooleanSupplier defaultValueSupplier) {
        return this.getBoolean(property.name(),
                defaultValueSupplier.getAsBoolean());
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Long type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a Long, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Long value = config.getLong("property.name");
     * }
     * </pre>
     */
    public Long getLong(String property) {
        return this.getLong(property, () -> this.getDefaultValue(property, Long.class));
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Long, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Long value = config.getLong("property.name", 10L);
     * }
     * </pre>
     */
    public Long getLong(String property, Long defaultValue) {
        return this.getLong(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Long, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Long value = config.getLong("property.name", () -> 10L);
     * }
     * </pre>
     */
    public Long getLong(String property, Supplier<Long> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Long.class);
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Long type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a Long, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.LONG)
     *      .build()
     * Long value = config.getLong(property);
     * }
     * </pre>
     */
    @ValidateField
    public Long getLong(Field property) {
        return this.getLong(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Long.class));
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Long, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.LONG)
     *      .build()
     * Long value = config.getLong(property, 10L);
     * }
     * </pre>
     */
    @ValidateField
    public Long getLong(Field property, Long defaultValue) {
        return this.getLong(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Long.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Long, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.LONG)
     *      .build()
     * Long value = config.getLong(property, () -> 10L);
     * }
     * </pre>
     */
    @ValidateField
    public Long getLong(Field property, LongSupplier defaultValueSupplier) {
        return this.getLong(property.name(),
                defaultValueSupplier.getAsLong());
    }

    /**
     * Retrieves the value of a configuration property as a Float.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Float type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a Float, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Float value = config.getFloat("property.name");
     * }
     * </pre>
     */
    public Float getFloat(String property) {
        return this.getFloat(property, () -> this.getDefaultValue(property, Float.class));
    }

    /**
     * Retrieves the value of a configuration property as a Float.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Float, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Float value = config.getFloat("property.name", 10.0f);
     * }
     * </pre>
     */
    public Float getFloat(String property, Float defaultValue) {
        return this.getFloat(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Float.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Float, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Float value = config.getFloat("property.name", () -> 10.0f);
     * }
     * </pre>
     */
    public Float getFloat(String property, Supplier<Float> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Float.class);
    }

    /**
     * Retrieves the value of a configuration property as a Float.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Float type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a Float, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.FLOAT)
     *      .build()
     * Float value = config.getFloat(property);
     * }
     * </pre>
     */
    @ValidateField
    public Float getFloat(Field property) {
        return this.getFloat(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Float.class));
    }

    /**
     * Retrieves the value of a configuration property as a Float.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Float, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.FLOAT)
     *      .build()
     * Float value = config.getFloat(property, 10.0f);
     * }
     * </pre>
     */
    @ValidateField
    public Float getFloat(Field property, Float defaultValue) {
        return this.getFloat(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Float.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Float, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.FLOAT)
     *      .build()
     * Float value = config.getFloat(property, () -> 10.0f);
     * }
     * </pre>
     */
    @ValidateField
    public Float getFloat(Field property, Supplier<Float> defaultValueSupplier) {
        return this.getFloat(property.name(),
                defaultValueSupplier.get());
    }

    /**
     * Retrieves the value of a configuration property as a Double.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Double type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a Double, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Double value = config.getDouble("property.name");
     * }
     * </pre>
     */
    public Double getDouble(String property) {
        return this.getDouble(property, () -> this.getDefaultValue(property, Double.class));
    }

    /**
     * Retrieves the value of a configuration property as a Double.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Double, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Double value = config.getDouble("property.name", 10.0);
     * }
     * </pre>
     */
    public Double getDouble(String property, Double defaultValue) {
        return this.getDouble(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Double.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Double, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Double value = config.getDouble("property.name", () -> 10.0);
     * }
     * </pre>
     */
    public Double getDouble(String property, Supplier<Double> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Double.class);
    }

    /**
     * Retrieves the value of a configuration property as a Double.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Double type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a Double, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.DOUBLE)
     *      .build()
     * Double value = config.getDouble(property);
     * }
     * </pre>
     */
    @ValidateField
    public Double getDouble(Field property) {
        return this.getDouble(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Double.class));
    }

    /**
     * Retrieves the value of a configuration property as a Double.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Double, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.DOUBLE)
     *      .build()
     * Double value = config.getDouble(property, 10.0);
     * }
     * </pre>
     */
    @ValidateField
    public Double getDouble(Field property, Double defaultValue) {
        return this.getDouble(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Double.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Double, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.DOUBLE)
     *      .build()
     * Double value = config.getDouble(property, () -> 10.0);
     * }
     * </pre>
     */
    @ValidateField
    public Double getDouble(Field property, DoubleSupplier defaultValueSupplier) {
        return this.getDouble(property.name(),
                defaultValueSupplier.getAsDouble());
    }

    /**
     * Retrieves the value of a configuration property as a List of Strings.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the List type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a List of Strings, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<String> value = config.getList("property.name");
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public List<String> getList(String property) {
        return this.getList(property, () -> this.getDefaultValue(property, List.class));
    }

    /**
     * Retrieves the value of a configuration property as a List of Strings.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the List type.
     *
     * @param property   the name of the configuration property
     * @param targetType the target type of the list elements
     * @param <T>        the type of the property value
     * @return           the value of the property as a List of Strings, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<Integer> value = config.getList("property.name", Integer.class);
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String property, Class<T> targetType) {
        return this.getList(property, () -> this.getDefaultValue(property, List.class), targetType);
    }

    /**
     * Retrieves the value of a configuration property as a List of Strings.
     * If the property is not found, the specified default value is returned.
     *
     * @param property     the name of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @param <T>          the type of the property value
     * @return the value of the property as a List of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<String> value = config.getList("property.name", Arrays.asList("value1", "value2"));
     * }
     * </pre>
     */
    public <T> List<T> getList(String property, List<T> defaultValue) {
        return this.getList(property, () -> defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a List of Strings.
     * If the property is not found, the default value is returned
     *
     * @param property           the name of the configuration property
     * @param defaultValue       the default value to return if the property is not found
     * @param targetType         the target type of the list elements
     * @param <T>                the type of the property value
     * @return the value of the property as a List of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<Integer> value = config.getList("property.name", Arrays.asList(1, 2, 3), Integer.class);
     * }
     * </pre>
     */
    public <T> List<T> getList(String property, List<T> defaultValue, Class<T> targetType) {
        return this.getList(property, () -> defaultValue, targetType);
    }

    /**
     * Retrieves the value of a configuration property as a List of Strings.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property             the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @param <T>                  the type of the property value
     * @return the value of the property as a List of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<String> value = config.getList("property.name", () -> Arrays.asList("value1", "value2"));
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String property, Supplier<List<T>> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier.get(), List.class);
    }

    /**
     * Retrieves a list of objects of the specified type from the configuration
     * based on the given property key. If the property is not found, the
     * default value provided by the supplier is returned.
     *
     * @param property the key of the property to retrieve
     * @param defaultValueSupplier a supplier that provides a default list if the property is not found
     * @param targetType the class type of the objects in the list
     * @param <T> the type of objects in the list
     * @return a list of objects of the specified type
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<Integer> value = config.getList("property.name", () -> Arrays.asList(1, 2, 3), Integer.class);
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String property, Supplier<List<T>> defaultValueSupplier, Class<T> targetType) {
        return this.getOptional(property, defaultValueSupplier.get(), List.class)
                .map(objectList -> objectList.stream().map(entry -> mapper.convertValue(entry, targetType)).toList())
                .orElse(Collections.emptyList());

    }

    /**
     * Retrieves the value of a configuration property as a List of Strings.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the List type.
     *
     * @param property the {@link Field} of the configuration property
     * @param <T>      the type of the property value
     * @return the value of the property as a List of Strings, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.LIST)
     *      .build()
     * List<String> value = config.getList(property);
     * }
     * </pre>
     */
    @ValidateField
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(Field property) {
        return this.getList(property, () -> this.getDefaultValue(property.name(), property.defaultValue(), List.class));
    }

    /**
     * Retrieves a list of values associated with the specified field.
     * If the field is not found or is null, the provided default value is returned.
     *
     * @param <T> The type of elements in the list.
     * @param property The field whose associated list is to be returned.
     * @param defaultValue The default list to return if the field is not found or is null.
     * @return The list of values associated with the specified field, or the default value if not found.
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.LIST)
     *      .build()
     * List<String> value = config.getList(property, Arrays.asList("value1", "value2"));
     * }
     * </pre>
     */
    @ValidateField
    public <T> List<T>  getList(Field property, List<T>  defaultValue) {
        return this.getList(property, () -> defaultValue);
    }

    /**
     * Retrieves a list of values associated with the specified field.
     * If the field is not found or is null, the provided default value is returned.
     *
     * @param <T> The type of elements in the list.
     * @param property The field whose associated list is to be returned.
     * @param defaultValueSupplier The supplier of the default list to return if the field is not found or is null.
     * @return The list of values associated with the specified field, or the default value if not found.
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.LIST)
     *      .build()
     * List<String> value = config.getList(property, () -> Arrays.asList("value1", "value2"));
     * }
     * </pre>
     */
    @ValidateField
    @SuppressWarnings("unchecked")
    public <T> List<T>  getList(Field property, Supplier<List<T>> defaultValueSupplier) {
        if (property.className()!=null)
            return this.getList(property.name(), defaultValueSupplier.get(), (Class<T>) property.className());
        return this.getList(property.name(),
                defaultValueSupplier.get());
    }

    /**
     * Retrieves the value of a configuration property as a Map of Strings.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Map type.
     *
     * @param property the name of the configuration property
     * @return the value of the property as a Map of Strings, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Map<String, Object> value = config.getMap("property.name");
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String property) {
        return this.getMap(property, () -> this.getDefaultValue(property, Map.class));
    }

    /**
     * Retrieves the value of a configuration property as a Map of Strings.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Map type.
     *
     * @param property the name of the configuration property
     * @param defaultMap the default value to return if the property is not found
     * @return the value of the property as a Map of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Map<String, Object> value = config.getMap("property.name", new HashMap<>());
     * }
     * </pre>
     */
    public Map<String, Object> getMap(String property, Map<String, Object> defaultMap) {
        return this.getOptional(property, String.class).map(value -> new JSONObject(value).toMap())
                .orElse(defaultMap);
    }

    /**
     * Retrieves the value of a configuration property as a Map of Strings.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the name of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Map of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Map<String, Object> value = config.getMap("property.name", () -> new HashMap<>());
     * }
     * </pre>
     */
    public Map<String, Object> getMap(String property, Supplier<Map<String, Object>> defaultValueSupplier) {
        return this.getOptional(property, String.class).map(value -> new JSONObject(value).toMap())
                .orElse(defaultValueSupplier.get());
    }

    /**
     * Retrieves the value of a configuration property as a Map of Strings.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Map type.
     *
     * @param property the {@link Field} of the configuration property
     * @return the value of the property as a Map of Strings, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.MAP)
     *      .build()
     * Map<String, Object> value = config.getMap(property);
     * }
     * </pre>
     */
    @ValidateField
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(Field property) {
        return this.getMap(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Map.class));
    }

    /**
     * Retrieves the value of a configuration property as a Map of Strings.
     * If the property is not found, the specified default value is returned.
     *
     * @param property    the {@link Field} of the configuration property
     * @param defaultValue the default value to return if the property is not found
     * @return the value of the property as a Map of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.MAP)
     *      .build()
     * Map<String, Object> value = config.getMap(property, new HashMap<>());
     * }
     * </pre>
     */
    @ValidateField
    public Map<String, Object> getMap(Field property, Map<String, Object> defaultValue) {
        return this.getMap(property.name(), defaultValue);
    }

    /**
     * Retrieves the value of a configuration property as a Map of Strings.
     * If the property is not found, the default value is supplied by the specified
     * Supplier.
     *
     * @param property           the {@link Field} of the configuration property
     * @param defaultValueSupplier the Supplier that provides the default value
     * @return the value of the property as a Map of Strings, or the default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.MAP)
     *      .build()
     * Map<String, Object> value = config.getMap(property, () -> new HashMap<>());
     * }
     * </pre>
     */
    @ValidateField
    public Map<String, Object> getMap(Field property, Supplier<Map<String, Object>> defaultValueSupplier) {
        return this.getMap(property.name(), defaultValueSupplier);
    }

    /**
     * Retrieves the value of a configuration property as an Object.
     * If the property is not found, a default value is supplied by invoking
     * the getDefaultValue method for the Object type.
     *
     * @param property the name of the configuration property
     * @param <T> the type of the property value
     * @param targetType the class type of the object to be returned
     * @return the value of the property as an Object, or a default value if the property is not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Class ApplicationConfig {
     *     private String name;
     *
     *     public ApplicationConfig(String name) {
     *          this.name = name;
     *     }
     * }
     *
     * ApplicationConfig value = config.getObject("property.name", ApplicationConfig.class);
     * }
     * </pre>
     */
    public <T> T getObject(String property, Class<T> targetType) {
        return this.getObject(property, (Supplier<T>) () -> this.getDefaultValue(property, targetType), targetType);
    }

    /**
     * Retrieves an object of the specified type from the configuration using the given property key.
     * If the property is not found, the default value provided by the supplier is returned.
     *
     * @param property the key of the property to retrieve
     * @param defaultValueSupplier a supplier that provides a default value if the property is not found
     * @param targetType the class type of the object to be returned
     * @param <T> the type of the object to be returned
     * @return the object of the specified type from the configuration, or the default value if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Class ApplicationConfig {
     *     private String name;
     *
     *     public ApplicationConfig(String name) {
     *          this.name = name;
     *     }
     * }
     *
     * ApplicationConfig value = config.getObject("property.name", () -> new ApplicationConfig("default"), ApplicationConfig.class);
     * }
     * </pre>
     */
    public <T> T getObject(String property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.getObject(property, defaultValueSupplier.get(), targetType);
    }

    /**
     * Retrieves the value of a specified property, converting it to the desired type.
     * If the property is not found, returns the provided default value.
     *
     * @param property    the name of the property to retrieve
     * @param defaultValue the default value to return if the property is not found
     * @param targetType  the class type to which the property value should be converted
     * @param <T>         the type of the property value
     * @return the property value converted to the specified type, or the default value if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Class ApplicationConfig {
     *     private String name;
     *
     *     public ApplicationConfig(String name) {
     *          this.name = name;
     *     }
     * }
     *
     * ApplicationConfig value = config.getObject("property.name", new ApplicationConfig("default"), ApplicationConfig.class);
     * }
     * </pre>
     */
    public <T> T getObject(String property, T defaultValue, Class<T> targetType) {
        return this.getOptional(property, String.class)
                .map(value -> mapper.convertValue(new JSONObject(value).toMap(), targetType))
                .orElse(defaultValue);
    }

    /**
     * Retrieves an object of the specified type from the configuration using the given field.
     *
     * @param property  the field representing the configuration property to retrieve
     * @param targetType the class type of the object to be returned
     * @param <T> the type of the object to be returned
     * @return the object of the specified type from the configuration
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.OBJECT)
     *      .className(ApplicationConfig.class)
     *      .build()
     * Class ApplicationConfig {
     *     private String name;
     *
     *     public ApplicationConfig(String name) {
     *          this.name = name;
     *     }
     * }
     *
     * ApplicationConfig value = config.getObject(property, ApplicationConfig.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> T getObject(Field property, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType), targetType);
    }

    /**
     * Retrieves an object of the specified type from the configuration using the given field.
     * If the field is not found, the default value provided by the supplier is returned.
     *
     * @param property the field representing the configuration property to retrieve
     * @param defaultValueSupplier a supplier that provides a default value if the property is not found
     * @param targetType the class type of the
     * @param <T> the type of the object to be returned
     * @return the object of the specified type from the configuration, or the default value if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.OBJECT)
     *      .className(ApplicationConfig.class)
     *      .build()
     * Class ApplicationConfig {
     *     private String name;
     *
     *     public ApplicationConfig(String name) {
     *          this.name = name;
     *     }
     * }
     *
     * ApplicationConfig value = config.getObject(property, () -> new ApplicationConfig("default"), ApplicationConfig.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> T getObject(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), defaultValueSupplier.get(), targetType), targetType);
    }

    /**
     * Retrieves an object of the specified type from the configuration using the given field.
     * If the field is not found, the default value provided by the supplier is returned.
     *
     * @param property the field representing the configuration property to retrieve
     * @param defaultValue the default value to return if the property is not found
     * @param targetType the class type of the object to be returned
     * @param <T> the type of the object to be returned
     * @return the object of the specified type from the configuration, or the default value if not found
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field property = Field.builder()
     *      .name("property.name")
     *      .type(Field.Type.OBJECT)
     *      .className(ApplicationConfig.class)
     *      .build()
     * Class ApplicationConfig {
     *     private String name;
     *
     *     public ApplicationConfig(String name) {
     *          this.name = name;
     *     }
     * }
     *
     * ApplicationConfig value = config.getObject(property, new ApplicationConfig("default"), ApplicationConfig.class);
     * }
     * </pre>
     */
    @ValidateField
    public <T> T getObject(Field property, T defaultValue, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), defaultValue, targetType), targetType);
    }

    /**
     * Validates a collection of fields and records any validation problems.
     *
     * @param fields   an iterable collection of Field objects to be validated
     * @param problems a Consumer to record validation problem messages
     * @return true if all fields are valid, false otherwise
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Consumer<String> problems = (problem) -> System.out.println(problem);
     * boolean valid = config.validateAndRecord(fields, problems);
     * }
     * </pre>
     */
    public boolean validateAndRecord(Iterable<Field> fields, Consumer<String> problems) {
        return this.validate(fields, ((field, value, problemMessage) -> {
            if (value == null) {
                problems.accept(String.format("Value for %s is invalid.", field.name()));
                return;
            }

            problems.accept("'Value " + value.toString() + " is invalid for the field " + field.name() + " : " + problemMessage);
        }));
    }

    /**
     * Validates a collection of fields and records any validation problems.
     *
     * @param fields   an iterable collection of Field objects to be validated
     * @param problems a Field.ValidationOutput object to record validation problem messages
     * @return true if all fields are valid, false otherwise
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field.ValidationOutput problems = new Field.ValidationOutput();
     * boolean valid = config.validate(fields, problems);
     * }
     * </pre>
     */
    public boolean validate(Iterable<Field> fields, Field.ValidationOutput problems) {
        var valid = true;
        for (Field field: fields) {
            if (!field.validate(this, problems)) {
                valid = false;
            }
        }

        return valid;
    }
    /**
     * Retrieves the default value for a given property, converting it to the specified class type if necessary.
     *
     * @param <T> The type of the default value to be returned.
     * @param property The name of the property for which the default value is needed.
     * @param defaultValue The default value to be used if the property is not found in the defaults map.
     * @param defaultClassType The class type to which the default value should be converted.
     * @param args Optional arguments to format the property name.
     * @return The default value of the specified type, or null if no default is found.
     * @throws ConfigException If the conversion of the default value fails or if the configuration is invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String value = config.getDefaultValue("property.name", "default", String.class);
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private <T> T getDefaultValue(String property, Object defaultValue, Class<T> defaultClassType, String ...args) {
        return Try.of(() -> {
            String propertyName = ArrayUtils.isNotEmpty(args)?String.format(property, (Object[]) args):property;
            if (defaultValue!=null) {
                if (defaultClassType.isInstance(defaultValue)) {
                    return (T) defaultValue;
                } else {
                    return this.mapper.convertValue(defaultValue, defaultClassType);
                }
            }
            else if (this.defaultsMap.get(propertyName) == null) {
                return null;
            }
            return this.mapper.convertValue(this.defaultsMap.get(propertyName), defaultClassType);
        }).getOrElseThrow(() -> new ConfigException(String.format("Invalid configuration for field: %s", property), "Please validate the configurations for - " + property));
    }

    private <T> T getDefaultValueWithArgs(String property, Class<T> defaultClassType, String[] args) {
        return this.getDefaultValue(property, null, defaultClassType, args);
    }

    private <T> T getDefaultValue(String property, Object defaultValue, Class<T> defaultClassType) {
        return this.getDefaultValue(property, defaultValue, defaultClassType, (String[]) null);
    }

    private <T> T getDefaultValue(String property, Class<T> defaultClassType) {
        return this.getDefaultValue(property, null, defaultClassType, (String[]) null);
    }

}
