package io.github.vickycmd.config.sources;

import io.github.vickycmd.config.model.ApplicationProperty;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link ApplicationProperty} entities.
 *
 * <p>This interface provides methods to interact with the underlying data
 * source for {@code ApplicationProperty} objects. It allows retrieval of
 * all properties or a specific property by its key.
 *
 * <p>Methods:
 * <ul>
 *   <li>{@link #findAll()} - Retrieves a list of all application properties.</li>
 *   <li>{@link #findByKey(String)} - Finds an application property by its key, returning an {@link Optional}.</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 *     ApplicationPropertyRepository repository = ...;
 *     List&lt;{@link io.github.vickycmd.config.model.ApplicationProperty}&gt; properties = repository.findAll();
 *     Optional&lt;{@link io.github.vickycmd.config.model.ApplicationProperty}&gt; property = repository.findByKey("configKey");
 * </pre>
 *
 * <p>Related Classes:
 * <ul>
 *   <li>{@link io.github.vickycmd.config.model.ApplicationProperty} - Represents the configuration property model.</li>
 * </ul>
 *
 * <p>Author: Vicky CMD
 *
 * <p>See also:
 * <ul>
 *   <li><a href="https://github.com/vickycmd/spring-archaius-core">Project Repository</a></li>
 * </ul>
 */
public interface ApplicationPropertyRepository {
    /**
     * Retrieves all application properties.
     *
     * @return a list of all {@link ApplicationProperty} instances.
     */
    List<ApplicationProperty> findAll();

    /**
     * Finds an application property by its key.
     *
     * @param key the key of the application property to find.
     * @return an {@link Optional} containing the {@link ApplicationProperty} if found, or an empty {@link Optional} if not found.
     */
    Optional<ApplicationProperty> findByKey(String key);
}
