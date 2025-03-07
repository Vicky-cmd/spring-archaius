package io.github.vickycmd.config.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffersFeaturesDto {

    @JsonAlias("v1")
    private V1FeaturesDto v1Features;
    private boolean displayFeatures;
    private String displayText;


    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class V1FeaturesDto {
        private boolean enabled;
    }
}
