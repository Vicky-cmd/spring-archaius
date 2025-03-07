package io.github.vickycmd.config.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ApplicationProperty {
    private String id;
    private String key;
    private String value;
    private String description;

    public ApplicationProperty(String id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public ApplicationProperty(String id, String key, String value, String description) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.description = description;
    }


}
