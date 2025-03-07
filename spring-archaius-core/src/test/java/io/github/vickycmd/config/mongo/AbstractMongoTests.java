package io.github.vickycmd.config.mongo;


import com.mongodb.client.MongoClient;
import io.github.vickycmd.config.ArchaiusTests;
import io.github.vickycmd.config.model.ApplicationProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMongoTests extends ArchaiusTests {

    @MockitoBean
    protected MongoClient client;

    @MockitoBean
    protected MongoTemplate template;

    @Override
    public List<ApplicationProperty> getApplicationProperties() {
        List<ApplicationProperty> applicationProperties = new ArrayList<>(super.getApplicationProperties());
        applicationProperties.addAll(List.of(
                new ApplicationProperty("123", "spring.archaius.config.mongo.uri", "mongodb://localhost:27015"),
                new ApplicationProperty("456", "spring.archaius.config.mongo.database", "test"),
                new ApplicationProperty("789", "spring.archaius.config.mongo.collection", "configurations")
        ));

        return applicationProperties;
    }
}
