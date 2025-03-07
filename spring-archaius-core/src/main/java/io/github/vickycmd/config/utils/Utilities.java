package io.github.vickycmd.config.utils;

import io.github.vickycmd.config.Configuration;
import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.fields.Field;
import org.slf4j.Logger;

public class Utilities {
    public static void validateConfig(Configuration configuration, Field.Set fields, Class<?> sourceClassName, Logger log) {
        if (!configuration.validateAndRecord(fields, log::error)) {
            throw new ConfigException("Error configuring an instance of " + sourceClassName.getSimpleName() + "; check the logs for details.", "Please validate the configurations for - " + fields);
        }
    }
}
