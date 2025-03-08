package io.github.vickycmd.config.fields;

import io.github.vickycmd.config.Configuration;
import io.vavr.control.Try;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.util.Tuple;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.function.Supplier;

/**
 * The Field class represents a configuration field within the application, encapsulating
 * various attributes such as name, type, default value, and validation logic. It is designed
 * to facilitate the definition and validation of configuration properties, ensuring that
 * they adhere to specified constraints and types.
 *
 * <p>This class provides a builder pattern through the nested FieldBuilder class, allowing
 * for flexible and fluent creation of Field instances. The Field class supports various
 * data types, including STRING, INT, BOOLEAN, and more, and provides mechanisms for
 * validating these types using custom validators.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Support for default values using a Supplier.</li>
 *   <li>Validation of field values using custom and type-specific validators.</li>
 *   <li>Support for specifying allowed values and marking fields as required.</li>
 *   <li>Integration with the Configuration class for retrieving and validating configuration values.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Field field = Field.builder()
 *     .name("property.name")
 *     .type(Field.Type.STRING)
 *     .defaultValue("default value")
 *     .validator(Field.isRequiredValidator)
 *     .build();
 * }
 * </pre>
 *
 * <p>This class works in conjunction with:</p>
 * <ul>
 *   <li>{@link io.github.vickycmd.config.Configuration} - For managing configuration properties.</li>
 *   <li>{@link io.github.vickycmd.config.errors.ConfigException} - For handling configuration-related errors.</li>
 * </ul>
 *
 * <p>Note: This class is annotated with @Slf4j for logging purposes.</p>
 *
 * <p>Author: Vicky CMD</p>
 * <p>Version: 1.0</p>
 * <p>Since: 1.0</p>
 */
@Slf4j
public class Field {

    private final String name;
    private final String displayName;
    private final String desc;
    private final Supplier<Object> defaultValueGenerator;
    private final Validator validator;
    private final Type type;
    private final Class<?> className;
    private final Importance importance;
    private final java.util.Set<?> allowedValues;
    private final boolean isRequired;

    /**
     * Constructs a Field instance with the specified parameters.
     *
     * @param name the unique name of the field
     * @param displayName the display name of the field
     * @param desc a description of the field
     * @param defaultValueGenerator a supplier for generating the default value of the field
     * @param validator a validator for validating the field's value
     * @param type the data type of the field
     * @param className the class type associated with the field
     * @param importance the importance level of the field
     * @param allowedValues a set of allowed values for the field
     * @param isRequired a flag indicating if the field is required
     */
    private Field(String name, String displayName, String desc, Supplier<Object> defaultValueGenerator,
                  Validator validator, Type type, Class<?> className, Importance importance,
                  java.util.Set<?> allowedValues, boolean isRequired) {
        this.name = name;
        this.displayName = displayName;
        this.desc = desc;
        this.defaultValueGenerator = defaultValueGenerator;
        this.validator = validator;
        this.type = type;
        this.className = className;
        this.importance = importance;
        this.allowedValues = allowedValues;
        this.isRequired = isRequired;
    }

    /**
     * The FieldBuilder class provides a builder pattern for constructing instances of the Field class.
     * It allows for the flexible and fluent creation of Field objects by setting various attributes
     * such as name, display name, description, default value, type, validator, class type, importance,
     * allowed values, and whether the field is required.
     *
     * <p>This builder supports setting default values either directly or through a Supplier, and
     * allows chaining of multiple validators, including custom validators defined as tuples of
     * StartupValidator and AspectValidator. The builder also provides methods for specifying
     * allowed values as a collection or varargs, and for marking a field as required.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field field = Field.builder()
     *     .name("property.name")
     *     .type(Field.Type.STRING)
     *     .defaultValue("default value")
     *     .validator(Field.isRequiredValidator)
     *     .build();
     * }
     * </pre>
     *
     * <p>Related classes:</p>
     * <ul>
     *   <li>{@link Field} - The class being constructed by this builder.</li>
     *   <li>{@link io.github.vickycmd.config.Configuration} - For managing configuration properties.</li>
     *   <li>{@link io.github.vickycmd.config.errors.ConfigException} - For handling configuration-related errors.</li>
     * </ul>
     *
     * <p>Note: This class is annotated with @NoArgsConstructor to provide a no-argument constructor.</p>
     *
     * <p>Author: Vicky CMD</p>
     * <p>Version: 1.0</p>
     * <p>Since: 1.0</p>
     */
    @NoArgsConstructor
    public static class FieldBuilder {
        private String name;
        private String displayName;
        private String desc;
        private Supplier<Object> defaultValueGenerator = () -> null;
        private Validator validator = null;
        private Type type = Type.STRING;
        private Class<?> className = String.class;
        private Importance importance = Importance.LOW;
        private java.util.Set<?> allowedValues = Collections.emptySet();
        private boolean isRequired = false;

        /**
         * Builds and returns a new instance of the Field class using the current state
         * of the FieldBuilder. The constructed Field object will have its attributes
         * set according to the values specified in the builder.
         *
         * @return a new Field instance with the configured attributes
         */
        public Field build() {
            return new Field(this.name, this.displayName, this.desc, this.defaultValueGenerator, this.validator,
                    this.type, this.className, this.importance, this.allowedValues, this.isRequired);
        }

