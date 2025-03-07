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

    public MongoSourceConfiguration (io.github.vickycmd.config.Configuration configuration) {
        this.configuration = configuration;
        Utilities.validateConfig(this.configuration, Field.Set.of(MONGO_URI_CONFIG), getClass(), log);
    }

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

    @Bean
    public MongoTemplate mongoTemplate(MongoClient client) {
        return  new MongoTemplate(client, this.configuration.get(DATABASE_FIELD, String.class));
    }

}
