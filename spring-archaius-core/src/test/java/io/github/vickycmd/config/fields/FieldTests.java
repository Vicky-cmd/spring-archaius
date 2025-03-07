package io.github.vickycmd.config.fields;

import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import com.netflix.config.sources.URLConfigurationSource;
import io.github.vickycmd.config.ArchaiusTests;
import io.github.vickycmd.config.Configuration;
import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.model.ApplicationProperty;
import io.github.vickycmd.config.utils.Utilities;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.vickycmd.config.utils.TestUtilities.triggerConfigReload;


@TestPropertySource(locations = {"classpath:application-test.properties"})
class FieldTests extends ArchaiusTests {

    @Autowired
    private Configuration configuration;

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
    void testFieldValidationFailsWithWrongConfiguration() throws IOException {
        Field.Set ALL_FIELDS = this.getAllFields();
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testFieldValidationPassesWithProperConfiguration() throws IOException {
        Field.Set ALL_FIELDS = this.getAllFields();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString("application.offers.features")));

        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));

    }

    @Test
    void testIsRequiredFailsStartupValidationWithoutAnyConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsRequiredFailsAspectValidationWithoutAnyConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsRequiredSucceedsStartupValidationWithProperConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsRequiredSucceedsAspectValidationWithoutProperConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .required()
                .build();
        updateConfigPropertiesFile(getApplicationProperties());
        Awaitility.await()
                .atMost(500, TimeUnit.SECONDS)
                .until(() -> StringUtils.hasText(this.configuration.getString(offersCountField.name())));

        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongFailsStartupValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue("ABC")
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongFailsAspectValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue("ABC")
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsStartupValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(45)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(45)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsStartupValidationWithNonNegativeValueWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(-175)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithNonNegativeValueWithInvalidDataTypeWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .defaultValue(-175)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongFailsStartupValidationWithPositiveValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isPositiveLongValidator)
                .defaultValue(-1)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongFailsAspectValidationWithPositiveValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isPositiveLongValidator)
                .defaultValue(-1)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongFailsAspectValidationWithPositiveValueWithInvalidDataTypeZero() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isPositiveLongValidator)
                .defaultValue(0)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsStartupValidationWithPositiveValueWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isPositiveLongValidator)
                .defaultValue(45)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithPositiveValueWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isPositiveLongValidator)
                .defaultValue(75)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithPositiveValueWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isPositiveLongValidator)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongFailsStartupValidationWithNonNegativeValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isNonNegativeLongValidator)
                .defaultValue(-1)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongFailsAspectValidationWithNonNegativeValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isNonNegativeLongValidator)
                .defaultValue(-1)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsStartupValidationWithNonNegativeValueWithValidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isNonNegativeLongValidator)
                .defaultValue(0)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithNonNegativeValueWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isNonNegativeLongValidator)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsLongSucceedsAspectValidationWithNonNegativeValueWithValidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isNonNegativeLongValidator)
                .defaultValue(0)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getLong(offersCountField));
    }

    @Test
    void testIsIntegerFailsStartupValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue("ABC")
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerFailsAspectValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerSucceedsStartupValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(45)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(45)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithNullValueConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }



    @Test
    void testIsIntegerSucceedsStartupValidationWithNonNegativeValueWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(-175)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithNonNegativeValueWithInvalidDataTypeWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .defaultValue(-175)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerFailsStartupValidationWithPositiveValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isPositiveIntegerValidator)
                .defaultValue(-1)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerFailsAspectValidationWithPositiveValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isPositiveIntegerValidator)
                .defaultValue(-1)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerFailsAspectValidationWithPositiveValueWithInvalidDataTypeZero() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isPositiveIntegerValidator)
                .defaultValue(0)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerSucceedsStartupValidationWithPositiveValueWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isPositiveIntegerValidator)
                .defaultValue(45)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithPositiveValueWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isPositiveIntegerValidator)
                .defaultValue(75)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithPositiveValueWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isPositiveIntegerValidator)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerFailsStartupValidationWithNonNegativeValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isNonNegativeIntegerValidator)
                .defaultValue(-1)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerFailsAspectValidationWithNonNegativeValueWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isNonNegativeIntegerValidator)
                .defaultValue(-1)
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerSucceedsStartupValidationWithNonNegativeValueWithValidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isNonNegativeIntegerValidator)
                .defaultValue(0)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithNonNegativeValueWithValidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isNonNegativeIntegerValidator)
                .defaultValue(0)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsIntegerSucceedsAspectValidationWithNonNegativeValueWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.INT)
                .validator(Field.isNonNegativeIntegerValidator)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getInteger(offersCountField));
    }

    @Test
    void testIsBooleanFailsStartupValidationWithWithInvalidDataType() throws IOException {
        Field dbField = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity Flag")
                .desc("The toggle to control if the database connectivity is enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue("ABC")
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(dbField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsBooleanFailsAspectValidationWithWithInvalidDataType() throws IOException {
        Field dbField = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity Flag")
                .desc("The toggle to control if the database connectivity is enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue("ABC")
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getBoolean(dbField));
    }

    @Test
    void testIsBooleanSucceedsStartupValidationWithValidConfiguration() throws IOException {
        Field dbField = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity Flag")
                .desc("The toggle to control if the database connectivity is enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue(true)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(dbField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsBooleanSucceedsAspectValidationWithValidConfiguration() throws IOException {
        Field dbField = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity Flag")
                .desc("The toggle to control if the database connectivity is enabled")
                .type(Field.Type.BOOLEAN)
                .defaultValue(true)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getBoolean(dbField));
    }

    @Test
    void testIsBooleanSucceedsAspectValidationWithNoValueInConfiguration() throws IOException {
        Field dbField = Field.builder()
                .name("database.connectivity.enabled")
                .displayName("Database Connectivity Flag")
                .desc("The toggle to control if the database connectivity is enabled")
                .type(Field.Type.BOOLEAN)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getBoolean(dbField));
    }

    @Test
    void testIsShortFailsStartupValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue("ABC")
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsShortFailsAspectValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue("ABC")
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getShort(offersCountField));
    }

    @Test
    void testIsShortSucceedsStartupValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(45)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsShortSucceedsAspectValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(45)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getShort(offersCountField));
    }

    @Test
    void testIsShortSucceedsAspectValidationWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getShort(offersCountField));
    }

    @Test
    void testIsShortSucceedsStartupValidationWithNonNegativeValueWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(-175)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsShortSucceedsAspectValidationWithNonNegativeValueWithInvalidDataTypeWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.SHORT)
                .defaultValue(-175)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getShort(offersCountField));
    }

    @Test
    void testIsDoubleFailsStartupValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .defaultValue("ABC")
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertThrowsExactly(ConfigException.class, () -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsDoubleFailsAspectValidationWithWithInvalidDataType() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .defaultValue("ABC")
                .required()
                .build();
        Assertions.assertThrowsExactly(ConfigException.class, () -> this.configuration.getDouble(offersCountField));
    }

    @Test
    void testIsDoubleSucceedsStartupValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .defaultValue(45.24)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsDoubleSucceedsAspectValidationWithValidConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .defaultValue(45.24)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getDouble(offersCountField));
    }

    @Test
    void testIsDoubleSucceedsAspectValidationWithNoValueInConfiguration() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getDouble(offersCountField));
    }

    @Test
    void testIsDoubleSucceedsStartupValidationWithNonNegativeValueWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .defaultValue(-175.784)
                .required()
                .build();
        Field.Set ALL_FIELDS = Field.Set.of(offersCountField);
        Assertions.assertDoesNotThrow(() -> Utilities.validateConfig(this.configuration, ALL_FIELDS, this.getClass(), logger()));
    }

    @Test
    void testIsDoubleSucceedsAspectValidationWithNonNegativeValueWithInvalidDataTypeWithoutAdditonalValidations() throws IOException {
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.DOUBLE)
                .defaultValue(-175.784)
                .required()
                .build();
        Assertions.assertDoesNotThrow(() -> this.configuration.getDouble(offersCountField));
    }

    private Field.Set getAllFields() {
        Field offerFeaturesField = Field.builder()
                .name("application.offers.features")
                .displayName("Offer Features")
                .desc("The map containing configuration related to different offer features")
                .type(Field.Type.OBJECT)
                .importance(Field.Importance.HIGH)
                .required()
                .build();
        Field offersVersion = Field.builder()
                .name("application.offers.version")
                .displayName("Offers Version")
                .desc("Application Offers Version")
                .type(Field.Type.STRING)
                .allowedValues("v1", "v2", "v3")
                .defaultValueGenerator(() -> "v2")
                .build();
        Field offersCountField = Field.builder()
                .name("application.offers.count")
                .displayName("Offers Count")
                .desc("Total Offers Count in the Application")
                .type(Field.Type.LONG)
                .validator(Field.isLongValidator)
                .required()
                .build();
        Field activeCodesListField = Field.builder()
                .name("application.offers.active.codes")
                .displayName("Active Offers Codes")
                .desc("The list of offer codes for the active offers in the system")
                .type(Field.Type.STRING)
                .build();
        return Field.Set.of(offerFeaturesField, offersVersion, offersCountField, activeCodesListField);
    }

}
