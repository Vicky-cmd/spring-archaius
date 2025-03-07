package io.github.vickycmd.config.errors;

import lombok.Getter;

@Getter
public class ConfigException extends RuntimeException {

    private final String action;

    public ConfigException(String message, String action) {
        super(message);
        this.action = action;
    }

}
