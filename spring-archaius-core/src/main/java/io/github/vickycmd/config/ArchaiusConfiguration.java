package io.github.vickycmd.config;

import com.netflix.config.*;
import com.netflix.config.sources.URLConfigurationSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "io.github.vickycmd")
public class ArchaiusConfiguration {

    private final ArchaiusPropertySource archaiusPropertySource;
    private final Environment env;

    @Value("${spring.archaius.config.source.file:application.properties}")
    private String fileName;

    @Value("${spring.archaius.config.source.initial-delay:1000}")
    private int initialDelayMillis;

    @Value("${spring.archaius.config.source.delay-ms:5000}")
    private int delayMillis;

    @Value("${spring.archaius.config.source.ignore-deletes-from-source:false}")
    private boolean ignoreDeletesFromSource;

    public ArchaiusConfiguration(ArchaiusPropertySource archaiusPropertySource, Environment env) {
        this.archaiusPropertySource = archaiusPropertySource;
        this.env = env;
    }

    @Bean
    public DynamicConfiguration addApplicationPropertiesSource() throws IOException {
        URL configPropertyURL = (new ClassPathResource(fileName)).getURL();
        PolledConfigurationSource source = new URLConfigurationSource(configPropertyURL);
        return new DynamicConfiguration(source, new FixedDelayPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource));
    }

    @Bean
    public DynamicPropertyFactory dynamicPropertyFactory(@Autowired(required = false) Map<String, PolledConfigurationSource> configurationSourcesMap) throws IOException {
        FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(1000, 5000, false);
        ConcurrentCompositeConfiguration configuration = new ConcurrentCompositeConfiguration();
        if (configurationSourcesMap!=null) {
            configurationSourcesMap.forEach((configName, config) -> {
                DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(config, scheduler);
                configuration.addConfiguration(dynamicConfiguration, configName);
            });
        }
        configuration.addConfiguration(addApplicationPropertiesSource(), "fileConfig");
        ConfigurationManager.install(configuration);
        return DynamicPropertyFactory.getInstance();
    }

    @PostConstruct
    public void initEnvironment() {
        if (env instanceof ConfigurableEnvironment configurableEnvironment) {
            MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
            propertySources.addFirst(archaiusPropertySource);
        }
    }

}
