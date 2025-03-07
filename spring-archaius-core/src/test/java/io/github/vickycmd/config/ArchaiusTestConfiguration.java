package io.github.vickycmd.config;

import com.netflix.config.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.Map;

@TestConfiguration
@EnableSpringArchaius
public class ArchaiusTestConfiguration {


    @Autowired(required = false)
    private Map<String, PolledConfigurationSource> configurationSourcesMap;

    @Bean
    @Primary
    public DynamicPropertyFactory dynamicPropertyFactory(DynamicConfiguration addApplicationPropertiesSource) throws IOException {
        FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(1000, 5000, false);
        ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();
        if (configurationSourcesMap!=null) {
            configurationSourcesMap.forEach((configName, config) -> {
                DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(config, scheduler);
                configuration.addConfiguration(dynamicConfiguration, configName);
            });
        }
        configuration.addConfiguration(addApplicationPropertiesSource, "fileConfig");
        if (!ConfigurationManager.isConfigurationInstalled()) {
            ConfigurationManager.install(configuration);
        }
        return DynamicPropertyFactory.getInstance();
    }
}
