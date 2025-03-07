package io.github.vickycmd.config.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.config.*;
import com.netflix.config.sources.URLConfigurationSource;
import io.github.vickycmd.config.ArchaiusTests;
import io.github.vickycmd.config.dto.OffersFeaturesDto;
import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.model.ApplicationProperty;
import io.github.vickycmd.config.fields.Field;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static io.github.vickycmd.config.utils.TestUtilities.triggerConfigReload;

@Slf4j
@TestPropertySource(locations = {"classpath:application-test.properties"})
class ConfigurationTests extends ArchaiusTests {

    @Autowired
    private io.github.vickycmd.config.Configuration configuration;

    private final ObjectMapper mapper = new ObjectMapper();

    private final FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(1000, 5000, false);

    @BeforeEach
    void setup() throws IOException {
        updateConfigPropertiesFile(List.of(new ApplicationProperty("000", "default.version", "1")));
        URL configPropertyURL = (new ClassPathResource("config.properties")).getURL();
        PolledConfigurationSource source = new URLConfigurationSource(configPropertyURL);
        triggerConfigReload(source, scheduler, logger());
    }

    @AfterEach
    public void clearSetup() throws IOException {
        updateConfigPropertiesFile(List.of(new ApplicationProperty("000", "default.version", "1")));
    }

    @Test
    void testWithDefaultValueReferred() {
        this.configuration.withDefault("application.datasource.type", "mongo")
                .withDefault("application.datasource.uri", "mongodb://localhost:27017/testdb");
        String type = this.configuration.getString("application.datasource.type");
        String uri = this.configuration.getString("application.datasource.uri");
        String username = this.configuration.getString("application.datasource.username");
        Assertions.assertNotNull(type);
        Assertions.assertEquals("mongo", type);
        Assertions.assertNotNull(uri);
        Assertions.assertEquals("mongodb://localhost:27017/testdb", uri);
        Assertions.assertNull(username);
    }

    @Test
    void testWithDefaultValueIgnoredWhenDefaultIsPassed() {
        this.configuration.withDefault("application.datasource.type", "mongo")
                .withDefault("application.datasource.uri", "mongodb://localhost:27017/testdb");
        String type = this.configuration.getString("application.datasource.type", "mysql");
        String uri = this.configuration.getString("application.datasource.uri", "mysql://localhost:3306");
        String username = this.configuration.getString("application.datasource.username", "root");
        Assertions.assertNotNull(type);
        Assertions.assertEquals("mysql", type);
        Assertions.assertNotNull(uri);
        Assertions.assertEquals("mysql://localhost:3306", uri);
        Assertions.assertNotNull(username);
        Assertions.assertEquals("root", username);
    }

