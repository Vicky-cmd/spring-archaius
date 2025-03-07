package io.github.vickycmd.config.sources;

import io.github.vickycmd.config.model.ApplicationProperty;

import java.util.List;
import java.util.Optional;

public interface ApplicationPropertyRepository {
    public List<ApplicationProperty> findAll();
    public Optional<ApplicationProperty> findByKey(String key);
}
