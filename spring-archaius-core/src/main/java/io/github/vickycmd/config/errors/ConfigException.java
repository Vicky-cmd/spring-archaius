package io.github.vickycmd.config.errors;

import lombok.Getter;


@Getter
/**
 * The ConfigException class represents an exception that occurs during configuration processing.
 * It extends RuntimeException and provides additional context through an action field that 
 * describes steps to resolve the configuration issue.
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Custom message describing the configuration error</li>
 *   <li>Action string providing resolution steps</li>
 *   <li>Runtime exception behavior for unchecked exception handling</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * try {
 *     config.get("missing.property", String.class); 
 * } catch (ConfigException e) {
 *     System.err.println("Error: " + e.getMessage());
 *     System.err.println("Action: " + e.getAction());
 * }
 * }
 * </pre>
 *
 * <p>This class is used throughout the configuration framework to provide consistent
 * error handling and reporting for configuration-related issues.</p>
 *
 * @author Vicky CMD
 * @version 1.0
 * @see io.github.vickycmd.config.Configuration
 * @since 1.0
 */
public class ConfigException extends RuntimeException {

    /**
     * Represents the specific action associated with this configuration.
     */
    @Getter
    private final String action;

    /**
     * Represents an exception that occurs during configuration processing.
     * This exception includes an action string that describes the specific
     * action being performed when the exception was thrown.
     *
     * @param message the detail message of the exception
     * @param action the action to be performed to mitigate the exception
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * try {
     *     // Configuration processing logic
     * } catch (Exception e) {
     *     throw new ConfigException("Failed to load configuration", "Please validate the configuration configurations");
     * }
     * }
     * </pre>
     */
    public ConfigException(String message, String action) {
        super(message);
        this.action = action;
    }

}
