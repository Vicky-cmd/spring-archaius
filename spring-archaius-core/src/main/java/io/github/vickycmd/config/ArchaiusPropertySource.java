package io.github.vickycmd.config;

import com.netflix.config.DynamicPropertyFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ArchaiusPropertySource is a custom implementation of the Spring PropertySource
 * that integrates with Netflix's Archaius library to provide dynamic property
 * management. This class allows for the retrieval of configuration properties
 * using Archaius's DynamicPropertyFactory.
 *
 * <p>This class is marked as a Spring Component, making it eligible for
 * component scanning and dependency injection within a Spring application
 * context. It extends the PropertySource class, providing a bridge between
 * Archaius and Spring's environment abstraction.</p>
 *
 * <p>The primary functionality of this class is to override the getProperty
 * method, which retrieves a property value by its name using Archaius's
 * DynamicPropertyFactory. If the property is not found, it returns null.</p>
 *
 * <p>Equality and hash code methods are overridden to ensure that instances
 * of this class can be compared and used in collections effectively, based
 * on their property factory instance.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
 * ArchaiusPropertySource propertySource = context.getBean(ArchaiusPropertySource.class);
 * String value = (String) propertySource.getProperty("some.property.name");
 * }
 * </pre>
 *
 * <p>Related Classes: {@link com.netflix.config.DynamicPropertyFactory},
 * {@link org.springframework.core.env.PropertySource}</p>
 *
 * <p>Author: Vicky CMD</p>
 * <p>Version: 1.0</p>
 * <p>Since: 2023</p>
 */
@Component
public final class ArchaiusPropertySource extends PropertySource<Object> {

    DynamicPropertyFactory propertyFactory = DynamicPropertyFactory.getInstance();

    /**
     * Constructs a new ArchaiusPropertySource with the name "archaiusPropertySource".
     * This serves as a bridge between Archaius and Spring's PropertySource abstraction.
     */
    public ArchaiusPropertySource() {
        super("archaiusPropertySource");
    }

    /**
     * Retrieves the value of a property by its name using Archaius's DynamicPropertyFactory.
     * If the property is not found, it returns null.
     *
     * @param name the name of the property to retrieve
     * @return the value of the property, or null if not found
     */
    @Override
    public Object getProperty(@NonNull String name) {
        return propertyFactory.getStringProperty(name, null).get();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArchaiusPropertySource that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(propertyFactory, that.propertyFactory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyFactory);
    }
}
