package io.github.vickycmd.config.utils;

import io.github.vickycmd.config.Configuration;
import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.fields.Field;
import org.slf4j.Logger;

/**
 * The Utilities class provides utility methods for configuration validation.
 * It includes methods to validate a given configuration against a set of fields,
 * logging any errors encountered, and throwing a ConfigException if validation fails.
 * This class is designed to ensure consistent configuration validation and error handling
 * across the application.
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Validation of configuration against specified fields</li>
 *   <li>Logging of validation errors using a Logger instance</li>
 *   <li>Throwing ConfigException for invalid configurations</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Configuration config = new Configuration(environment);
 * Field.Set fields = new Field.Set();
 * fields.add(new Field("property.name", "Property Name", "Description", () -> "default value", null, Field.Type.STRING, String.class, Field.Importance.LOW, null, true));
 * fields.add(new Field("property.number", "Property Number", "Description", () -> 42, null, Field.Type.INTEGER, Integer.class, Field.Importance.LOW, null, true));
 * fields.add(new Field("property.list", "Property List", "Description", () -> Arrays.asList("a", "b", "c"), null, Field.Type.LIST, List.class, Field.Importance.LOW, null, true));
 *
 * validateConfig(config, fields, Configuration.class, log);
 * }
 * </pre>
 *
 * <p>This class is used throughout the configuration framework to provide consistent
 * validation and error handling for configuration-related issues.</p>
 *
 * <p>Note: This class cannot be instantiated.</p>
 *
 * @author Vicky CMD
 * @version 1.0
 * @since 1.0
 * @see io.github.vickycmd.config.Configuration
 * @see io.github.vickycmd.config.errors.ConfigException
 * @see io.github.vickycmd.config.fields.Field
 */
public class Utilities {
    private Utilities() {}

    /**
     * <p>This method checks the provided configuration using the specified set of fields.
     * If validation fails, it logs the error messages using the provided Logger instance.
     * If any validation errors are found, a ConfigException is thrown, indicating an
     * error in configuring an instance of the specified source class.</p>
     *
     * @param configuration the configuration to validate
     * @param fields the fields to validate
     * @param sourceClassName the source class name
     * @param log the logger to use for logging errors
     * @throws ConfigException if the configuration is invalid
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Configuration config = new Configuration(environment);
     * Field.Set fields = new Field.Set();
     * fields.add(new Field("property.name", "Property Name", "Description", () -> "default value", null, Field.Type.STRING, String.class, Field.Importance.LOW, null, true));
     * fields.add(new Field("property.number", "Property Number", "Description", () -> 42, null, Field.Type.INTEGER, Integer.class, Field.Importance.LOW, null, true));
     * fields.add(new Field("property.list", "Property List", "Description", () -> Arrays.asList("a", "b", "c"), null, Field.Type.LIST, List.class, Field.Importance.LOW, null, true));
     *
     * validateConfig(config, fields, Configuration.class, log);
     * }
     * </pre>
     */
    public static void validateConfig(Configuration configuration, Field.Set fields, Class<?> sourceClassName, Logger log) {
        if (!configuration.validateAndRecord(fields, log::error)) {
            throw new ConfigException("Error configuring an instance of " + sourceClassName.getSimpleName() + "; check the logs for details.", "Please validate the configurations for - " + fields);
        }
    }
}
