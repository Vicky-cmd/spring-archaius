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

@Slf4j
public class Field {

    private String name;
    private String displayName;
    private String desc;
    private Supplier<Object> defaultValueGenerator;
    private Validator validator;
    private Type type;
    private Class<?> className;
    private Importance importance;
    private java.util.Set<?> allowedValues;
    private boolean isRequired;

    public Field() {}

    private Field(String name, String displayName, String desc, Supplier<Object> defaultValueGenerator, Validator validator, Type type, Class<?> className, Importance importance, java.util.Set<?> allowedValues, boolean isRequired) {
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

        public Field build() {
            return new Field(this.name, this.displayName, this.desc, this.defaultValueGenerator, this.validator,
                    this.type, this.className, this.importance, this.allowedValues, this.isRequired);
        }

        public FieldBuilder name(String name) {
            this.name = name;
            return this;
        }
        public FieldBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        public FieldBuilder desc(String desc) {
            this.desc = desc;
            return this;
        }

        public FieldBuilder defaultValue(Object defaultValue) {
            this.defaultValueGenerator = () -> defaultValue;
            return this;
        }
        public FieldBuilder defaultValueGenerator(Supplier<Object> defaultValueGenerator) {
            this.defaultValueGenerator = defaultValueGenerator;
            return this;
        }
        public FieldBuilder type(Type type) {
            this.type = type;
            return this;
        }
        public FieldBuilder validator(Validator... validators) {
            for (Validator additionalValidator: validators) {
                if (additionalValidator!=null) {
                    this.validator = additionalValidator.and(this.validator);
                }
            }
            return this;
        }

        @SafeVarargs
        public final FieldBuilder validator(Tuple<StartupValidator, AspectValidator>... validators) {
            for (Tuple<StartupValidator, AspectValidator> additionalValidator: validators) {
                if (additionalValidator!=null) {
                    this.validator = createValidator(additionalValidator).and(this.validator);
                }
            }
            return this;
        }
        public FieldBuilder className(Class<?> className) {
            this.className = className;
            return this;
        }
        public FieldBuilder importance(Importance importance) {
            this.importance = importance;
            return this;
        }
        public FieldBuilder allowedValues(java.util.Collection<?> allowedValues) {
            this.allowedValues = new HashSet<>(allowedValues);
            return this;
        }

        public FieldBuilder allowedValues(Object ...allowedValues) {
            this.allowedValues = java.util.Set.of(allowedValues);
            return this;
        }
        public FieldBuilder required() {
            this.isRequired = true;
            this.validator(createValidator(isRequiredValidator));
            return this;
        }
    }

    public static FieldBuilder builder() {
        return new FieldBuilder();
    }

    public static FieldBuilder create(String name) {
        return Field.builder().name(name);
    }

    public String name() {
        return name;
    }

    public String defaultValueAsString() {
        return defaultValue()!=null?defaultValue().toString():null;
    }

    public <T> T defaultValue() {
        return (T) defaultValueGenerator.get();
    }

    public String description() {
        return desc;
    }

    public String displayName() {
        return displayName;
    }

    public Type type() {
        return type;
    }

    public Class<?> className() {
        return className;
    }

    public Importance importance() {
        return importance;
    }

    public Validator validator() {
        return validator;
    }

    public boolean isRequired() {
        return isRequired;
    }

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

    public boolean validate(Configuration config, ValidationOutput problems) {
        Validator typeValidator = validatorForType(this.type);
        int errors = 0;
        if (typeValidator!=null)
            errors += typeValidator.validate(config, this, problems);
        if (this.validator!=null)
            errors += this.validator.validate(config, this, problems);

        return errors == 0;
    }

    public boolean validate(Object value) {
        Validator typeValidator = validatorForType(this.type);
        boolean noerrors = true;
        if (typeValidator!=null)
            noerrors = typeValidator.validateAspect(value);
        if (this.validator!=null)
            noerrors = noerrors && this.validator.validateAspect(value);

        return noerrors;
    }

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

    public boolean isAllowed(Object value) {
        if (CollectionUtils.isEmpty(this.allowedValues())) return true;
        return this.allowedValues().contains(value);
    }

