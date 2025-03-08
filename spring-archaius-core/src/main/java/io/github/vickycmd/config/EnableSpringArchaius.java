package io.github.vickycmd.config;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation to enable Archaius integration with Spring applications.
 * <p>
 * This annotation, when added to a Spring configuration class, imports the
 * {@link ArchaiusConfiguration} class, which sets up the necessary beans and
 * configurations for integrating Netflix Archaius with the Spring environment.
 * It allows for dynamic property management and configuration updates from
 * external sources, leveraging Archaius's capabilities within a Spring context.
 * </p>
 *
 * Usage:
 * <ul>
 *   <li>Annotate a Spring configuration class with {@code @EnableSpringArchaius}
 *   to automatically configure Archaius with Spring.</li>
 * </ul>
 *
 * Related Classes:
 * <ul>
 *   <li>{@link ArchaiusConfiguration}</li>
 * </ul>
 *
 * Author: Vicky CMD
 * Since: 1.0
 * Version: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ArchaiusConfiguration.class)
public @interface EnableSpringArchaius {
}
