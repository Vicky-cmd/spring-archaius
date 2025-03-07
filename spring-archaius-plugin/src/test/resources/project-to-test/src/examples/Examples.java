package io.github.vickycmd.config.examples;

import io.github.vickycmd.config.fields.Field;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class Examples {
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
    Supplier<Object> supplier = () -> 45L;
    Field offersCountField = Field.builder()
            .name("application.offers.count")
            .displayName("Offers Count")
            .desc("Total Offers Count in the Application")
            .type(Field.Type.LONG)
            .defaultValueGenerator(supplier)
            .allowedValues(new HashSet<>(List.of(45L, 2L, 74L, 14L, 74L)))
            .validator(Field.createValidator(Field.isLongValidator))
            .required()
            .build();
    Field offersCountField1 = Field.builder()
            .name("application.offers.count1")
            .displayName("Offers Count")
            .desc("Total Offers Count in the Application")
            .type(Field.Type.LONG)
            .defaultValueGenerator(() -> {
                if (Math.random() > 0.5) return 2L;
                return 45L;
            })
            .allowedValues(new HashSet<>(List.of(45L, 2L, 74L, 14L, 74L)))
            .validator(Field.isLongValidator, Field.isPositiveLongValidator)
            .required()
            .build();
    Field activeCodesListField = Field.builder()
            .name("application.offers.active.codes")
            .displayName("Active Offers Codes")
            .desc("The list of offer codes for the active offers in the system")
            .defaultValue("code1,code2,code3")
            .type(Field.Type.STRING)
            .build();

    public void setup() {
        Field eventmanagerField = Field.builder()
                .name("event.manager.controls")
                .displayName("Event Manager Controls")
                .desc("The controls for managing the eventmanager application.")
                .type(Field.Type.MAP)
                .defaultValueGenerator(Collections::emptyMap)
                .allowedValues(new HashSet<>(List.of(45L, 2L, 74L, 14L, 74L)))
                .required()
                .build();
        System.out.println(eventmanagerField.name());
    }
}
