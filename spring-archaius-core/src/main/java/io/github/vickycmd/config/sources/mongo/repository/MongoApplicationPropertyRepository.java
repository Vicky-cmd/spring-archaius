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

    @Autowired
    public MongoApplicationPropertyRepository(Configuration configuration, MongoTemplate template) {
        this.configuration = configuration;
        Utilities.validateConfig(this.configuration, ALL_FIELDS, getClass(), log);
        this.template = template;
    }

    @Override
    public List<ApplicationProperty> findAll() {
        return this.template.findAll(ApplicationProperty.class, this.configuration.get(COLLECTION_FIELD, String.class));
    }

    @Override
    public Optional<ApplicationProperty> findByKey(String key) {
        Query query = new Query();
        query.addCriteria(Criteria.where("key").is(key));
        return Optional.ofNullable(this.template.findOne(query, ApplicationProperty.class, this.configuration.get(COLLECTION_FIELD, String.class)));
    }
}
