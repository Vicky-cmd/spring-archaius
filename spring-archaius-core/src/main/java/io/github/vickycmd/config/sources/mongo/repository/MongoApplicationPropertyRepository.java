package io.github.vickycmd.config.sources.mongo.repository;

import io.github.vickycmd.config.Configuration;
import io.github.vickycmd.config.model.ApplicationProperty;
import io.github.vickycmd.config.fields.Field;
import io.github.vickycmd.config.sources.ApplicationPropertyRepository;
import io.github.vickycmd.config.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation of the {@link ApplicationPropertyRepository} interface.
 *
 * <p>This service class provides methods to interact with MongoDB for managing
 * {@link ApplicationProperty} entities. It retrieves all properties or a specific
 * property by its key from a MongoDB collection specified in the configuration.
 *
 * <p>Configuration Fields:
 * <ul>
 *   <li>{@link #DATABASE_FIELD} - Specifies the MongoDB database containing the collection.</li>
 *   <li>{@link #COLLECTION_FIELD} - Specifies the MongoDB collection containing the application configurations.</li>
 * </ul>
 *
 * <p>Methods:
 * <ul>
 *   <li>{@link #findAll()} - Retrieves a list of all application properties from the MongoDB collection.</li>
 *   <li>{@link #findByKey(String)} - Finds an application property by its key, returning an {@link Optional}.</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 *     MongoApplicationPropertyRepository repository = ...;
 *     List&lt;{@link ApplicationProperty}&gt; properties = repository.findAll();
 *     Optional&lt;{@link ApplicationProperty}&gt; property = repository.findByKey("configKey");
 * </pre>
 *
 * <p>Related Classes:
 * <ul>
 *   <li>{@link ApplicationProperty} - Represents the configuration property model.</li>
 *   <li>{@link Configuration} - Provides configuration management.</li>
 *   <li>{@link MongoTemplate} - Facilitates MongoDB operations.</li>
 *   <li>{@link Utilities} - Contains utility methods for configuration validation.</li>
 * </ul>
 *
 * <p>Author: Vicky CMD
 * <p> Version: 1.0
 * <p>Since: 1.0
 *
 * <p>See also:
 * <ul>
 *   <li><a href="https://github.com/vickycmd/spring-archaius-core">Project Repository</a></li>
 * </ul>
 *
 * <p>Annotations:
 * <ul>
 *   <li>{@link Slf4j} - Enables logging capabilities.</li>
 *   <li>{@link Service} - Marks this class as a Spring service component.</li>
 *   <li>{@link ConditionalOnProperty} - Activates this service only if the specified property is set to "mongo".</li>
 * </ul>
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.archaius.config", name = "source", havingValue = "mongo")
public class MongoApplicationPropertyRepository implements ApplicationPropertyRepository {

    private final Configuration configuration;
    private final MongoTemplate template;

    static final Field DATABASE_FIELD = Field.create("spring.archaius.config.mongo.database")
            .displayName("Mongo Config Source Database")
            .desc("The database containing the Collection with the Application configurations")
            .type(Field.Type.STRING)
            .importance(Field.Importance.HIGH)
            .required()
            .build();

    static final Field COLLECTION_FIELD = Field.create("spring.archaius.config.mongo.collection")
            .displayName("Mongo Config Source Collection")
            .desc("The Collection with the Application configurations")
            .type(Field.Type.STRING)
            .importance(Field.Importance.HIGH)
            .required()
            .build();

    static final Field.Set ALL_FIELDS = new Field.Set(DATABASE_FIELD, COLLECTION_FIELD);

    /**
     * Constructs a MongoApplicationPropertyRepository instance with the specified Configuration and MongoTemplate.
     * Validates the configuration fields using the Utilities class.
     *
     * @param configuration the Configuration instance for managing application properties
     * @param template the MongoTemplate instance for MongoDB operations
     */
    @Autowired
    public MongoApplicationPropertyRepository(Configuration configuration, MongoTemplate template) {
        this.configuration = configuration;
        Utilities.validateConfig(this.configuration, ALL_FIELDS, getClass(), log);
        this.template = template;
    }

    /**
     * Retrieves all application properties from the MongoDB collection.
     *
     * @return a list of all {@link ApplicationProperty} instances found in the specified collection.
     */
    @Override
    public List<ApplicationProperty> findAll() {
        return this.template.findAll(ApplicationProperty.class, this.configuration.get(COLLECTION_FIELD, String.class));
    }

    /**
     * Finds an application property by its key from the MongoDB collection.
     *
     * <p>Executes a query to search for an {@link ApplicationProperty} with the specified key
     * in the configured MongoDB collection. Returns an {@link Optional} containing the property
     * if found, or an empty {@link Optional} if not.
     *
     * @param key the key of the application property to find
     * @return an {@link Optional} containing the found {@link ApplicationProperty}, or empty if not found
     */
    @Override
    public Optional<ApplicationProperty> findByKey(String key) {
        Query query = new Query();
        query.addCriteria(Criteria.where("key").is(key));
        return Optional.ofNullable(this.template.findOne(query, ApplicationProperty.class, this.configuration.get(COLLECTION_FIELD, String.class)));
    }
}