    public static final Tuple<StartupValidator, AspectValidator> isClassNameValidator = new Tuple<>(Field::isClassName, Field::validateIsClassNameInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isBooleanValidator = new Tuple<>(Field::isBoolean, Field::validateIsBooleanInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isIntegerValidator = new Tuple<>(Field::isInteger, Field::validateIsIntegerInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isPositiveIntegerValidator = new Tuple<>(Field::isPositiveInteger, Field::validateIsPositiveIntegerInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isNonNegativeIntegerValidator = new Tuple<>(Field::isNonNegativeInteger, Field::validateIsNonNegativeIntegerInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isLongValidator = new Tuple<>(Field::isLong, Field::validateIsLongInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isPositiveLongValidator = new Tuple<>(Field::isPositiveLong, Field::validateIsPositiveLongInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isNonNegativeLongValidator = new Tuple<>(Field::isNonNegativeLong, Field::validateIsNonNegativeLongInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isShortValidator = new Tuple<>(Field::isShort, Field::validateIsShortInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isDoubleValidator = new Tuple<>(Field::isDouble, Field::validateisDoubleInAspect);
    public static final Tuple<StartupValidator, AspectValidator> isRequiredValidator = new Tuple<>(Field::isRequired, Field::validateIsRequiredInAspect);


    public static int isClassName(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsClassNameInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A Java class name is expected");
        return 1;
    }

    public static boolean validateIsClassNameInAspect(Object value) {
        return value == null || value instanceof Class<?> || SourceVersion.isName(value.toString());
    }

    public static int isBoolean(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsBooleanInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "Either 'true' or 'false' is expected");
        return 1;
    }

    public static boolean validateIsBooleanInAspect(Object value) {
        return value == null ||
                value instanceof Boolean ||
                value.toString().trim().equalsIgnoreCase(Boolean.TRUE.toString()) ||
                value.toString().trim().equalsIgnoreCase(Boolean.FALSE.toString());
    }

    public static int isInteger(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsIntegerInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "An integer is expected");
        return 1;

    }

    public static boolean validateIsIntegerInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Integer) return (Integer) value;
            else return Integer.parseInt(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    public static int isPositiveInteger(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsPositiveIntegerInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A positive, non-zero integer value is expected");
        return 1;
    }

    public static boolean validateIsPositiveIntegerInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Integer) return (Integer) value;
            else return Integer.parseInt(value.toString());
        }).map(i -> i > 0).getOrElse(false);
    }

    public static int isNonNegativeInteger(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsNonNegativeIntegerInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "An non-negative integer is expected");
        return 1;
    }

    public static boolean validateIsNonNegativeIntegerInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Integer) return (Integer) value;
            else return Integer.parseInt(value.toString());
        }).map(i -> i >= 0).getOrElse(false);
    }

    public static int isLong(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsLongInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A long value is expected");
        return 1;
    }

    public static boolean validateIsLongInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Long) return value;
            else return Long.parseLong(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    public static int isPositiveLong(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsPositiveLongInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A positive, non-zero long value is expected");
        return 1;
    }


    public static boolean validateIsPositiveLongInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Long) return (Long) value;
            else return Long.parseLong(value.toString());
        }).map(i -> i > 0).getOrElse(false);
    }
    public static int isNonNegativeLong(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsNonNegativeLongInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A non-negative long value is expected");
        return 1;
    }

    public static boolean validateIsNonNegativeLongInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Long) return (Long) value;
            else return Long.parseLong(value.toString());
        }).map(i -> i >= 0).getOrElse(false);
    }

    public static int isShort(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsShortInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A short value is expected");
        return 1;
    }

    public static boolean validateIsShortInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Short) return (Short) value;
            else return Short.parseShort(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    public static int isDouble(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateisDoubleInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A double value is expected");
        return 1;
    }

    public static boolean validateisDoubleInAspect(Object value) {
        if (value == null) {
            return true;
        }

        return Try.of(() -> {
            if (value instanceof Double) return (Double) value;
            else return Double.parseDouble(value.toString());
        }).map(i -> true).getOrElse(false);
    }

    public static int isRequired(Configuration config, Field field, ValidationOutput problems) {
        String value = config.getString(field);
        if (validateIsRequiredInAspect(value)) {
            return 0;
        }
        problems.accept(field, value, "A value is required");
        return 1;
    }

    public static boolean validateIsRequiredInAspect(Object value) {
        return value != null && !String.valueOf(value).trim().isEmpty();
    }

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

    public enum Importance {
        HIGH, MEDIUM, LOW
    }

    public enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, FLOAT, DOUBLE, LIST, CLASS, PASSWORD, MAP, OBJECT;

        public boolean isSensitive() {
            return this == PASSWORD;
        }
    }

    @FunctionalInterface
    public interface ValidationOutput {
        void accept(Field field, Object value, String problemMessage);
    }

    public interface Validator {

        int validate(Configuration config, Field field, ValidationOutput problems);
        boolean validateAspect(Object value);
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

    public interface AspectValidator {

        boolean validate(Object value);
        default AspectValidator and(AspectValidator other) {
            if (other == null || other == this) {
                return this;
            }
            return (value) -> validate(value) || other.validate(value);
        }
    }

    public interface StartupValidator {

        int validate(Configuration config, Field field, ValidationOutput problems);
        default StartupValidator and(StartupValidator other) {
            if (other == null || other == this) {
                return this;
            }
            return (config, field, problems) -> validate(config, field, problems) + other.validate(config, field, problems);
        }
    }


    public static final class Set implements Iterable<Field> {

        private final Map<String, Field> fieldsMap;

        public Set() {
            this.fieldsMap = Collections.emptyMap();
        }

        public Set(java.util.Set<Field> fields) {
            Map<String, Field> tmpFieldsMap = new HashMap<>();
            for (Field field: fields) {
                tmpFieldsMap.put(field.name(), field);
            }
            this.fieldsMap = Collections.unmodifiableMap(tmpFieldsMap);
        }

        public Set(Field ...fields) {
            Map<String, Field> tmpFieldsMap = new HashMap<>();
            for (Field field: fields) {
                tmpFieldsMap.put(field.name(), field);
            }
            this.fieldsMap = Collections.unmodifiableMap(tmpFieldsMap);
        }

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

        public static Set of(Field ...fields) {
            return new Set(fields);
        }

        public static Set of(java.util.Set<Field> fieldSet, Field ...fields) {
            return new Set(fieldSet, fields);
        }

        public static Set of(Set allFields, Field ...fields) {
            java.util.Set<Field> fieldSet = new java.util.HashSet<>(allFields.fieldsMap.values());
            fieldSet.addAll(Arrays.asList(fields));
            return new Set(fieldSet);
        }

        public Field getField(String name) {
            return this.fieldsMap.get(name);
        }

        public Field[] asArray() {
            return this.fieldsMap.values().toArray(new Field[0]);
        }

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
