package io.github.vickycmd.config.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Config {
    private Config() {}
    private static final List<String> KEYS = List.of(
            "isClassNameValidator", "isBooleanValidator", "isIntegerValidator",
            "isPositiveIntegerValidator", "isNonNegativeIntegerValidator", "isLongValidator",
            "isPositiveLongValidator", "isNonNegativeLongValidator", "isShortValidator",
            "isDoubleValidator", "isRequiredValidator"
    );

    private static final List<String> VALUES = List.of(
            "Validates if the value is a valid class name. Access via getString() or String.class in Configuration.",
            "Validates if the value is a boolean. Access via getBoolean() or Boolean.class in Configuration.",
            "Validates if the value is an integer. Access via getInteger() or Integer.class in Configuration.",
            "Validates if the value is a positive integer. Access via getInteger() or Integer.class in Configuration.",
            "Validates if the value is a non-negative integer. Access via getInteger() or Integer.class in Configuration.",
            "Validates if the value is a long. Access via getLong() or Long.class in Configuration.",
            "Validates if the value is a positive long. Access via getLong() or Long.class in Configuration.",
            "Validates if the value is a non-negative long. Access via getLong() or Long.class in Configuration.",
            "Validates if the value is a short. Access via getShort() or Short.class in Configuration.",
            "Validates if the value is a double. Access via getDouble() or Double.class in Configuration.",
            "Validates if the value is required. Access via getString() or String.class in Configuration."
    );

    public static final Map<String, String> VALIDATORS_DESCRIPTION = createMap();

    public static final String VALIDATOR_ARG = "validator";
    public static final String ALLOWED_VALUES_ARG = "allowedValues";
    public static final String LAMBDA_EXP_ARG = "lambdaExpression";
    public static final String LAMBDA_EXP_ARG_VALUE = "Lambda Expression";


    private static Map<String, String> createMap() {
        if (KEYS.size() != VALUES.size()) {
            throw new IllegalStateException("Keys and Values lists must be of the same size!");
        }

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < KEYS.size(); i++) {
            map.put(KEYS.get(i), VALUES.get(i));
        }
        return Collections.unmodifiableMap(map);
    }
}
