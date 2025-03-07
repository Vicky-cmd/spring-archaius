package io.github.vickycmd.config.mongo;


import io.github.vickycmd.config.Configuration;
import io.github.vickycmd.config.model.ApplicationProperty;
import io.github.vickycmd.config.sources.mongo.repository.MongoApplicationPropertyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@TestPropertySource(locations = {"classpath:application-mongo.properties"})
class MongoApplicationPropertyRepoTests extends AbstractMongoTests {

    @Autowired
    private Configuration configuration;

    private MongoApplicationPropertyRepository mongoApplicationPropertyRepo;

    @BeforeEach
    void setUp() {
        mongoApplicationPropertyRepo = new MongoApplicationPropertyRepository(configuration, template);
    }


    @Test
    void testFindAllReturnsObjectFromTheMongoTemplate() {
        List<ApplicationProperty> expectedList = getApplicationProperties();
        when(this.template.findAll(any(), any())).thenReturn(expectedList.stream().map(data -> (Object) data).toList());
        List<ApplicationProperty> properties = mongoApplicationPropertyRepo.findAll();

        Assertions.assertNotNull(properties);
        Assertions.assertEquals(expectedList.size(), properties.size());
        Assertions.assertArrayEquals(expectedList.toArray(new ApplicationProperty[0]), properties.toArray(new ApplicationProperty[0]));
    }

    @Test
    void testFindByKeyReturnsEmptyWhenObjectIsNotFound() {
        Optional<ApplicationProperty> property = mongoApplicationPropertyRepo.findByKey("spring.application.name");

        Assertions.assertTrue(property.isEmpty());
    }


    @Test
    void testFindByKeyReturnsObjectWhenObjectIsFound() {
        ApplicationProperty expectedProperty = new ApplicationProperty("123", "spring.archaius.config.mongo.uri", "mongodb://localhost:27015");
        when(this.template.findOne(any(), any(), any())).thenReturn(expectedProperty);

        Optional<ApplicationProperty> property = mongoApplicationPropertyRepo.findByKey("spring.archaius.config.mongo.uri");

        Assertions.assertTrue(property.isPresent());
        Assertions.assertEquals(expectedProperty.getKey(), property.get().getKey());
        Assertions.assertEquals(expectedProperty.getValue(), property.get().getValue());
    }
}
