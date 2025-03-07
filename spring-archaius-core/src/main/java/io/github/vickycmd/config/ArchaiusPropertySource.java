package io.github.vickycmd.config;

import com.netflix.config.DynamicPropertyFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class ArchaiusPropertySource extends PropertySource<Object> {

    DynamicPropertyFactory propertyFactory = DynamicPropertyFactory.getInstance();

    public ArchaiusPropertySource() {
        super("archaiusPropertySource");
    }

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
