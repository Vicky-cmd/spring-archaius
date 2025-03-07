package io.github.vickycmd.config;

import io.github.vickycmd.config.model.ApplicationProperty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@SpringJUnitConfig(classes = {ArchaiusTestConfiguration.class})
@TestPropertySource(locations = "classpath:application-test.properties")
    public abstract class ArchaiusTests {
    public List<ApplicationProperty> getApplicationProperties() {
        return List.of(
                new ApplicationProperty("123", "application.offers.version", "v2"),
                new ApplicationProperty("124", "application.offers.count", "25"),
                new ApplicationProperty("125", "application.offers.active.codes", "25,54,745,12"),
                new ApplicationProperty("456", "database.connectivity.enabled", "true"),
                new ApplicationProperty("789", "application.bbsale.offer.percentage", "5.7"),
                new ApplicationProperty("754", "application.control.annotation", "io.github.vickycmd.config.EnableSpringArchaius"),
                new ApplicationProperty("101", "application.offers.features", "{\"v1\": {\"enabled\": \"true\"}, \"displayFeatures\": \"true\", \"displayText\": \"Offer Features\"}", "All the configuration related to the Offers Features")
        );
    }

    protected Logger logger() {
        return log;
    }


    protected void updateConfigPropertiesFile(List<ApplicationProperty> props) throws IOException {
        StringBuilder builder =  new StringBuilder();
        props.forEach(prop -> builder.append(String.format("%s=%s\n", prop.getKey(), prop.getValue())));
        Files.writeString(new ClassPathResource("config.properties").getFile().toPath(), builder);
    }
}
