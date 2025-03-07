package io.github.vickycmd.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.fields.Field;
import io.vavr.control.Try;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.*;

@Component
@Scope("prototype")
public class Configuration {


   private final Environment environment;

    private final HashMap<String, Object> defaultsMap;

    private final ObjectMapper mapper;

    @Autowired
    public Configuration(Environment environment) {
        this.environment = environment;
        this.defaultsMap = new HashMap<>();
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Configuration withDefault(String name, Object value) {
        this.defaultsMap.put(name, value);
        return this;
    }

    public <T> T get(String property, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> this.getDefaultValue(property, targetType), targetType);
    }

    public <T> T get(String property, T defaultValue, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType);
    }

    public <T> T get(String property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.environment.getProperty(property, targetType, defaultValueSupplier.get());
    }

    @ValidateField
    public <T> T get(Field property, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType), targetType);
    }

    @ValidateField
    public <T> T get(Field property, T defaultValue, Class<T> targetType) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType);
    }

    @ValidateField
    public <T> T get(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.get(property.name(),
                defaultValueSupplier.get(), targetType);
    }

    public <T> Optional<T> getOptional(String property, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, targetType));
    }

    public <T> Optional<T> getOptional(String property, T defaultValue, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValue, targetType));
    }

    public <T> Optional<T> getOptional(String property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValueSupplier, targetType));
    }

    @ValidateField
    public <T> Optional<T> getOptional(Field property, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, targetType));
    }

    @ValidateField
    public <T> Optional<T> getOptional(Field property, T defaultValue, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValue, targetType));
    }

    @ValidateField
    public <T> Optional<T> getOptional(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return Optional.ofNullable(this.get(property, defaultValueSupplier, targetType));
    }

    public <T> T get(String property, Class<T> targetType, String ...args) {
        return this.get(property, (Supplier<T>) () -> this.getDefaultValueWithArgs(property, targetType, args), targetType, args);
    }

    public <T> T get(String property, T defaultValue, Class<T> targetType, String ...args) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType, args);
    }

    public <T> T get(String property, Supplier<T> defaultValueSupplier, Class<T> targetType, String ...args) {
        return this.get(String.format(property, (Object[]) args), defaultValueSupplier, targetType);
    }

    @ValidateField
    public <T> T get(Field property, Class<T> targetType, String ...args) {
        return this.get(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType, args), targetType, args);
    }

    @ValidateField
    public <T> T get(Field property, T defaultValue, Class<T> targetType, String ...args) {
        return this.get(property, (Supplier<T>) () -> defaultValue, targetType, args);
    }

    @ValidateField
    public <T> T get(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType, String ...args) {
        return this.get(property.name(),
                defaultValueSupplier.get(), targetType, args);
    }

    public String getString(String property) {
        return this.getString(property, () -> this.getDefaultValue(property, String.class));
    }

    public String getString(String property, String defaultValue) {
        return this.getString(property, () -> defaultValue);
    }

    public String getString(String property, Supplier<String> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, String.class);
    }

    @ValidateField
    public String getString(Field property) {
        return this.getString(property, () -> this.getDefaultValue(property.name(), property.defaultValueAsString(), String.class));
    }

    @ValidateField
    public String getString(Field property, String defaultValue) {
        return this.getString(property, () -> defaultValue);
    }

    @ValidateField
    public String getString(Field property, Supplier<String> defaultValueSupplier) {
        return this.getString(property.name(), defaultValueSupplier.get());
    }

    public Integer getInteger(String property) {
        return this.getInteger(property, () -> this.getDefaultValue(property, Integer.class));
    }

    public Integer getInteger(String property, Integer defaultValue) {
        return this.getInteger(property, () -> defaultValue);
    }

    public Integer getInteger(String property, Supplier<Integer> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Integer.class);
    }

    @ValidateField
    public Integer getInteger(Field property) {
        return this.getInteger(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Integer.class));
    }

    @ValidateField
    public Integer getInteger(Field property, Integer defaultValue) {
        return this.getInteger(property, () -> defaultValue);
    }

    @ValidateField
    public Integer getInteger(Field property, IntSupplier defaultValueSupplier) {
        return this.getInteger(property.name(),
                        defaultValueSupplier.getAsInt());
    }

    public Short getShort(String property) {
        return this.getShort(property, () -> this.getDefaultValue(property, Short.class));
    }

    public Short getShort(String property, Short defaultValue) {
        return this.getShort(property, () -> defaultValue);
    }

    public Short getShort(String property, Supplier<Short> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Short.class);
    }

    @ValidateField
    public Short getShort(Field property) {
        return this.getShort(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Short.class));
    }

    @ValidateField
    public Short getShort(Field property, Short defaultValue) {
        return this.getShort(property, () -> defaultValue);
    }

    @ValidateField
    public Short getShort(Field property, Supplier<Short> defaultValueSupplier) {
        return this.getShort(property.name(),
                defaultValueSupplier.get());
    }

    public Boolean getBoolean(String property) {
        return this.getBoolean(property, () -> this.getDefaultValue(property, Boolean.class));
    }

    public Boolean getBoolean(String property, Boolean defaultValue) {
        return this.getBoolean(property, () -> defaultValue);
    }

    public Boolean getBoolean(String property, Supplier<Boolean> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Boolean.class);
    }

    @ValidateField
    public Boolean getBoolean(Field property) {
        return this.getBoolean(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Boolean.class));
    }

    @ValidateField
    public Boolean getBoolean(Field property, Boolean defaultValue) {
        return this.getBoolean(property, () -> defaultValue);
    }

    @ValidateField
    public Boolean getBoolean(Field property, BooleanSupplier defaultValueSupplier) {
        return this.getBoolean(property.name(),
                defaultValueSupplier.getAsBoolean());
    }

    public Long getLong(String property) {
        return this.getLong(property, () -> this.getDefaultValue(property, Long.class));
    }

    public Long getLong(String property, Long defaultValue) {
        return this.getLong(property, () -> defaultValue);
    }

    public Long getLong(String property, Supplier<Long> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Long.class);
    }

    @ValidateField
    public Long getLong(Field property) {
        return this.getLong(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Long.class));
    }

    @ValidateField
    public Long getLong(Field property, Long defaultValue) {
        return this.getLong(property, () -> defaultValue);
    }

    @ValidateField
    public Long getLong(Field property, LongSupplier defaultValueSupplier) {
        return this.getLong(property.name(),
                defaultValueSupplier.getAsLong());
    }

    public Float getFloat(String property) {
        return this.getFloat(property, () -> this.getDefaultValue(property, Float.class));
    }

    public Float getFloat(String property, Float defaultValue) {
        return this.getFloat(property, () -> defaultValue);
    }

    public Float getFloat(String property, Supplier<Float> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Float.class);
    }

    @ValidateField
    public Float getFloat(Field property) {
        return this.getFloat(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Float.class));
    }

    @ValidateField
    public Float getFloat(Field property, Float defaultValue) {
        return this.getFloat(property, () -> defaultValue);
    }

    @ValidateField
    public Float getFloat(Field property, Supplier<Float> defaultValueSupplier) {
        return this.getFloat(property.name(),
                defaultValueSupplier.get());
    }

    public Double getDouble(String property) {
        return this.getDouble(property, () -> this.getDefaultValue(property, Double.class));
    }

    public Double getDouble(String property, Double defaultValue) {
        return this.getDouble(property, () -> defaultValue);
    }

    public Double getDouble(String property, Supplier<Double> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier, Double.class);
    }

    @ValidateField
    public Double getDouble(Field property) {
        return this.getDouble(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Double.class));
    }

    @ValidateField
    public Double getDouble(Field property, Double defaultValue) {
        return this.getDouble(property, () -> defaultValue);
    }

    @ValidateField
    public Double getDouble(Field property, DoubleSupplier defaultValueSupplier) {
        return this.getDouble(property.name(),
                defaultValueSupplier.getAsDouble());
    }

    public List<String> getList(String property) {
        return this.getList(property, () -> this.getDefaultValue(property, List.class));
    }

    public <T> List<T> getList(String property, Class<T> targetType) {
        return this.getList(property, () -> this.getDefaultValue(property, List.class), targetType);
    }

    public <T> List<T> getList(String property, List<T> defaultValue) {
        return this.getList(property, () -> defaultValue);
    }

    public <T> List<T> getList(String property, List<T> defaultValue, Class<T> targetType) {
        return this.getList(property, () -> defaultValue, targetType);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String property, Supplier<List<T>> defaultValueSupplier) {
        return this.get(property, defaultValueSupplier.get(), List.class);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String property, Supplier<List<T>> defaultValueSupplier, Class<T> targetType) {
        return this.getOptional(property, defaultValueSupplier.get(), List.class)
                .map(objectList -> objectList.stream().map(entry -> mapper.convertValue(entry, targetType)).toList())
                .orElse(Collections.emptyList());

    }

    @ValidateField
    public <T> List<T> getList(Field property) {
        return this.getList(property, () -> this.getDefaultValue(property.name(), property.defaultValue(), List.class));
    }

    @ValidateField
    public <T> List<T>  getList(Field property, List<T>  defaultValue) {
        return this.getList(property, () -> defaultValue);
    }

    @ValidateField
    @SuppressWarnings("unchecked")
    public <T> List<T>  getList(Field property, Supplier<List<T>> defaultValueSupplier) {
        if (property.className()!=null)
            return this.getList(property.name(), defaultValueSupplier.get(), (Class<T>) property.className());
        return this.getList(property.name(),
                defaultValueSupplier.get());
    }

    public Map<String, Object> getMap(String property) {
        return this.getMap(property, () -> this.getDefaultValue(property, Map.class));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String property, Map<String, Object> defaultMap) {
        return this.getOptional(property, String.class).map(value -> new JSONObject(value).toMap())
                .orElse(defaultMap);
    }

    public Map<String, Object> getMap(String property, Supplier<Map<String, Object>> defaultValueSupplier) {
        return this.getOptional(property, String.class).map(value -> new JSONObject(value).toMap())
                .orElse(defaultValueSupplier.get());
    }



    @ValidateField
    public Map<String, Object> getMap(Field property) {
        return this.getMap(property.name(), () -> this.getDefaultValue(property.name(), property.defaultValue(), Map.class));
    }

    @ValidateField
    public Map<String, Object> getMap(Field property, Map<String, Object> defaultMap) {
        return this.getMap(property.name(), defaultMap);
    }

    @ValidateField
    public Map<String, Object> getMap(Field property, Supplier<Map<String, Object>> defaultValueSupplier) {
        return this.getMap(property.name(), defaultValueSupplier);
    }

    public <T> T getObject(String property, Class<T> targetType) {
        return this.getObject(property, (Supplier<T>) () -> this.getDefaultValue(property, targetType), targetType);
    }

    public <T> T getObject(String property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.getObject(property, defaultValueSupplier.get(), targetType);
    }

    public <T> T getObject(String property, T defaultValue, Class<T> targetType) {
        return this.getOptional(property, String.class)
                .map(value -> mapper.convertValue(new JSONObject(value).toMap(), targetType))
                .orElse(defaultValue);
    }

    @ValidateField
    public <T> T getObject(Field property, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), property.defaultValue(), targetType), targetType);
    }

    @ValidateField
    public <T> T getObject(Field property, Supplier<T> defaultValueSupplier, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), defaultValueSupplier.get(), targetType), targetType);
    }

    @ValidateField
    public <T> T getObject(Field property, T defaultValue, Class<T> targetType) {
        return this.getObject(property.name(), (Supplier<T>) () -> this.getDefaultValue(property.name(), defaultValue, targetType), targetType);
    }

    public boolean validateAndRecord(Iterable<Field> fields, Consumer<String> problems) {
        return this.validate(fields, ((field, value, problemMessage) -> {
            if (value == null) {
                problems.accept(String.format("Value for %s is invalid.", field.name()));
                return;
            }

            problems.accept("'Value " + value.toString() + " is invalid for the field " + field.name() + " : " + problemMessage);
        }));
    }

    public boolean validate(Iterable<Field> fields, Field.ValidationOutput problems) {
        var valid = true;
        for (Field field: fields) {
            if (!field.validate(this, problems)) {
                valid = false;
            }
        }

        return valid;
    }

    @SuppressWarnings("unchecked")
    private <T> T getDefaultValue(String property, Object defaultValue, Class<T> defaultClassType, String ...args) {
        return Try.of(() -> {
            String propertyName = ArrayUtils.isNotEmpty(args)?String.format(property, (Object[]) args):property;
            if (defaultValue!=null) {
                if (defaultClassType.isInstance(defaultValue)) {
                    return (T) defaultValue;
                } else {
                    return this.mapper.convertValue(defaultValue, defaultClassType);
                }
            }
            else if (this.defaultsMap.get(propertyName) == null) {
                return null;
            }
            return this.mapper.convertValue(this.defaultsMap.get(propertyName), defaultClassType);
        }).getOrElseThrow(() -> new ConfigException(String.format("Invalid configuration for field: %s", property), "Please validate the configurations for - " + property));
    }

    private <T> T getDefaultValueWithArgs(String property, Class<T> defaultClassType, String[] args) {
        return this.getDefaultValue(property, null, defaultClassType, args);
    }

    private <T> T getDefaultValue(String property, Object defaultValue, Class<T> defaultClassType) {
        return this.getDefaultValue(property, defaultValue, defaultClassType, (String[]) null);
    }

    private <T> T getDefaultValue(String property, Class<T> defaultClassType) {
        return this.getDefaultValue(property, null, defaultClassType, (String[]) null);
    }

//    private String getDefaultValue(String property) {
//        return this.getDefaultValue(property, null, String.class, (String[]) null);
//    }
}