        /**
         * Sets the name attribute for the FieldBuilder and returns the builder instance
         * to allow for method chaining. This is the actual property for which the value has to be fetched
         *
         * @param name the name to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the display name attribute for the FieldBuilder and returns the builder instance
         * to allow for method chaining.
         *
         * @param displayName the display name to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the description attribute for the FieldBuilder and returns the builder instance
         * to allow for method chaining.
         *
         * @param desc the description to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder desc(String desc) {
            this.desc = desc;
            return this;
        }

        /**
         * Sets the default value for the FieldBuilder and returns the builder instance
         * to allow for method chaining. The default value is provided directly and
         * stored as a Supplier.
         *
         * @param defaultValue the default value to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder defaultValue(Object defaultValue) {
            this.defaultValueGenerator = () -> defaultValue;
            return this;
        }

        /**
         * Sets the default value generator for the FieldBuilder and returns the builder instance
         * to allow for method chaining. The default value generator is a Supplier that provides
         * the default value when needed.
         *
         * @param defaultValueGenerator the Supplier to set as the default value generator
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder defaultValueGenerator(Supplier<Object> defaultValueGenerator) {
            this.defaultValueGenerator = defaultValueGenerator;
            return this;
        }

        /**
         * Sets the type attribute for the FieldBuilder and returns the builder instance
         * to allow for method chaining.
         *
         * @param type the Type to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder type(Type type) {
            this.type = type;
            return this;
        }

        /**
         * Adds one or more validators to the FieldBuilder and returns the builder instance
         * to allow for method chaining. Each provided validator is combined with the existing
         * validator using logical 'and' operation.
         *
         * @param validators one or more Validator instances to add to the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder validator(Validator... validators) {
            for (Validator additionalValidator: validators) {
                if (additionalValidator!=null) {
                    this.validator = additionalValidator.and(this.validator);
                }
            }
            return this;
        }

        /**
         * Adds one or more validators to the FieldBuilder and returns the builder instance
         * to allow for method chaining. Each provided validator is a tuple of StartupValidator
         * and AspectValidator, and is combined with the existing validator using logical 'and' operation.
         *
         * @param validators one or more tuples of StartupValidator and AspectValidator to add
         * @return the current instance of FieldBuilder for method chaining
         */
        @SafeVarargs
        public final FieldBuilder validator(Tuple<StartupValidator, AspectValidator>... validators) {
            for (Tuple<StartupValidator, AspectValidator> additionalValidator: validators) {
                if (additionalValidator!=null) {
                    this.validator = createValidator(additionalValidator).and(this.validator);
                }
            }
            return this;
        }

        /**
         * Sets the class type attribute for the FieldBuilder and returns the builder instance
         * to allow for method chaining.
         *
         * @param className the Class type to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder className(Class<?> className) {
            this.className = className;
            return this;
        }

        /**
         * Sets the importance level for the FieldBuilder and returns the builder instance
         * to allow for method chaining.
         *
         * @param importance the Importance level to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder importance(Importance importance) {
            this.importance = importance;
            return this;
        }

        /**
         * Sets the allowed values for the FieldBuilder using a collection and returns the builder instance
         * to allow for method chaining. The provided collection is converted into a HashSet to ensure
         * uniqueness of the allowed values.
         *
         * @param allowedValues a collection of allowed values to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder allowedValues(java.util.Collection<?> allowedValues) {
            this.allowedValues = new HashSet<>(allowedValues);
            return this;
        }

        /**
         * Sets the allowed values for the FieldBuilder using varargs and returns the builder instance
         * to allow for method chaining. The provided values are converted into a Set to ensure
         * uniqueness of the allowed values.
         *
         * @param allowedValues one or more allowed values to set for the FieldBuilder
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder allowedValues(Object ...allowedValues) {
            this.allowedValues = java.util.Set.of(allowedValues);
            return this;
        }

        /**
         * Marks the field as required and adds a required validator to the FieldBuilder.
         * This method sets the isRequired flag to true and appends the isRequiredValidator
         * to the existing validators, ensuring that the field must be provided.
         *
         * @return the current instance of FieldBuilder for method chaining
         */
        public FieldBuilder required() {
            this.isRequired = true;
            this.validator(createValidator(isRequiredValidator));
            return this;
        }
    }

    /**
     * Returns a new instance of FieldBuilder, enabling the construction of Field objects
     * using the builder pattern. This method provides a convenient entry point for
     * creating and configuring Field instances with various attributes.
     *
     * @return a new FieldBuilder instance
     */
    public static FieldBuilder builder() {
        return new FieldBuilder();
    }


    /**
     * Creates a new instance of FieldBuilder with the specified name.
     *
     * @param name the name to set for the FieldBuilder
     * @return a FieldBuilder instance with the name set
     */
    public static FieldBuilder create(String name) {
        return Field.builder().name(name);
    }

    /**
     * Creates a new instance of FieldBuilder with the specified name and type.
     *
     * @param name the name to set for the FieldBuilder
     * @param type the type to set for the FieldBuilder
     * @return a FieldBuilder instance with the name and type set
     */
    public static FieldBuilder create(String name, Type type) {
        return Field.builder().name(name).type(type);
    }

    /**
     * Creates a new instance of FieldBuilder with the specified name, type, and class type.
     *
     * @param name the name to set for the FieldBuilder
     * @param type the type to set for the FieldBuilder
     * @param className the class type to set for the FieldBuilder
     * @return a FieldBuilder instance with the name, type, and class type set
     */
    public static FieldBuilder create(String name, Type type, Class<?> className) {
        return Field.builder().name(name).type(type).className(className);
    }

    /**
     * Returns the name of the field.
     *
     * @return the name of the field as a String
     */
    public String name() {
        return name;
    }

    /**
     * Returns the default value as a string.
     *
     * @return the string representation of the default value if it is not null;
     *         otherwise, returns null.
     */
    public String defaultValueAsString() {
        return defaultValue()!=null?defaultValue().toString():null;
    }

    /**
     * Retrieves the default value by invoking the default value generator.
     *
     * @param <T> the type of the default value
     * @return the default value generated
     */
    @SuppressWarnings("unchecked")
    public <T> T defaultValue() {
        return (T) defaultValueGenerator.get();
    }

    /**
     * Returns the description of the field.
     *
     * @return the description of the field as a String
     */
    public String description() {
        return desc;
    }

