package io.github.vickycmd.config.mongo;

import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PollResult;
import io.github.vickycmd.config.model.ApplicationProperty;
import io.github.vickycmd.config.sources.mongo.ApplicationPropertyCodec;
import io.github.vickycmd.config.sources.mongo.MongoPolledConfigurationSource;
import io.github.vickycmd.config.sources.mongo.repository.MongoApplicationPropertyRepository;
import io.github.vickycmd.config.utils.TestUtilities;
import org.bson.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@TestPropertySource(locations = {"classpath:application-mongo.properties"})
class SpringArchaiusTests extends AbstractMongoTests {

    @MockitoBean
    private MongoApplicationPropertyRepository mongoRepository;

    @Autowired
    private MongoPolledConfigurationSource configurationSource;

    private final FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(1000, 5000, false);

    private final ApplicationPropertyCodec codec = new ApplicationPropertyCodec();

    @Test
    void testMongoConfigurationResult() throws Exception {
        List<ApplicationProperty> properties = getApplicationProperties();
        when(this.mongoRepository.findAll()).thenReturn(properties);
        TestUtilities.triggerConfigReload(configurationSource, scheduler, logger());

        PollResult result = configurationSource.poll(true, null);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getComplete());
        Assertions.assertEquals(properties.size(), result.getComplete().size());
        Assertions.assertEquals("mongodb://localhost:27015", result.getComplete().get("spring.archaius.config.mongo.uri"));
    }

    @Test
    void testFindByValueReturnsNonNullValue() {

        String key = "spring.archaius.config.mongo.uri";
        when(mongoRepository.findByKey(key)).thenReturn(Optional.of(new ApplicationProperty("123", key, "mongodb://localhost:27015")));

        Optional<ApplicationProperty> property = mongoRepository.findByKey(key);

        Assertions.assertTrue(property.isPresent());
        logger().info(property.get().toString());
        Assertions.assertEquals(key, property.get().getKey());
        Assertions.assertEquals("mongodb://localhost:27015", property.get().getValue());

    }

    @Test
    void testApplicationPropertyCodecGetEncoderClassReturnsExpectedValue() {
        Class<ApplicationProperty> clazz = codec.getEncoderClass();
        Assertions.assertEquals(ApplicationProperty.class, clazz);
    }

    @Test
    void testApplicationPropertyCodecDecodeReturnsExpectedValue() {
        BsonObjectId id = new BsonObjectId();
        String key = "spring.archaius.config.mongo.uri";
        String description = "MongoDB URI";
        String value = "mongodb://localhost:27015";
        BsonDocument document = new BsonDocument();
        document.append("_id", id);
        document.append("key", new BsonString(key));
        document.append("description", new BsonString(description));
        document.append("value", new BsonString(value));
        BsonReader reader = new BsonDocumentReader(document);

        ApplicationProperty applicationProperty = codec.decode(reader, null);

        Assertions.assertNotNull(applicationProperty);
        Assertions.assertEquals(id.getValue().toString(), applicationProperty.getId());
        Assertions.assertEquals(key, applicationProperty.getKey());
        Assertions.assertEquals(value, applicationProperty.getValue());
    }

    @Test
    void testApplicationPropertyCodecEncodeReturnsExpectedValue() {
        String id = new BsonObjectId().getValue().toString();
        String key = "spring.archaius.config.mongo.uri";
        String description = "MongoDB URI";
        String value = "mongodb://localhost:27015";
        ApplicationProperty applicationProperty = new ApplicationProperty(id, key, value);
        BsonWriter writer = mock(BsonWriter.class);

        codec.encode(writer, applicationProperty, null);

        verify(writer, times(1)).writeString("_id", id);
        verify(writer, times(1)).writeString("key", key);
        verify(writer, times(1)).writeString("value", value);
    }

    @Test
    void testApplicationPropertyCodecEncodeReturnsExpectedValueWithDescription() {
        String id = new BsonObjectId().getValue().toString();
        String key = "spring.archaius.config.mongo.uri";
        String description = "MongoDB URI";
        String value = "mongodb://localhost:27015";
        ApplicationProperty applicationProperty = new ApplicationProperty(id, key, value, description);
        BsonWriter writer = mock(BsonWriter.class);

        codec.encode(writer, applicationProperty, null);

        verify(writer, times(1)).writeString("_id", id);
        verify(writer, times(1)).writeString("key", key);
        verify(writer, times(1)).writeString("value", value);
    }

}
