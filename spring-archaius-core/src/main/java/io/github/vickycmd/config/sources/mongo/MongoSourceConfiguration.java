package io.github.vickycmd.config.sources.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.vickycmd.config.fields.Field;
import io.github.vickycmd.config.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Configuration class for setting up MongoDB as a configuration source for the application.
 *
 * <p>This class is annotated with {@link Configuration} and {@link ConditionalOnProperty} to
 * enable MongoDB as a configuration source when the property "spring.archaius.config.source"
 * is set to "mongo". It defines beans for {@link MongoClient} and {@link MongoTemplate} to
 * facilitate interaction with the MongoDB database.
 *
 * <p>The class uses two configuration fields:
 * <ul>
 *   <li>{@code MONGO_URI_CONFIG}: Specifies the URI for connecting to the MongoDB instance.
 *   <li>{@code DATABASE_FIELD}: Specifies the database name containing the application configurations.
 * </ul>
 *
 * <p>The {@code mongoClient} method creates a {@link MongoClient} bean with a custom codec
 * registry that includes {@link ApplicationPropertyCodec} for encoding and decoding
 * {@link io.github.vickycmd.config.model.ApplicationProperty} objects.
 *
 * <p>The {@code mongoTemplate} method creates a {@link MongoTemplate} bean using the
 * {@link MongoClient} and the specified database name.
 *
 * <p>Usage of this class ensures that the application can read configuration properties
 * from a MongoDB database, allowing for dynamic configuration management.
 *
 * <p>Related Classes:
 * <ul>
 *   <li>{@link ApplicationPropertyCodec}: Codec for encoding and decoding application properties.
 *   <li>{@link Utilities}: Utility class for configuration validation.
 *   <li>{@link Field}: Represents configuration fields with validation capabilities.
 * </ul>
 *
 * <p>Author: Vicky
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.archaius.config", name = "source", havingValue = "mongo")
public class MongoSourceConfiguration {
    private io.github.vickycmd.config.Configuration configuration;

    private final Field MONGO_URI_CONFIG = Field.create("spring.archaius.config.mongo.uri")
            .displayName("Mongo Config Source URI")
            .desc("The URI for the mongo Database to be used by the application")
            .defaultValue("mongodb://localhost:27017")
            .importance(Field.Importance.HIGH)
            .type(Field.Type.STRING)
            .required()
            .build();

    static final Field DATABASE_FIELD = Field.create("spring.archaius.config.mongo.database")
            .displayName("Mongo Config Source Database")
            .desc("The database containing the Collection with the Application configurations")
            .type(Field.Type.STRING)
            .importance(Field.Importance.HIGH)
            .required()
            .build();

    /**
     * Constructs a MongoSourceConfiguration instance with the specified Configuration.
     * Validates the configuration using the provided Configuration object and logs any errors.
     *
     * @param configuration the Configuration instance used for managing application properties
     *
     * <p>This constructor initializes the MongoSourceConfiguration by setting the configuration
     * and validating it against the MONGO_URI_CONFIG field. If validation fails, a ConfigException
     * is thrown, and the error details are logged using the provided Logger.</p>
     */
    public MongoSourceConfiguration (io.github.vickycmd.config.Configuration configuration) {
        this.configuration = configuration;
        Utilities.validateConfig(this.configuration, Field.Set.of(MONGO_URI_CONFIG), getClass(), log);
    }

    /**
     * Creates and configures a {@link MongoClient} bean with a custom codec registry.
     *
     * <p>This method sets up a {@link MongoClient} using a {@link ConnectionString} obtained
     * from the application's configuration. It registers a custom {@link ApplicationPropertyCodec}
     * to handle BSON encoding and decoding of {@link io.github.vickycmd.config.model.ApplicationProperty} objects.
     *
     * <p>The {@link MongoClient} is marked as the primary bean to be used by the application.
     *
     * @return a configured {@link MongoClient} instance
     */
    @Bean
    @Primary
    public MongoClient mongoClient() {
        CodecRegistry registry = CodecRegistries.fromCodecs(new ApplicationPropertyCodec());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                registry
        );
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(this.configuration.getString(MONGO_URI_CONFIG)))
                .codecRegistry(codecRegistry)
                .build();
        return MongoClients.create(settings);
    }

    /**
     * Creates and returns a {@link MongoTemplate} bean configured with the specified {@link MongoClient}.
     * The database name is retrieved from the configuration using the {@code DATABASE_FIELD}.
     *
     * @param client the {@link MongoClient} used to connect to the MongoDB instance
     * @return a configured {@link MongoTemplate} instance
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoClient client) {
        return  new MongoTemplate(client, this.configuration.get(DATABASE_FIELD, String.class));
    }

}