    /**
     * Returns the display name of the field.
     *
     * @return the display name of the field as a String
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the type of the field.
     *
     * @return the type of the field as a Type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the class type of the field. This is used with the {@link Field} of {@link Type#CLASS} to
     * specify the class type of the field.
     *
     * @return the class type of the field as a Class
     */
    public Class<?> className() {
        return className;
    }

    /**
     * Returns the importance level of the field.
     *
     * @return the importance level of the field as an Importance
     */
    public Importance importance() {
        return importance;
    }

    /**
     * Returns the validator of the field.
     *
     * @return the validator of the field as a Validator
     */
    public Validator validator() {
        return validator;
    }

    /**
     * Returns whether the field is required.
     *
     * @return true if the field is required, false otherwise
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Returns the set of allowed values for the field.
     *
     * @return the set of allowed values for the field as a Set
     */
    public java.util.Set<?> allowedValues() {
        return allowedValues;
    }


    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Field && ((Field) obj).name().equals(this.name);
    }

    @Override
    public String toString() {
        return String.format("Field(%s)", this.name);
    }

    /**
     * Validates the current field using the specified configuration and records any validation problems.
     *
     * @param config the Configuration instance used for validation
     * @param problems a ValidationOutput to record any validation issues
     * @return true if the field is valid, false if there are validation errors
     */
    public boolean validate(Configuration config, ValidationOutput problems) {
        Validator typeValidator = validatorForType(this.type);
        int errors = 0;
        if (typeValidator!=null)
            errors += typeValidator.validate(config, this, problems);
        if (this.validator!=null)
            errors += this.validator.validate(config, this, problems);

        return errors == 0;
    }

    /**
     * Validates the given value against the type-specific and custom validators.
     *
     * @param value the object to be validated
     * @return true if the value passes both the type-specific and custom validation checks, false otherwise
     */
    public boolean validate(Object value) {
        Validator typeValidator = validatorForType(this.type);
        boolean noerrors = true;
        if (typeValidator!=null)
            noerrors = typeValidator.validateAspect(value);
        if (this.validator!=null)
            noerrors = noerrors && this.validator.validateAspect(value);

        return noerrors;
    }

    /**
     * Returns a Validator instance based on the specified Type.
     * <p>
     * This method maps a given Type to its corresponding Validator by utilizing
     * predefined validator tuples. It supports BOOLEAN, CLASS, DOUBLE, INT, SHORT,
     * and LONG types, each associated with a specific validation logic.
     * <p>
     * For unsupported types such as STRING, LIST, and PASSWORD, or any other
     * unspecified types, the method returns null.
     * <p>
     * Usage of this method ensures that the appropriate validation logic is applied
     * based on the configuration field type, enhancing type safety and validation
     * consistency.
     *
     * @param type the Type for which a Validator is required
     * @return a Validator instance for the specified Type, or null if the Type is unsupported
     */
    public static Validator validatorForType(Type type) {
        switch (type) {
            case BOOLEAN:
                return createValidator(isBooleanValidator);
            case CLASS:
                return createValidator(isClassNameValidator);
            case DOUBLE:
                return createValidator(isDoubleValidator);
            case INT:
                return createValidator(isIntegerValidator);
            case SHORT:
                return createValidator(isShortValidator);
            case LONG:
                return createValidator(isLongValidator);
            case STRING,LIST,PASSWORD:
                break;
            default:
                return null;
        }
        return null;
    }

    /**
     * Returns the corresponding Java class for the current field type.
     *
     * This method uses a switch expression to map the field's type to its
     * corresponding Java class. The mapping is based on the predefined
     * field types such as BOOLEAN, CLASS, FLOAT, DOUBLE, INT, SHORT, LONG,
     * STRING, PASSWORD, LIST, MAP, and OBJECT. If the field type is OBJECT,
     * it returns the class specified by the `className` attribute. If the
     * type does not match any predefined types, it returns null.
     *
     * @return the Java class corresponding to the field's type, or null if
     *         the type is not recognized.
     */
    public Class<?> getTypeClass() {
        return switch (this.type) {
            case BOOLEAN -> Boolean.class;
            case CLASS -> Class.class;
            case FLOAT -> Float.class;
            case DOUBLE -> Double.class;
            case INT -> Integer.class;
            case SHORT -> Short.class;
            case LONG -> Long.class;
            case STRING, PASSWORD -> String.class;
            case LIST -> List.class;
            case MAP -> Map.class;
            case OBJECT -> this.className;
            default -> null;
        };
    }

    /**
     * Validates whether the value of a given field in the configuration is allowed.
     *
     * <p>This method retrieves the value of the specified field from the provided
     * Configuration instance based on the field's type. It supports various types
     * including INT, LONG, BOOLEAN, FLOAT, DOUBLE, LIST, MAP, OBJECT, and defaults
     * to STRING for unsupported types. The retrieved value is then checked against
     * the field's set of allowed values.</p>
     *
     * <p>If the value is null or part of the allowed values, the method returns 0,
     * indicating no validation errors. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1, indicating a validation error.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is allowed or null, 1 if the value is not part of the allowed values
     */
    public static int isAllowed(Configuration config, Field field, ValidationOutput problems) {
        Object value = switch (field.type()) {
            case INT -> config.getInteger(field);
            case LONG -> config.getLong(field);
            case BOOLEAN -> config.getBoolean(field);
            case FLOAT -> config.getFloat(field);
            case DOUBLE -> config.getDouble(field);
            case LIST -> config.getList(field);
            case MAP -> config.getMap(field);
            case OBJECT -> config.getObject(field, field.className());
            default -> config.getString(field);
        };
        if (value == null || field.allowedValues().contains(value)) {
            return 0;
        }
        problems.accept(field, value, "Value is not part of the allowedValues - " + field.allowedValues());
        return 1;
    }

    /**
     * Checks if the given value is allowed based on the set of allowed values.
     *
     * @param value the value to check against the allowed values
     * @return true if the allowed values set is empty or contains the value, false otherwise
     */
    public boolean isAllowed(Object value) {
        if (CollectionUtils.isEmpty(this.allowedValues())) return true;
        return this.allowedValues().contains(value);
    }

    /**
     * A Tuple containing a StartupValidator and an AspectValidator for validating
     * if a configuration field value is a valid Java class name.
     *
     * <p>The StartupValidator uses the {@code isClassName} method to validate
     * the field value during the startup phase, while the AspectValidator uses
     * the {@code validateIsClassNameInAspect} method to check the validity of
     * the class name aspect.
     */
    public static final Tuple<StartupValidator, AspectValidator> isClassNameValidator = new Tuple<>(Field::isClassName, Field::validateIsClassNameInAspect);

    /**
     * A Tuple containing validators for checking if a configuration field value is a boolean.
     *
     * <p>This Tuple pairs a StartupValidator and an AspectValidator to ensure that a field
     * value can be validated both during startup and in specific aspects. The StartupValidator
     * uses the isBoolean method to validate the field within a configuration context, while
     * the AspectValidator uses the validateIsBooleanInAspect method to check if an object
     * can be interpreted as a boolean.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isBooleanValidator = new Tuple<>(Field::isBoolean, Field::validateIsBooleanInAspect);

    /**
     * A constant Tuple containing validators for checking if a value is an integer.
     *
     * <p>This Tuple pairs a StartupValidator and an AspectValidator, both of which
     * are used to validate integer values within a configuration context. The
     * StartupValidator uses the isInteger method to validate configuration fields,
     * while the AspectValidator uses the validateIsIntegerInAspect method to
     * validate individual objects.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isIntegerValidator = new Tuple<>(Field::isInteger, Field::validateIsIntegerInAspect);

    /**
     * A tuple containing validators for checking if a value is a positive integer.
     *
     * <p>This tuple combines a {@link StartupValidator} and an {@link AspectValidator}
     * to validate configuration fields and individual objects, respectively, ensuring
     * they represent positive integers. The {@code StartupValidator} is used for
     * validating fields within a configuration context, while the {@code AspectValidator}
     * is used for validating individual objects.
     */
    public static final Tuple<StartupValidator, AspectValidator> isPositiveIntegerValidator = new Tuple<>(Field::isPositiveInteger, Field::validateIsPositiveIntegerInAspect);

    /**
     * A tuple containing validators for checking if a value is a non-negative integer.
     *
     * <p>This tuple consists of a {@link StartupValidator} and an {@link AspectValidator}.
     * The {@code StartupValidator} validates a configuration field during startup,
     * while the {@code AspectValidator} checks if an object is a non-negative integer.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isNonNegativeIntegerValidator = new Tuple<>(Field::isNonNegativeInteger, Field::validateIsNonNegativeIntegerInAspect);

    /**
     * A tuple containing validators for checking if a value is a long integer.
     *
     * <p>This tuple consists of a {@link StartupValidator} and an {@link AspectValidator}.
     * The {@code StartupValidator} validates a configuration field during startup,
     * while the {@code AspectValidator} checks if an object is a long integer.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isLongValidator = new Tuple<>(Field::isLong, Field::validateIsLongInAspect);

    /**
     * A Tuple containing validators for checking if a configuration field value is a positive long.
     *
     * <p>This Tuple pairs a StartupValidator and an AspectValidator. The StartupValidator
     * validates a field within a configuration context during startup, while the AspectValidator
     * checks if an object represents a positive long.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isPositiveLongValidator = new Tuple<>(Field::isPositiveLong, Field::validateIsPositiveLongInAspect);

    /**
     * A Tuple containing validators for checking if a field value is a non-negative long.
     *
     * <p>This Tuple pairs a StartupValidator and an AspectValidator. The StartupValidator
     * uses the isNonNegativeLong method to validate configuration fields during startup,
     * while the AspectValidator uses the validateIsNonNegativeLongInAspect method to
     * validate individual objects.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isNonNegativeLongValidator = new Tuple<>(Field::isNonNegativeLong, Field::validateIsNonNegativeLongInAspect);

    /**
     * A Tuple containing a StartupValidator and an AspectValidator for validating
     * whether a configuration field value is a short. The StartupValidator uses
     * the isShort method to validate the field within a configuration context,
     * while the AspectValidator uses the validateIsShortInAspect method to check
     * if an object can be interpreted as a short.
     */
    public static final Tuple<StartupValidator, AspectValidator> isShortValidator = new Tuple<>(Field::isShort, Field::validateIsShortInAspect);

    /**
     * A constant Tuple combining a StartupValidator and an AspectValidator for double validation.
     *
     * <p>This Tuple pairs the isDouble method, which validates if a configuration field's value
     * is a double, with the validateIsDoubleInAspect method, which checks if an object can be
     * interpreted as a double. This combination allows for comprehensive validation of double
     * values within a configuration context.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isDoubleValidator = new Tuple<>(Field::isDouble, Field::validateIsDoubleInAspect);

    /**
     * A constant Tuple containing a StartupValidator and an AspectValidator for
     * checking if a field value is required in a configuration.
     *
     * <p>The StartupValidator validates the requirement of a field during the
     * startup phase, while the AspectValidator checks if the value is non-null
     * and non-empty.</p>
     */
    public static final Tuple<StartupValidator, AspectValidator> isRequiredValidator = new Tuple<>(Field::isRequired, Field::validateIsRequiredInAspect);

    /**
     * Validates whether the value of a specified field in the configuration is a valid Java class name.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a valid Java class name using the validateIsClassNameInAspect method.
     * If the value is valid, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a valid Java class name, 1 if it is not
     */
    public static int isClassName(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsClassNameInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A Java class name is expected");
        return 1;
    }

    /**
     * Checks if the given value is a valid Java class name.
     *
     * @param value the value to check if it is a valid Java class name
     * @return true if the value is a valid Java class name, false otherwise
     */
    public static boolean validateIsClassNameInAspect(Object value) {
        return value == null || value instanceof Class<?> || SourceVersion.isName(value.toString());
    }

    /**
     * Validates whether the value of a specified field in the configuration is a boolean value.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a boolean value using the validateIsBooleanInAspect method.
     * If the value is a boolean, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a boolean, 1 if it is not
     */
    public static int isBoolean(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsBooleanInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "Either 'true' or 'false' is expected");
        return 1;
    }

    /**
     * Validates if the given object can be interpreted as a boolean value.
     *
     * <p>This method checks if the provided object is either null, an instance of Boolean,
     * or a string representation that matches "true" or "false" (case-insensitive).
     * It is useful for ensuring that a value can be safely treated as a boolean
     * within certain contexts or configurations.
     *
     * @param value the object to validate as a boolean
     * @return true if the object is null, a Boolean instance, or a string
     *         that equals "true" or "false" (ignoring case); false otherwise
     */
    public static boolean validateIsBooleanInAspect(Object value) {
        return value == null ||
                value instanceof Boolean ||
                value.toString().trim().equalsIgnoreCase(Boolean.TRUE.toString()) ||
                value.toString().trim().equalsIgnoreCase(Boolean.FALSE.toString());
    }

    /**
     * Validates whether the value of a specified field in the configuration is an integer.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is an integer using the validateIsIntegerInAspect method.
     * If the value is an integer, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is an integer, 1 if it is not
     */
    public static int isInteger(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsIntegerInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "An integer is expected");
        return 1;

    }

    /**
     * Validates whether the given object can be interpreted as an integer.
     *
     * <p>This method checks if the provided value is either null or can be
     * successfully converted to an integer. If the value is null, the method
     * returns true, indicating that null is considered valid in this context.
     * If the value is an instance of Integer, it is directly validated. For
     * other types, the method attempts to parse the value's string representation
     * as an integer. The validation process uses a Try block to handle any
     * potential parsing exceptions gracefully, returning false if parsing fails.
     *
     * @param value the object to validate as an integer
     * @return true if the value is null or can be parsed as an integer; false otherwise
     */
    public static boolean validateIsIntegerInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Integer intValue) return intValue;
            else return Integer.parseInt(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a positive integer.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a positive integer using the validateIsPositiveIntegerInAspect method.
     * If the value is a positive integer, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a positive integer, 1 if it is not
     */
    public static int isPositiveInteger(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsPositiveIntegerInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A positive, non-zero integer value is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a positive integer.
     *
     * <p>This method checks if the provided object is either an Integer or can be
     * parsed into an Integer. If the object is null, it returns true, assuming
     * that null values are considered valid in this context. If the object is
     * a valid integer, it further checks if the integer is greater than zero.
     *
     * <p>Uses the Try monad from the Vavr library to handle potential parsing
     * exceptions gracefully, returning false if parsing fails or if the integer
     * is not positive.
     *
     * @param value the object to validate
     * @return true if the object is null or a positive integer, false otherwise
     */
    public static boolean validateIsPositiveIntegerInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Integer intValue) return intValue;
            else return Integer.parseInt(value.toString());
        }).map(i -> i > 0).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a non-negative integer.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a non-negative integer using the validateIsNonNegativeIntegerInAspect method.
     * If the value is a non-negative integer, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a non-negative integer, 1 if it is not
     */
    public static int isNonNegativeInteger(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsNonNegativeIntegerInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "An non-negative integer is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a non-negative integer.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a non-negative integer. If the value is null, the method
     * returns true. If the value is an instance of Integer, it checks if it is
     * non-negative. Otherwise, it attempts to parse the value as an integer
     * and checks if the parsed integer is non-negative.</p>
     *
     * <p>The method uses a Try block to handle potential parsing exceptions,
     * ensuring that any non-integer values or parsing errors result in a
     * return value of false.</p>
     *
     * @param value the object to validate
     * @return true if the value is null or a non-negative integer, false otherwise
     */
    public static boolean validateIsNonNegativeIntegerInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Integer intValue) return intValue;
            else return Integer.parseInt(value.toString());
        }).map(i -> i >= 0).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a long.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a long using the validateIsLongInAspect method.
     * If the value is a long, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a long, 1 if it is not
     */
    public static int isLong(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsLongInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A long value is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a long.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a long. If the value is null, the method returns true,
     * assuming that null values are considered valid in this context. If the
     * value is an instance of Long, it is directly validated. For other types,
     * the method attempts to parse the value's string representation as a long.
     * The validation process uses a Try block to handle any potential parsing
     * exceptions gracefully, returning false if parsing fails.
     *
     * @param value the object to validate
     * @return true if the value is null or can be parsed as a long, false otherwise
     */
    public static boolean validateIsLongInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Long) return value;
            else return Long.parseLong(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a positive long.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a positive long using the validateIsPositiveLongInAspect method.
     * If the value is a positive long, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a positive long, 1 if it is not
     */
    public static int isPositiveLong(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsPositiveLongInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A positive, non-zero long value is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a positive long.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a positive long. If the value is null, the method returns
     * true, assuming that null values are considered valid in this context. If
     * the value is an instance of Long, it checks if it is positive. Otherwise,
     * it attempts to parse the value as a long and checks if the parsed long
     * is positive.</p>
     *
     * <p>The method uses a Try block to handle potential parsing exceptions,
     * ensuring that any non-long values or parsing errors result in a return
     * value of false.</p>
     *
     * @param value the object to validate
     * @return true if the value is null or a positive long, false otherwise
     */
    public static boolean validateIsPositiveLongInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Long longValue) return longValue;
            else return Long.parseLong(value.toString());
        }).map(i -> i > 0).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a non-negative long.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a non-negative long using the validateIsNonNegativeLongInAspect method.
     * If the value is a non-negative long, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a non-negative long, 1 if it is not
     */
    public static int isNonNegativeLong(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsNonNegativeLongInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A non-negative long value is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a non-negative long.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a non-negative long. If the value is null, the method returns
     * true, assuming that null values are considered valid in this context. If
     * the value is an instance of Long, it checks if it is non-negative. Otherwise,
     * it attempts to parse the value as a long and checks if the parsed long
     * is non-negative.</p>
     *
     * <p>The method uses a Try block to handle potential parsing exceptions,
     * ensuring that any non-long values or parsing errors result in a return
     * value of false.</p>
     *
     * @param value the object to validate
     * @return true if the value is null or a non-negative long, false otherwise
     */
    public static boolean validateIsNonNegativeLongInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Long longValue) return longValue;
            else return Long.parseLong(value.toString());
        }).map(i -> i >= 0).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a short.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a short using the validateIsShortInAspect method.
     * If the value is a short, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * <p>The validation process involves converting the field value to a string and
     * attempting to parse it as a short. If parsing is successful, the value is considered
     * valid. If parsing fails, a problem message is recorded, indicating that a short
     * value is expected.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a short, 1 if it is not
     */
    public static int isShort(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsShortInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A short value is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a short.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a short. If the value is null, the method returns true,
     * assuming that null values are considered valid in this context. If the
     * value is an instance of Short, it is directly validated. For other types,
     * the method attempts to parse the value's string representation as a short.
     * The validation process uses a Try block to handle any potential parsing
     * exceptions gracefully, returning false if parsing fails.
     *
     * @param value the object to validate
     * @return true if the value is null or can be parsed as a short, false otherwise
     */
    public static boolean validateIsShortInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Short shortValue) return shortValue;
            else return Short.parseShort(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is a double.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is a double using the validateisDoubleInAspect method.
     * If the value is a double, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is a double, 1 if it is not
     */
    public static int isDouble(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsDoubleInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A double value is expected");
        return 1;
    }

    /**
     * Validates whether the given object represents a double.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a double. If the value is null, the method returns true,
     * assuming that null values are considered valid in this context. If the
     * value is an instance of Double, it is directly validated. For other types,
     * the method attempts to parse the value's string representation as a double.
     * The validation process uses a Try block to handle any potential parsing
     * exceptions gracefully, returning false if parsing fails.
     *
     * @param value the object to validate
     * @return true if the value is null or can be parsed as a double, false otherwise
     */
    public static boolean validateIsDoubleInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Double doubleValue) return doubleValue;
            else return Double.parseDouble(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    /**
     * Validates whether the value of a specified field in the configuration is required.
     *
     * <p>This method retrieves the value of the field from the given Configuration instance
     * and checks if it is required using the isRequiredInAspect method.
     * If the value is required, it returns 0. Otherwise, it records a validation problem
     * using the provided ValidationOutput and returns 1.</p>
     *
     * @param config the Configuration instance from which to retrieve the field value
     * @param field the Field object representing the configuration field to validate
     * @param problems the ValidationOutput to record any validation issues
     * @return 0 if the value is required, 1 if it is not
     */
    public static int isRequired(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsRequiredInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A value is required");
        return 1;
    }

    /**
     * Validates whether the given object represents a required value.
     *
     * <p>This method checks if the provided value is either null or can be
     * interpreted as a non-empty string. If the value is null, the method returns
     * false, assuming that null values are not considered valid in this context.
     * If the value is an instance of String, it checks if it is not empty. For
     * other types, it converts the value to a string and checks if it is not empty.
     * The validation process uses a Try block to handle any potential parsing
     * exceptions gracefully, returning false if parsing fails.
     *
     * @param value the object to validate
     * @return true if the value is not null and is not an empty string, false otherwise
     */
    public static boolean validateIsRequiredInAspect(Object value) {
        return value != null && !String.valueOf(value).trim().isEmpty();
    }

    /**
     * Creates a Validator instance from a Tuple of StartupValidator and AspectValidator.
     *
     * <p>This method takes a Tuple containing a StartupValidator and an AspectValidator,
     * and returns a Validator instance. The Validator instance is created by XXXXXXXX
     * the validation methods of the StartupValidator and AspectValidator. If either
     * validator is null, the method returns null.</p>
     *
     * @param validator a Tuple containing a StartupValidator and an AspectValidator
     * @return a Validator instance, or null if either validator in the Tuple is null
     */
    public static Validator createValidator(Tuple<StartupValidator, AspectValidator> validator) {
        if (validator == null) return null;

        return new Validator() {
            @Override
            public int validate(Configuration config, Field field, ValidationOutput problems) {
                return validator._1().validate(config, field, problems);
            }

            @Override
            public boolean validateAspect(Object value) {
                return validator._2().validate(value);
            }
        };
    }

    /**
     * Enum representing the levels of importance.
     * It provides three levels: HIGH, MEDIUM, and LOW.
     * This enum is used to categorize the importance of a field or configuration item for documentation purposes.
     */
    public enum Importance {
        HIGH, MEDIUM, LOW
    }

    /**
     * Enum representing the data types of a field.
     * It provides various data types such as BOOLEAN, STRING, INT, SHORT, LONG, FLOAT, DOUBLE, LIST, CLASS, PASSWORD, MAP, and OBJECT.
     * This enum is used to specify the data type of a field in the configuration.
     */
    public enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, FLOAT, DOUBLE, LIST, CLASS, PASSWORD, MAP, OBJECT;

        /**
         * Checks if the data type is sensitive.
         * Sensitive data types include PASSWORD.
         *
         * @return true if the data type is sensitive, false otherwise
         */
        public boolean isSensitive() {
            return this == PASSWORD;
        }
    }

    /**
     * Functional interface for validation output.
     * It defines a method to accept a Field, value, and problem message.
     * This interface is used to report validation issues during the configuration validation process.
     * @see Field#validate(Configuration, Field.ValidationOutput)
     * @see Configuration#validate(Iterable, Field.ValidationOutput)
     * @see Field.ValidationOutput#accept(Field, Object, String)
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Field.ValidationOutput problems = (field, value, problemMessage) -> {
     *     // Handle the validation problem
     * };
     * }
     * </pre>
     */
    @FunctionalInterface
    public interface ValidationOutput {
        /**
         * Accepts a validation problem for a specific field, recording the field,
         * the problematic value, and a descriptive problem message.
         *
         * @param field the Field object associated with the validation issue
         * @param value the value that caused the validation problem
         * @param problemMessage a message describing the validation problem
         */
        void accept(Field field, Object value, String problemMessage);
    }

    /**
     * The Validator interface defines a contract for implementing validation logic
     * for configuration fields. It provides methods to validate a field within a
     * configuration context and to validate individual values. The interface also
     * supports combining multiple validators using logical 'and' operations.
     *
     * <p>Implementations of this interface should provide specific validation
     * logic in the `validate` and `validateAspect` methods. The `validate` method
     * is used for validating a field within a configuration, while the
     * `validateAspect` method is used for validating individual values.</p>
     *
     * <p>The `and` method allows for chaining multiple validators, enabling
     * composite validation logic. If the other validator is null or the same
     * instance, the current validator is returned.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Validator customValidator = new Validator() {
     *     @Override
     *     public int validate(Configuration config, Field field, ValidationOutput problems) {
     *         // Custom validation logic
     *         return 0;
     *     }
     *
     *     @Override
     *     public boolean validateAspect(Object value) {
     *         // Custom aspect validation logic
     *         return true;
     *     }
     * };
     *
     * Validator combinedValidator = customValidator.and(anotherValidator);
     * }
     * </pre>
     */
    public interface Validator {

        /**
         * Validates a field within a configuration context.
         *
         * <p>This method is used to validate a specific field within a configuration.
         * It takes a Configuration instance, a Field object, and a ValidationOutput
         * instance to record any validation problems. The method returns the number
         * of validation problems encountered during the validation process.</p>
         *
         * @param config the Configuration instance containing the field to be validated
         * @param field the Field object representing the field to be validated
         * @param problems a ValidationOutput instance to record any validation problems
         * @return the number of validation problems encountered during the validation process
         */
        int validate(Configuration config, Field field, ValidationOutput problems);

        /**
         * Validates an individual value.
         *
         * <p>This method is used to validate a specific value within a configuration.
         * It takes an Object representing the value to be validated. The method returns
         * a boolean indicating whether the value is valid according to the validation logic.</p>
         *
         * @param value the Object representing the value to be validated
         * @return true if the value is valid, false otherwise
         */
        boolean validateAspect(Object value);

        /**
         * Combines this Validator with another Validator using a logical 'and' operation.
         *
         * <p>If the other Validator is null or the same instance as this one, the current
         * Validator is returned. Otherwise, a new Validator is created that performs validation
         * by invoking both Validators and combining their results.</p>
         *
         * <p>The combined Validator's `validate` method sums the validation results from both
         * Validators, while the `validateAspect` method returns true only if both Validators
         * return true for the given value.</p>
         *
         * @param other the other Validator to combine with this one
         * @return a new Validator that represents the logical 'and' of this Validator and the other
         */
        default Validator and(Validator other) {
            if (other == null || other == this) {
                return this;
            }
            var parent = this;
            return new Validator() {
                @Override
                public int validate(Configuration config, Field field, ValidationOutput problems) {
                    return  parent.validate(config, field, problems) + other.validate(config, field, problems);
                }

                @Override
                public boolean validateAspect(Object value) {
                    return parent.validateAspect(value) && other.validateAspect(value);
                }
            };
        }
    }

    /**
     * Interface for validating aspects of an object.
     *
     * <p>The {@code validate} method checks if a given object meets certain criteria.
     * Implementations should define the specific validation logic.
     *
     * <p>The {@code and} method allows combining multiple validators. It returns a new
     * {@code AspectValidator} that performs a logical OR operation between the current
     * validator and another specified validator. If the other validator is null or the
     * same as the current one, the current validator is returned.
     *
     * <p>Usage example:
     * <pre>
     *     AspectValidator validator1 = value -> value != null;
     *     AspectValidator validator2 = value -> value instanceof String;
     *     AspectValidator combinedValidator = validator1.and(validator2);
     * </pre>
     *
     * <p>In this example, {@code combinedValidator} will validate if a value is non-null
     * or an instance of {@code String}.
     */
    public interface AspectValidator {

        boolean validate(Object value);

        /**
         * Combines the current validator with another specified validator using a logical OR operation.
         *
         * <p>If the other validator is null or the same as the current one, the current validator is returned.
         * Otherwise, returns a new validator that validates if either the current or the other validator
         * validates the given value.
         *
         * @param other the other AspectValidator to combine with
         * @return a new AspectValidator that performs a logical OR operation with the other validator
         */
        default AspectValidator and(AspectValidator other) {
            if (other == null || other == this) {
                return this;
            }
            return value -> validate(value) || other.validate(value);
        }
    }

    /**
     * The StartupValidator interface defines a contract for implementing validation logic
     * that is executed during the startup phase of a configuration process. It provides
     * a method to validate a specific field within a configuration context and allows
     * for combining multiple validators using a logical 'and' operation.
     *
     * <p>The `validate` method is responsible for performing validation on a given
     * configuration field, recording any validation issues in the provided
     * ValidationOutput. It returns an integer indicating the number of validation
     * errors encountered.</p>
     *
     * <p>The `and` method enables the combination of this validator with another
     * StartupValidator, creating a composite validator that aggregates the results
     * of both validators. If the other validator is null or the same instance, the
     * current validator is returned. Otherwise, a new validator is created that
     * sums the validation results from both validators.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * StartupValidator validator1 = ...;
     * StartupValidator validator2 = ...;
     * StartupValidator combinedValidator = validator1.and(validator2);
     * }
     * </pre>
     */
    public interface StartupValidator {

        int validate(Configuration config, Field field, ValidationOutput problems);

        /**
         * Combines this validator with another StartupValidator using a logical 'and' operation.
         * If the other validator is null or the same instance, returns the current validator.
         * Otherwise, creates a composite validator that aggregates the validation results
         * from both validators.
         *
         * @param other the other StartupValidator to combine with
         * @return a composite StartupValidator that sums the validation results of both validators
         */
        default StartupValidator and(StartupValidator other) {
            if (other == null || other == this) {
                return this;
            }
            return (config, field, problems) -> validate(config, field, problems) + other.validate(config, field, problems);
        }
    }

    /**
     * The Set class is a specialized collection for managing Field objects, providing
     * an immutable map-based structure to store and retrieve fields by their names.
     * It implements the Iterable interface, allowing iteration over the contained
     * Field objects.
     *
     * <p>Constructors are provided to initialize the Set with various combinations
     * of Field objects, either from a collection or varargs. The fields are stored
     * in an unmodifiable map to ensure immutability.</p>
     *
     * <p>Static factory methods are available to create instances of Set, offering
     * flexibility in combining existing sets with additional fields.</p>
     *
     * <p>Key methods include:</p>
     * <ul>
     *   <li>{@code getField(String name)}: Retrieves a Field by its name.</li>
     *   <li>{@code asArray()}: Returns the fields as an array.</li>
     *   <li>{@code iterator()}: Provides an iterator over the fields.</li>
     *   <li>{@code toString()}: Returns a string representation of the field names.</li>
     * </ul>
     *
     * <p>This class ensures that the fields are uniquely identified by their names
     * and provides a robust mechanism for field management within configurations.</p>
     */
    public static final class Set implements Iterable<Field> {

        private final Map<String, Field> fieldsMap;

        /**
         * Constructs a new instance of the Set class, initializing the fieldsMap
         * to an empty map. This ensures that the fieldsMap is never null and
         * provides a default state for the object.
         */
        public Set() {
            this.fieldsMap = Collections.emptyMap();
        }

        /**
         * Constructs a new instance of the Set class, initializing the fieldsMap
         * with the provided fields. It creates an unmodifiable map to ensure
         * immutability.
         *
         * @param fields The fields to be included in the Set.
         */
        public Set(java.util.Set<Field> fields) {
            Map<String, Field> tmpFieldsMap = new HashMap<>();
            for (Field field: fields) {
                tmpFieldsMap.put(field.name(), field);
            }
            this.fieldsMap = Collections.unmodifiableMap(tmpFieldsMap);
        }

        /**
         * Constructs a new instance of the Set class, initializing the fieldsMap
         * with the provided fields. It creates an unmodifiable map to ensure
         * immutability.
         *
         * @param fields The fields to be included in the Set.
         */
        public Set(Field ...fields) {
            Map<String, Field> tmpFieldsMap = new HashMap<>();
            for (Field field: fields) {
                tmpFieldsMap.put(field.name(), field);
            }
            this.fieldsMap = Collections.unmodifiableMap(tmpFieldsMap);
        }

        /**
         * Constructs a new instance of the Set class, initializing the fieldsMap
         * with the provided fields. It creates an unmodifiable map to ensure
         * immutability.
         *
         * @param fieldSet The set of fields to be included in the Set.
         * @param fields The additional fields to be included in the Set.
         */
        public Set(java.util.Set<Field> fieldSet, Field ...fields) {
            Map<String, Field> tmpFieldsMap = new HashMap<>();
            for (Field field: fieldSet) {
                tmpFieldsMap.put(field.name(), field);
            }
            for (Field field: fields) {
                tmpFieldsMap.put(field.name(), field);
            }
            this.fieldsMap = Collections.unmodifiableMap(tmpFieldsMap);
        }

        /**
         * Creates a new instance of the Set class containing the specified fields.
         *
         * @param fields the fields to be included in the Set
         * @return a new Set instance with the provided fields
         */
        public static Set of(Field ...fields) {
            return new Set(fields);
        }

        /**
         * Creates a new instance of the Set class containing the specified fields
         * and additional fields from the provided field set.
         *
         * @param fieldSet the initial set of fields to include in the Set
         * @param fields additional fields to include in the Set
         * @return a new Set instance with the combined fields
         */
        public static Set of(java.util.Set<Field> fieldSet, Field ...fields) {
            return new Set(fieldSet, fields);
        }

        /**
         * Creates a new instance of the Set class containing all fields from the
         * provided field set and additional fields.
         *
         * @param allFields the initial set of fields to include in the Set
         * @param fields additional fields to include in the Set
         * @return a new Set instance with all fields
         */
        public static Set of(Set allFields, Field ...fields) {
            java.util.Set<Field> fieldSet = new java.util.HashSet<>(allFields.fieldsMap.values());
            fieldSet.addAll(Arrays.asList(fields));
            return new Set(fieldSet);
        }

        /**
         * Retrieves the Field object associated with the specified name.
         *
         * @param name the name of the Field to retrieve
         * @return the Field object associated with the specified name, or null if not found
         */
        public Field getField(String name) {
            return this.fieldsMap.get(name);
        }

        /**
         * Returns an array containing all the Field objects in this Set.
         *
         * @return an array of Field objects
         */
        public Field[] asArray() {
            return this.fieldsMap.values().toArray(new Field[0]);
        }

        /**
         * Returns an iterator over the Field objects in this Set.
         *
         * @return an Iterator over the Field objects
         */
        @NonNull
        @Override
        public Iterator<Field> iterator() {
            return this.fieldsMap.values().iterator();
        }

        @Override
        public String toString() {
            return this.fieldsMap.keySet().toString();
        }
    }

}