    @Test
    void testGetReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String data = this.configuration.get(key, String.class);
        Assertions.assertNull(data);
    }

    @Test
    void testGetReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        String data = this.configuration.get(key, String.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String expectedValue = "v2";
        String data = this.configuration.get(key, String.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedData = this.configuration.get(key, String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String defaultValue = "v2";
        String data = this.configuration.get(key, defaultValue, String.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String data = this.configuration.get(key, String.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(key, "v1", String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String defaultValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String data = this.configuration.get(key, defaultValueSupplier, String.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String data = this.configuration.get(key, String.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> "v1";
        String updatedData = this.configuration.get(key, defaultValueSupplier, String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNull(data);
    }

    @Test
    void testGetFieldReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetFieldReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedData = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetFieldReturnsProperValueWhenResultIsPartOfAllowedValues() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .allowedValues("v1", "v2", "v3")
                .build();
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(data);
        Assertions.assertTrue(offersVersion.allowedValues().contains(data));
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetFieldThrowsErrorWhenFieldIsRequiredAndValueIsNotAvailable() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.get(offersVersion, String.class));
    }

    @Test
    void testGetFieldThrowsErrorWhenKeyNotPartOfTheAllowedList() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .allowedValues(List.of("v1.0.0", "v2.0.0"))
                .required()
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.get(offersVersion, String.class));
    }

    @Test
    void testGetFieldReturnsDefaultValueFromFieldWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);
    }

    @Test
    void testGetFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvidedFromField() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.get(offersVersion, defaultValue, String.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(offersVersion, defaultValue, String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String data = this.configuration.get(offersVersion, defaultValueSupplier, String.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFieldReturnsUpdateValueIfValueExistsEvenIfDefaultSupplierIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.get(offersVersion, String.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String defaultValue = "v3";
        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String updatedData = this.configuration.get(offersVersion, defaultValueSupplier, String.class);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetWithArgsReturnsNullWhenKeyDoesNotExist() {
        String key = "application.%s.version2";
        String data = this.configuration.get(key, String.class, "offers");
        Assertions.assertNull(data);
    }

    @Test
    void testGetWithArgsReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        String key = "application.%s.version";
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(key, String.class, "offers")));
        String data = this.configuration.get(key, String.class, "offers");
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetWithArgsReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.%s.version";
        String expectedValue = "v2";
        String data = this.configuration.get(key, String.class, "offers");
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(key, String.class, "offers")));

        String updatedData = this.configuration.get(key, String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetWithArgsReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.%s.version2";
        String defaultValue = "v2";
        String data = this.configuration.get(key, defaultValue, String.class, "offers");
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetWithArgsReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.%s.version";
        String data = this.configuration.get(key, String.class, "offers");
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(key, String.class, "offers")));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(key, "v1", String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetWithArgsReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.%s.version2";
        String defaultValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String data = this.configuration.get(key, defaultValueSupplier, String.class, "offers");
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetWithArgsReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.%s.version";
        String data = this.configuration.get(key, String.class, "offers");
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(key, String.class, "offers")));

        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> "v1";
        String updatedData = this.configuration.get(key, defaultValueSupplier, String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldWithArgsReturnsNullWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.%s.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNull(data);
    }

    @Test
    void testGetFieldWithArgsReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.%s.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(offersVersion.name(), String.class, "offers")));
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetFieldWithArgsReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.%s.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(offersVersion.name(), String.class, "offers")));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldWithArgsReturnsDefaultValueFromFieldWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.%s.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);
    }

    @Test
    void testGetFieldWithArgsReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvidedFromField() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.%s.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(offersVersion.name(), String.class, "offers")));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldWithArgsReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.%s.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.get(offersVersion, defaultValue, String.class, "offers");
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFieldWithArgsReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.%s.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(offersVersion.name(), String.class, "offers")));

        String updatedValue = "v2";
        String updatedData = this.configuration.get(offersVersion, defaultValue, String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetFieldWithArgsReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.%s.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String data = this.configuration.get(offersVersion, defaultValueSupplier, String.class, "offers");
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFieldWithArgsReturnsUpdateValueIfValueExistsEvenIfDefaultSupplierIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.%s.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.get(offersVersion, String.class, "offers");
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.get(offersVersion.name(), String.class, "offers")));

        String defaultValue = "v3";
        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String updatedData = this.configuration.get(offersVersion, defaultValueSupplier, String.class, "offers");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetOptionalReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        Optional<String> data = this.configuration.getOptional(key, String.class);
        Assertions.assertTrue(data.isEmpty());
    }

    @Test
    void testGetOptionalReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Optional<String> data = this.configuration.getOptional(key, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(expectedValue, data.get());
    }

    @Test
    void testGetOptionalReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String expectedValue = "v2";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Optional<String> updatedData = this.configuration.getOptional(key, String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(expectedValue, updatedData.get());
    }

    @Test
    void testGetOptionalReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String defaultValue = "v2";
        Optional<String> data = this.configuration.getOptional(key, defaultValue, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(defaultValue, data.get());
    }

    @Test
    void testGetOptionalReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        Optional<String> updatedData = this.configuration.getOptional(key, "v1", String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(updatedValue, updatedData.get());
    }

    @Test
    void testGetOptionalReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String defaultValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        Optional<String> data = this.configuration.getOptional(key, defaultValueSupplier, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(defaultValue, data.get());
    }

    @Test
    void testGetOptionalReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> "v1";
        Optional<String> updatedData = this.configuration.getOptional(key, defaultValueSupplier, String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(updatedValue, updatedData.get());
    }

    @Test
    void testGetOptionalFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        Optional<String> data = this.configuration.getOptional(offersVersion, String.class);
        Assertions.assertTrue(data.isEmpty());
    }

    @Test
    void testGetOptionalFieldReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));
        Optional<String> data = this.configuration.getOptional(offersVersion, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(expectedValue, data.get());
    }

    @Test
    void testGetOptionalFieldReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        Optional<String> updatedData = this.configuration.getOptional(offersVersion, String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(updatedValue, updatedData.get());
    }

    @Test
    void testGetOptionalFieldReturnsDefaultValueFromFieldWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        Optional<String> data = this.configuration.getOptional(offersVersion, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data.get());
    }

    @Test
    void testGetOptionalFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvidedFromField() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        Optional<String> updatedData = this.configuration.getOptional(offersVersion, String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(updatedValue, updatedData.get());
    }

    @Test
    void testGetOptionalFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        Optional<String> data = this.configuration.getOptional(offersVersion, defaultValue, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(defaultValue, data.get());
    }

    @Test
    void testGetOptionalFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        Optional<String> updatedData = this.configuration.getOptional(offersVersion, defaultValue, String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(updatedValue, updatedData.get());
    }

    @Test
    void testGetOptionalFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        Optional<String> data = this.configuration.getOptional(offersVersion, defaultValueSupplier, String.class);
        Assertions.assertTrue(data.isPresent());
        Assertions.assertEquals(defaultValue, data.get());
    }

    @Test
    void testGetOptionalFieldReturnsUpdateValueIfValueExistsEvenIfDefaultSupplierIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String defaultValue = "v3";
        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        Optional<String> updatedData = this.configuration.getOptional(offersVersion, defaultValueSupplier, String.class);
        Assertions.assertTrue(updatedData.isPresent());
        Assertions.assertEquals(updatedValue, updatedData.get());
    }

    @Test
    void testGetStringReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetStringReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        String data = this.configuration.getString(key);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetStringReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String expectedValue = "v2";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        String updatedData = this.configuration.getString(key);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetStringReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String defaultValue = "v2";
        String data = this.configuration.getString(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetStringReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        String updatedData = this.configuration.getString(key, "v1");
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetStringReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.version2";
        String defaultValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String data = this.configuration.getString(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetStringReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.version";
        String data = this.configuration.getString(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> "v1";
        String updatedData = this.configuration.getString(key, defaultValueSupplier);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetStringFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNull(data);
    }

    @Test
    void testGetStringFieldReturnsNullWhenKeyExist() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetStringFieldReturnsValueWhenKeyExistAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .build();
        String expectedValue = "v2";
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        String updatedData = this.configuration.getString(offersVersion);
        log.info(updatedValue);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetStringFieldReturnsDefaultValueFromFieldWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String data = this.configuration.getString(offersVersion);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);
    }

    @Test
    void testGetStringFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvidedFromField() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        String updatedData = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetStringFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.getString(offersVersion, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetStringFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String defaultValue = "v3";
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String updatedValue = "v2";
        String updatedData = this.configuration.getString(offersVersion, defaultValue);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetStringFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersVersion = Field.builder()
                .name("application.offers.version2")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v2")
                .build();
        String defaultValue = "v3";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String data = this.configuration.getString(offersVersion, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetStringFieldReturnsUpdateValueIfValueExistsEvenIfDefaultSupplierIsProvided() throws IOException, InterruptedException {
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .defaultValue("v1")
                .build();
        String data = this.configuration.getString(offersVersion);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersVersion.defaultValueAsString(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersVersion.name())));

        String defaultValue = "v3";
        String updatedValue = "v2";
        Supplier<String> defaultValueSupplier = () -> defaultValue;
        String updatedData = this.configuration.getString(offersVersion, defaultValueSupplier);
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(updatedValue, updatedData);
    }

    @Test
    void testGetBooleanReturnsNullWhenKeyDoesNotExist() {
        String key = "database.connectivity.enabled";
        Boolean data = this.configuration.getBoolean(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetBooleanReturnsTrueWhenKeyExists() throws IOException, InterruptedException {
        String key = "database.connectivity.enabled";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        boolean data = this.configuration.getBoolean(key);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanReturnsTrueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "database.connectivity.enabled";
        Boolean data = this.configuration.getBoolean(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        boolean updatedData = this.configuration.getBoolean(key);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetBooleanReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "database.connectivity.enabled";
        boolean defaultValue = true;
        boolean data = this.configuration.getBoolean(key, defaultValue);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "database.connectivity.enabled";
        Boolean data = this.configuration.getBoolean(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        boolean updatedData = this.configuration.getBoolean(key, true);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetBooleanReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "database.connectivity.enabled";
        boolean defaultValue = true;
        Supplier<Boolean> defaultValueSupplier = () -> defaultValue;
        boolean data = this.configuration.getBoolean(key, defaultValueSupplier);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "database.connectivity.enabled";
        Boolean data = this.configuration.getBoolean(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Supplier<Boolean> defaultValueSupplier = () -> true;
        boolean updatedData = this.configuration.getBoolean(key, defaultValueSupplier);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetBooleanFieldReturnsNullWhenKeyDoesNotExist() {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .build();
        Boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertNull(data);
    }

    @Test
    void testGetBooleanFieldReturnsTrueWhenKeyExists() throws IOException, InterruptedException {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(dbConnectivity.name())));
        boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanFieldReturnsTrueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .build();
        Boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(dbConnectivity.name())));

        boolean updatedData = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetBooleanFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue(true)
                .build();
        boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanFieldReturnsUpdateValueIfValueExistsEvenIfFieldDefaultIsProvided() throws IOException, InterruptedException {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue(false)
                .build();
        Boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertFalse(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(dbConnectivity.name())));

        boolean updatedData = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetBooleanFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue(false)
                .build();
        boolean defaultValue = true;
        boolean data = this.configuration.getBoolean(dbConnectivity, defaultValue);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field dbConnectivity = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity")
                .desc("Database Connectivity Enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue(false)
                .build();
        Boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertFalse(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(dbConnectivity.name())));

        boolean defaultValue = true;
        boolean updatedData = this.configuration.getBoolean(dbConnectivity, defaultValue);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetBooleanFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field dbConnectivity = Field.builder()
            .name("database.connectivity.enabled")
            .displayName("Database Connectivity")
            .desc("Database Connectivity Enabled")
            .type(Field.Type.BOOLEAN)
            .defaultValue(false)
            .build();
        boolean defaultValue = true;
        BooleanSupplier defaultValueSupplier = () -> defaultValue;
        boolean data = this.configuration.getBoolean(dbConnectivity, defaultValueSupplier);
        Assertions.assertTrue(data);
    }

    @Test
    void testGetBooleanFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field dbConnectivity = Field.builder()
            .name("database.connectivity.enabled")
            .displayName("Database Connectivity")
            .desc("Database Connectivity Enabled")
            .type(Field.Type.BOOLEAN)
            .defaultValue(false)
            .build();
        Boolean data = this.configuration.getBoolean(dbConnectivity);
        Assertions.assertFalse(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(dbConnectivity.name())));

        BooleanSupplier defaultValueSupplier = () -> true;
        boolean updatedData = this.configuration.getBoolean(dbConnectivity, defaultValueSupplier);
        Assertions.assertTrue(updatedData);
    }

    @Test
    void testGetIntegerReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Integer data = this.configuration.getInteger(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetIntegerReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.count";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Integer data = this.configuration.getInteger(key);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(25, data);
    }

    @Test
    void testGetIntegerReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Integer data = this.configuration.getInteger(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Integer updatedData = this.configuration.getInteger(key);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetIntegerReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Integer defaultValue = 10;
        Integer data = this.configuration.getInteger(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetIntegerReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Integer data = this.configuration.getInteger(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Integer updatedData = this.configuration.getInteger(key, 20);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetIntegerReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Integer defaultValue = 10;
        Supplier<Integer> defaultValueSupplier = () -> defaultValue;
        Integer data = this.configuration.getInteger(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetIntegerReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Integer data = this.configuration.getInteger(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Supplier<Integer> defaultValueSupplier = () -> 20;
        Integer updatedData = this.configuration.getInteger(key, defaultValueSupplier);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetIntegerFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .build();
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertNull(data);
    }

    @Test
    void testGetIntegerFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(25, data);
    }

    @Test
    void testGetIntegerFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .build();
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Integer updatedData = this.configuration.getInteger(offersCountField);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetIntegerFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(10)
                .build();
        Integer defaultValue = 10;
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetIntegerFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(10)
                .build();
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Integer updatedData = this.configuration.getInteger(offersCountField);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetIntegerFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(10)
                .build();
        Integer defaultValue = 78;
        Integer data = this.configuration.getInteger(offersCountField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetIntegerFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(10)
                .build();
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Integer defaultValue = 78;
        Integer updatedData = this.configuration.getInteger(offersCountField, defaultValue);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetIntegerFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(10)
                .build();
        Integer defaultValue = 75;
        IntSupplier defaultValueSupplier = () -> defaultValue;
        Integer data = this.configuration.getInteger(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetIntegerFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(10)
                .build();
        Integer data = this.configuration.getInteger(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        IntSupplier defaultValueSupplier = () -> 20;
        Integer updatedData = this.configuration.getInteger(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Long data = this.configuration.getLong(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetLongReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.count";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Long data = this.configuration.getLong(key);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(25, data);
    }

    @Test
    void testGetLongReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Long data = this.configuration.getLong(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Long updatedData = this.configuration.getLong(key);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Long defaultValue = 10L;
        Long data = this.configuration.getLong(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetLongReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Long data = this.configuration.getLong(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Long updatedData = this.configuration.getLong(key, 20L);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Long defaultValue = 10L;
        Supplier<Long> defaultValueSupplier = () -> defaultValue;
        Long data = this.configuration.getLong(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetLongReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Long data = this.configuration.getLong(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Supplier<Long> defaultValueSupplier = () -> 20L;
        Long updatedData = this.configuration.getLong(key, defaultValueSupplier);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .build();
        Long data = this.configuration.getLong(offersCountField);
        Assertions.assertNull(data);
    }

    @Test
    void testGetLongFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));
        Long data = this.configuration.getLong(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(25, data);
    }

    @Test
    void testGetLongFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .build();
        Long data = this.configuration.getLong(offersCountField);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Long updatedData = this.configuration.getLong(offersCountField);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(10L)
                .build();
        Long data = this.configuration.getLong(offersCountField);
        Assertions.assertEquals(offersCountField.defaultValue(), data);
    }

    @Test
    void testGetLongFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(10L)
                .build();
        Long data = this.configuration.getLong(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Long updatedData = this.configuration.getLong(offersCountField);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(10L)
                .build();
        Long defaultValue = 74L;
        Long data = this.configuration.getLong(offersCountField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetLongFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(10L)
                .build();
        Long defaultValue = 74L;
        Long data = this.configuration.getLong(offersCountField, defaultValue);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(defaultValue, data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Long updatedData = this.configuration.getLong(offersCountField);
        Assertions.assertEquals(25, updatedData);
    }

    @Test
    void testGetLongFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(10L)
                .build();
        Long defaultValue = 89L;
        LongSupplier defaultValueSupplier = () -> defaultValue;
        Long data = this.configuration.getLong(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetLongFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(10L)
                .build();
        Long data = this.configuration.getLong(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        LongSupplier defaultValueSupplier = () -> 20L;
        Long updatedData = this.configuration.getLong(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(25, updatedData);
    }






    @Test
    void testShortLongReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Short data = this.configuration.getShort(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetShortReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Short expectedValue = 25;
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Short data = this.configuration.getShort(key);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetShortReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Short expectedValue = 25;
        Short data = this.configuration.getShort(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Short updatedData = this.configuration.getShort(key);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetShortReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Short defaultValue = 10;
        Short data = this.configuration.getShort(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetShortReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Short expectedValue = 25;
        Short defaultValue = 20;
        Short data = this.configuration.getShort(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Short updatedData = this.configuration.getShort(key, defaultValue);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetShortReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.count";
        Short defaultValue = 10;
        Supplier<Short> defaultValueSupplier = () -> defaultValue;
        Short data = this.configuration.getShort(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetShortReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.count";
        Short expectedValue = 25;
        Short defaultValue = 20;
        Supplier<Short> defaultValueSupplier = () -> defaultValue;
        Short data = this.configuration.getShort(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Short updatedData = this.configuration.getShort(key, defaultValueSupplier);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetShortFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .build();
        Short data = this.configuration.getShort(offersCountField);
        Assertions.assertNull(data);
    }

    @Test
    void testGetShortFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Short expectedValue = 25;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));
        Short data = this.configuration.getShort(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValue, data);
    }

    @Test
    void testGetShortFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Short expectedValue = 25;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .build();
        Short data = this.configuration.getShort(offersCountField);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Short updatedData = this.configuration.getShort(offersCountField);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetShortFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Short defaultValue = 10;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(defaultValue)
                .build();
        Short data = this.configuration.getShort(offersCountField);
        Assertions.assertEquals(offersCountField.defaultValue(), data);
    }

    @Test
    void testGetShortFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Short expectedValue = 25;
        Short defaultValue = 10;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(defaultValue)
                .build();
        Short data = this.configuration.getShort(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Short updatedData = this.configuration.getShort(offersCountField);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetShortFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Short defaultFieldValue = 10;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(defaultFieldValue)
                .build();
        Short defaultValue = 74;
        Short data = this.configuration.getShort(offersCountField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetShortFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Short defaultFieldValue = 10;
        Short expectedValue = 25;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(defaultFieldValue)
                .build();
        Short defaultValue = 74;
        Short data = this.configuration.getShort(offersCountField, defaultValue);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(defaultValue, data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Short updatedData = this.configuration.getShort(offersCountField);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetShortFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Short defaultFieldValue = 10;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(defaultFieldValue)
                .build();
        Short defaultValue = 89;
        Supplier<Short> defaultValueSupplier = () -> defaultValue;
        Short data = this.configuration.getShort(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetShortFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Short defaultFieldValue = 10;
        Short expectedValue = 25;
        Short defaultValue = 20;
        Supplier<Short> defaultValueSupplier = () -> defaultValue;
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(defaultFieldValue)
                .build();
        Short data = this.configuration.getShort(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Short updatedData = this.configuration.getShort(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(expectedValue, updatedData);
    }

    @Test
    void testGetFloatReturnsNullWhenKeyDoesNotExist() {
        String key = "application.bbsale.offer.percentage";
        Float data = this.configuration.getFloat(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetFloatReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Float data = this.configuration.getFloat(key);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(5.7f, data);
    }

    @Test
    void testGetFloatReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        Float data = this.configuration.getFloat(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Float updatedData = this.configuration.getFloat(key);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetFloatReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.bbsale.offer.percentage";
        Float defaultValue = 5.7f;
        Float data = this.configuration.getFloat(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFloatReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        Float data = this.configuration.getFloat(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Float updatedData = this.configuration.getFloat(key, 5.8f);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetFloatReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.bbsale.offer.percentage";
        Float defaultValue = 5.7f;
        Supplier<Float> defaultValueSupplier = () -> defaultValue;
        Float data = this.configuration.getFloat(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFloatReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        Float data = this.configuration.getFloat(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Supplier<Float> defaultValueSupplier = () -> 5.8f;
        Float updatedData = this.configuration.getFloat(key, defaultValueSupplier);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetFloatFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .build();
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertNull(data);
    }

    @Test
    void testGetFloatFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(5.7f, data);
    }

    @Test
    void testGetFloatFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .build();
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Float updatedData = this.configuration.getFloat(offersCountField);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetFloatFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.47f)
                .build();
        Float defaultValue = 87.47f;
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFloatFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.47f)
                .build();
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Float updatedData = this.configuration.getFloat(offersCountField);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetFloatFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.47f)
                .build();
        Float defaultValue = 55.47f;
        Float data = this.configuration.getFloat(offersCountField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFloatFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.47f)
                .build();
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Float defaultValue = 55.47f;
        Float updatedData = this.configuration.getFloat(offersCountField, defaultValue);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetFloatFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.47f)
                .build();
        Float defaultValue = 55.47f;
        Supplier<Float> defaultValueSupplier = () -> defaultValue;
        Float data = this.configuration.getFloat(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetFloatFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.47f)
                .build();
        Float data = this.configuration.getFloat(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Supplier<Float> defaultValueSupplier = () -> 5.8f;
        Float updatedData = this.configuration.getFloat(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(5.7f, updatedData);
    }

    @Test
    void testGetDoubleReturnsNullWhenKeyDoesNotExist() {
        String key = "application.bbsale.offer.percentage";
        Double data = this.configuration.getDouble(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetDoubleReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Double data = this.configuration.getDouble(key);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(5.7d, data);
    }

    @Test
    void testGetDoubleReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        Double data = this.configuration.getDouble(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Double updatedData = this.configuration.getDouble(key);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetDoubleReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.bbsale.offer.percentage";
        Double defaultValue = 5.7d;
        Double data = this.configuration.getDouble(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetDoubleReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        Double data = this.configuration.getDouble(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Double updatedData = this.configuration.getDouble(key, 5.8d);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetDoubleReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.bbsale.offer.percentage";
        Double defaultValue = 5.7d;
        Supplier<Double> defaultValueSupplier = () -> defaultValue;
        Double data = this.configuration.getDouble(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetDoubleReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.bbsale.offer.percentage";
        Double data = this.configuration.getDouble(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Supplier<Double> defaultValueSupplier = () -> 5.8d;
        Double updatedData = this.configuration.getDouble(key, defaultValueSupplier);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetDoubleFieldReturnsNullWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .build();
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertNull(data);
    }

    @Test
    void testGetDoubleFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(5.7d, data);
    }

    @Test
    void testGetDoubleFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .build();
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Double updatedData = this.configuration.getDouble(offersCountField);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetDoubleFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.27d)
                .build();
        Double defaultValue = 87.27d;
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetDoubleFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.27d)
                .build();
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(87.27d, data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Double updatedData = this.configuration.getDouble(offersCountField);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetDoubleFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.27d)
                .build();
        Double defaultValue = 47.27d;
        Double data = this.configuration.getDouble(offersCountField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetDoubleFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.27d)
                .build();
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(87.27d, data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Double defaultValue = 47.27d;
        Double updatedData = this.configuration.getDouble(offersCountField, defaultValue);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetDoubleFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.27d)
                .build();
        Double defaultValue = 47.27d;
        DoubleSupplier defaultValueSupplier = () -> defaultValue;
        Double data = this.configuration.getDouble(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetDoubleFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offersCountField = Field.builder()
                .name("application.bbsale.offer.percentage")
                .displayName("BB Sale Offer Percentage")
                .desc("The Offer percentage for the BB Sale")
                .type(Field.Type.FLOAT)
                .defaultValue(87.27d)
                .build();
        Double data = this.configuration.getDouble(offersCountField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offersCountField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        DoubleSupplier defaultValueSupplier = () -> 47.27d;
        Double updatedData = this.configuration.getDouble(offersCountField, defaultValueSupplier);
        Assertions.assertEquals(5.7d, updatedData);
    }

    @Test
    void testGetListReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.active.codes";
        List<String> data = this.configuration.getList(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetListReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        String[] expectedValues = {"25", "54", "745", "12"};
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        List<String> data = this.configuration.getList(key);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValues.length, data.size());
        Assertions.assertArrayEquals(expectedValues, data.toArray(new String[0]));
    }

    @Test
    void testGetListReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        List<String> data = this.configuration.getList(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String[] expectedValues = {"25", "54", "745", "12"};
        List<String> updatedData = this.configuration.getList(key);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.active.codes";
        List<String> defaultValue = new ArrayList<>(List.of("145", "754", "150", "44"));
        List<String> data = this.configuration.getList(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        List<String> data = this.configuration.getList(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String[] expectedValues = {"25", "54", "745", "12"};
        List<String> updatedData = this.configuration.getList(key, List.of("145", "754", "150", "44"));
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.active.codes";
        List<String> defaultValue = new ArrayList<>(List.of("145", "754", "150", "44"));
        Supplier<List<String>> defaultValueSupplier = () -> defaultValue;
        List<String> data = this.configuration.getList(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        List<String> data = this.configuration.getList(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        String[] expectedValues = {"25", "54", "745", "12"};
        Supplier<List<String>> defaultValueSupplier = () -> List.of("145", "754", "150", "44");
        List<String> updatedData = this.configuration.getList(key, defaultValueSupplier);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListFieldReturnsNullWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .build();
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertArrayEquals(new String[0], data.toArray(new String[0]));
    }

    @Test
    void testGetListFieldWithoutTypeReturnsNullWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .build();
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertArrayEquals(new String[0], data.toArray(new String[0]));
    }

    @Test
    void testGetListFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .build();
        String[] expectedValues = {"25", "54", "745", "12"};
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));
        List<String> data = this.configuration.getList(activeCodesListField);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValues.length, data.size());
        Assertions.assertArrayEquals(expectedValues, data.toArray(new String[0]));
    }

    @Test
    void testGetListFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .build();
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertArrayEquals(new String[0], data.toArray(new String[0]));

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        String[] expectedValues = {"25", "54", "745", "12"};
        List<String> updatedData = this.configuration.getList(activeCodesListField);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListFieldReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .defaultValue(List.of("145", "754", "150", "44"))
                .build();
        List<String> defaultValue = new ArrayList<>(List.of("145", "754", "150", "44"));
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .defaultValue(List.of("145", "754", "150", "44"))
                .build();
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(activeCodesListField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        String[] expectedValues = {"25", "54", "745", "12"};
        List<String> updatedData = this.configuration.getList(activeCodesListField);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .defaultValue(List.of("145", "754", "150", "44"))
                .build();
        List<String> defaultValue = new ArrayList<>(List.of("754", "11", "87", "63"));
        List<String> data = this.configuration.getList(activeCodesListField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .defaultValue(List.of("145", "754", "150", "44"))
                .build();
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(activeCodesListField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        String[] expectedValues = {"25", "54", "745", "12"};
        List<String> updatedData = this.configuration.getList(activeCodesListField, List.of("754", "11", "87", "63"));
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .defaultValue(List.of("145", "754", "150", "44"))
                .build();
        List<String> defaultValue = new ArrayList<>(List.of("754", "11", "87", "63"));
        Supplier<List<String>> defaultValueSupplier = () -> defaultValue;
        List<String> data = this.configuration.getList(activeCodesListField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(String.class)
                .defaultValue(List.of("145", "754", "150", "44"))
                .build();
        List<String> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(activeCodesListField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        String[] expectedValues = {"25", "54", "745", "12"};
        Supplier<List<String>> defaultValueSupplier = () -> List.of("754", "11", "87", "63");
        List<String> updatedData = this.configuration.getList(activeCodesListField, defaultValueSupplier);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new String[0]));
    }

    @Test
    void testGetListWithIntTypeReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.active.codes";
        List<Integer> data = this.configuration.getList(key, Integer.class);
        Assertions.assertArrayEquals(new Integer[0], data.toArray(new Integer[0]));
    }

    @Test
    void testGetListWithIntTypeReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        Integer[] expectedValues = {25, 54, 745, 12};
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        List<Integer> data = this.configuration.getList(key, Integer.class);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValues.length, data.size());
        Assertions.assertArrayEquals(expectedValues, data.toArray(new Integer[0]));
    }

    @Test
    void testGetListWithIntTypeReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        List<Integer> data = this.configuration.getList(key, Integer.class);
        Assertions.assertArrayEquals(new Integer[0], data.toArray(new Integer[0]));

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Integer[] expectedValues = {25, 54, 745, 12};
        List<Integer> updatedData = this.configuration.getList(key, Integer.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetListWithIntTypeReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.active.codes";
        List<Integer> defaultValue = new ArrayList<>(List.of(145, 754, 150, 44));
        List<Integer> data = this.configuration.getList(key, defaultValue, Integer.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListWithIntTypeReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        List<Integer> data = this.configuration.getList(key, Integer.class);
        Assertions.assertArrayEquals(new Integer[0], data.toArray(new Integer[0]));

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Integer[] expectedValues = {25, 54, 745, 12};
        List<Integer> updatedData = this.configuration.getList(key, List.of(145, 754, 150, 44), Integer.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetListWithIntTypeReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.active.codes";
        List<Integer> defaultValue = new ArrayList<>(List.of(145, 754, 150, 44));
        Supplier<List<Integer>> defaultValueSupplier = () -> defaultValue;
        List<Integer> data = this.configuration.getList(key, defaultValueSupplier, Integer.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListWithIntTypeReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.active.codes";
        List<Integer> data = this.configuration.getList(key, Integer.class);
        Assertions.assertArrayEquals(new Integer[0], data.toArray(new Integer[0]));

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Integer[] expectedValues = {25, 54, 745, 12};
        Supplier<List<Integer>> defaultValueSupplier = () -> List.of(145, 754, 150, 44);
        List<Integer> updatedData = this.configuration.getList(key, defaultValueSupplier, Integer.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetListFieldWithIntTypeReturnsNullWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .build();
        List<Integer> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertArrayEquals(new Integer[0], data.toArray(new Integer[0]));
    }

    @Test
    void testGetListFieldWithIntTypeReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .build();
        Integer[] expectedValues = {25, 54, 745, 12};
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));
        List<Integer> data = this.configuration.getList(activeCodesListField);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValues.length, data.size());
        Assertions.assertArrayEquals(expectedValues, data.toArray(new Integer[0]));
    }

    @Test
    void testGetListFieldWithIntTypeReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .build();
        List<Integer> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertArrayEquals(new Integer[0], data.toArray(new Integer[0]));

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        Integer[] expectedValues = {25, 54, 745, 12};
        List<Integer> updatedData = this.configuration.getList(activeCodesListField);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetListFieldWithIntTypeReturnsDefaultFieldValueWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .defaultValue(List.of(145, 754, 150, 44))
                .build();
        List<Integer> defaultValue = new ArrayList<>(List.of(145, 754, 150, 44));
        List<Integer> data = this.configuration.getList(activeCodesListField);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListFieldWithIntTypeReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .defaultValue(List.of(145, 754, 150, 44))
                .build();
        List<Integer> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(activeCodesListField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        Integer[] expectedValues = {25, 54, 745, 12};
        List<Integer> updatedData = this.configuration.getList(activeCodesListField);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetListFieldWithIntTypeReturnsDefaultValueWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .defaultValue(List.of(145, 754, 150, 44))
                .build();
        List<Integer> defaultValue = new ArrayList<>(List.of(754, 11, 87, 63));
        List<Integer> data = this.configuration.getList(activeCodesListField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListFieldWithIntTypeReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .defaultValue(List.of(145, 754, 150, 44))
                .build();
        List<Integer> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(activeCodesListField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        Integer[] expectedValues = {25, 54, 745, 12};
        List<Integer> updatedData = this.configuration.getList(activeCodesListField, List.of(754, 11, 87, 63));
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetListFieldWithIntTypeReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .defaultValue(List.of(145, 754, 150, 44))
                .build();
        List<Integer> defaultValue = new ArrayList<>(List.of(754, 11, 87, 63));
        Supplier<List<Integer>> defaultValueSupplier = () -> defaultValue;
        List<Integer> data = this.configuration.getList(activeCodesListField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetListFieldWithIntTypeReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.LIST)
                .className(Integer.class)
                .defaultValue(List.of(145, 754, 150, 44))
                .build();
        List<Integer> data = this.configuration.getList(activeCodesListField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(activeCodesListField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(activeCodesListField.name())));

        Integer[] expectedValues = {25, 54, 745, 12};
        Supplier<List<Integer>> defaultValueSupplier = () -> List.of(754, 11, 87, 63);
        List<Integer> updatedData = this.configuration.getList(activeCodesListField, defaultValueSupplier);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.length, updatedData.size());
        Assertions.assertArrayEquals(expectedValues, updatedData.toArray(new Integer[0]));
    }

    @Test
    void testGetMapReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.features";
        Map<String, Object> data = this.configuration.getMap(key);
        Assertions.assertNull(data);
    }

    @Test
    void testGetMapReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.features";
        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        Map<String, Object> data = this.configuration.getMap(key);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValues.size(), data.size());
        Assertions.assertEquals(expectedValues, data);
    }

    @Test
    void testGetMapReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.features";
        Map<String, Object> data = this.configuration.getMap(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(key);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }

    @Test
    void testGetMapReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.features";
        Map<String, Object> defaultValue = new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap();
        Map<String, Object> data = this.configuration.getMap(key, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetMapReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.features";
        Map<String, Object> data = this.configuration.getMap(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(key, new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap());
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }

    @Test
    void testGetMapReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.features";
        Map<String, Object> defaultValue = new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap();
        Supplier<Map<String, Object>> defaultValueSupplier = () -> defaultValue;
        Map<String, Object> data = this.configuration.getMap(key, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetMapReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.features";
        Map<String, Object> data = this.configuration.getMap(key);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Supplier<Map<String, Object>> defaultValueSupplier = () -> new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(key, defaultValueSupplier);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }
    
    @Test
    void testGetMapFieldReturnsNullWhenKeyDoesNotExist() {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .build();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        Assertions.assertNull(data);
    }

    @Test
    void testGetMapFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .build();
        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField.name())));
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expectedValues.size(), data.size());
        Assertions.assertEquals(expectedValues, data);
    }

    @Test
    void testGetMapFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .build();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField.name())));

        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(offerFeaturesField);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }

    @Test
    void testGetMapFieldReturnsDefaultValueFieldWhenKeyDoesNotExist() {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .defaultValue(new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap())
                .build();
        Map<String, Object> defaultValue = new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetMapFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .defaultValue(new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap())
                .build();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(((Map<?, ?>) offerFeaturesField.defaultValue()).size(), data.size());
        Assertions.assertEquals(offerFeaturesField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField)));

        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(offerFeaturesField);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }

    @Test
    void testGetMapFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .defaultValue(new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap())
                .build();
        Map<String, Object> defaultValue = new JSONObject("{\"v1\": {\"enabled\": \"false\"}}").toMap();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField, defaultValue);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetMapFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .defaultValue(new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap())
                .build();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(((Map<?, ?>) offerFeaturesField.defaultValue()).size(), data.size());
        Assertions.assertEquals(offerFeaturesField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField)));

        Map<String, Object> defaultValue = new JSONObject("{\"v1\": {\"enabled\": \"false\"}}").toMap();
        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(offerFeaturesField, defaultValue);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }

    @Test
    void testGetMapFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .defaultValue(new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap())
                .build();
        Map<String, Object> defaultValue = new JSONObject("{\"v1\": {\"enabled\": \"false\"}}").toMap();
        Supplier<Map<String, Object>> defaultValueSupplier = () -> defaultValue;
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField, defaultValueSupplier);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetMapFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.MAP)
                .defaultValue(new JSONObject("{\"v1\": {\"enabled\": \"false\"}, \"displayFeatures\": \"false\", \"displayText\": \"Offers Not available\"}").toMap())
                .build();
        Map<String, Object> data = this.configuration.getMap(offerFeaturesField);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(((Map<?, ?>) offerFeaturesField.defaultValue()).size(), data.size());
        Assertions.assertEquals(offerFeaturesField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField.name())));

        Map<String, Object> expectedValues = new JSONObject("{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}").toMap();
        Supplier<Map<String, Object>> defaultValueSupplier = () -> new JSONObject("{\"v1\": {\"enabled\": \"false\"}}").toMap();
        Map<String, Object> updatedData = this.configuration.getMap(offerFeaturesField, defaultValueSupplier);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(expectedValues.size(), updatedData.size());
        Assertions.assertEquals(expectedValues, updatedData);
    }
    
    @Test
    void testGetObjectReturnsNullWhenKeyDoesNotExist() {
        String key = "application.offers.features";
        OffersFeaturesDto data = this.configuration.getObject(key, OffersFeaturesDto.class);
        Assertions.assertNull(data);
    }

    @Test
    void testGetObjectReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        String key = "application.offers.features";
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));
        OffersFeaturesDto data = this.configuration.getObject(key, OffersFeaturesDto.class);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(data));
    }

    @Test
    void testGetObjectReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        String key = "application.offers.features";
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto data = this.configuration.getObject(key, OffersFeaturesDto.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        OffersFeaturesDto updatedData = this.configuration.getObject(key, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

    @Test
    void testGetObjectReturnsDefaultValueWhenKeyDoesNotExist() {
        String key = "application.offers.features";
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();

        OffersFeaturesDto data = this.configuration.getObject(key, defaultValue, OffersFeaturesDto.class);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetObjectReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.features";
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();

        OffersFeaturesDto data = this.configuration.getObject(key, OffersFeaturesDto.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        OffersFeaturesDto updatedData = this.configuration.getObject(key, defaultValue, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

    @Test
    void testGetObjectReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        String key = "application.offers.features";
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();

        Supplier<OffersFeaturesDto> defaultValueSupplier = () -> defaultValue;
        OffersFeaturesDto data = this.configuration.getObject(key, defaultValueSupplier, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetObjectReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException, InterruptedException {
        String key = "application.offers.features";
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();

        OffersFeaturesDto data = this.configuration.getObject(key, OffersFeaturesDto.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(key)));

        Supplier<OffersFeaturesDto> defaultValueSupplier = () -> defaultValue;
        OffersFeaturesDto updatedData = this.configuration.getObject(key, defaultValueSupplier, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

    @Test
    void testGetObjectFieldReturnsNullWhenKeyDoesNotExist() {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        Assertions.assertNull(data);
    }

    @Test
    void testGetObjectFieldReturnsValueWhenKeyExists() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .build();
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField.name())));
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        logger().info(data.toString());
        Assertions.assertNotNull(data);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(data));
    }

    @Test
    void testGetObjectFieldReturnsValueWhenKeyExistsAndValueIsUpdated() throws IOException, InterruptedException {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .build();
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        Assertions.assertNull(data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField.name())));

        OffersFeaturesDto updatedData = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

    @Test
    void testGetObjectFieldReturnsDefaultValueFieldWhenKeyDoesNotExist() throws IOException {
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .defaultValue(defaultValue)
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(this.mapper.writeValueAsString(defaultValue), this.mapper.writeValueAsString(data));
    }

    @Test
    void testGetObjectFieldReturnsUpdateValueIfValueExistsEvenIfDefaultFieldIsProvided() throws IOException {
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();

        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .defaultValue(defaultValue)
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offerFeaturesField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField)));

        OffersFeaturesDto updatedData = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

    @Test
    void testGetObjectFieldReturnsDefaultValueWhenKeyDoesNotExist() {
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder().v1Features(new OffersFeaturesDto.V1FeaturesDto(false)).build();
        OffersFeaturesDto defaultFieldValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .defaultValue(defaultFieldValue)
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, defaultValue, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetObjectFieldReturnsUpdateValueIfValueExistsEvenIfDefaultIsProvided() throws IOException {
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder().v1Features(new OffersFeaturesDto.V1FeaturesDto(false)).build();
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto defaultFieldValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .defaultValue(defaultFieldValue)
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offerFeaturesField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField)));

        OffersFeaturesDto updatedData = this.configuration.getObject(offerFeaturesField, defaultValue, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

    @Test
    void testGetObjectFieldReturnsDefaultValueSupplierWhenKeyDoesNotExist() {
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder().v1Features(new OffersFeaturesDto.V1FeaturesDto(false)).build();
        OffersFeaturesDto defaultFieldValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .defaultValue(defaultFieldValue)
                .build();
        Supplier<OffersFeaturesDto> defaultValueSupplier = () -> defaultValue;
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, defaultValueSupplier, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(defaultValue, data);
    }

    @Test
    void testGetObjectFieldReturnsUpdateValueIfValueSupplierExistsEvenIfDefaultIsProvided() throws IOException {
        OffersFeaturesDto defaultValue = OffersFeaturesDto.builder().v1Features(new OffersFeaturesDto.V1FeaturesDto(false)).build();
        OffersFeaturesDto expectedValues = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(true))
                .displayFeatures(true)
                .displayText("Offer Features")
                .build();
        OffersFeaturesDto defaultFieldValue = OffersFeaturesDto.builder()
                .v1Features(new OffersFeaturesDto.V1FeaturesDto(false))
                .displayFeatures(false)
                .displayText("Offers Not available")
                .build();
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .defaultValue(defaultFieldValue)
                .build();
        OffersFeaturesDto data = this.configuration.getObject(offerFeaturesField, OffersFeaturesDto.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(offerFeaturesField.defaultValue(), data);

        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offerFeaturesField.name())));

        Supplier<OffersFeaturesDto> defaultValueSupplier = () -> defaultValue;
        OffersFeaturesDto updatedData = this.configuration.getObject(offerFeaturesField, defaultValueSupplier, OffersFeaturesDto.class);
        logger().info(updatedData.toString());
        Assertions.assertNotNull(updatedData);
        Assertions.assertEquals(this.mapper.writeValueAsString(expectedValues), this.mapper.writeValueAsString(updatedData));
    }

}
