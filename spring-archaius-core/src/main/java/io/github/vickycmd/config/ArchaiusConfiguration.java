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

/**
 * Configuration class for integrating Archaius with Spring, enabling dynamic property management.
 * <p>
 * This class configures Archaius to work with Spring's environment abstraction, allowing properties
 * to be dynamically loaded and refreshed from external sources. It uses a combination of Archaius's
 * DynamicConfiguration and Spring's Environment to manage application properties.
 * </p>
 * <p>
 * The configuration sources are polled at a fixed delay, and the properties are made available
 * through the {@link DynamicPropertyFactory}. The class also ensures that the Archaius properties
 * are added to the Spring environment's property sources.
 * </p>
 *
 * Related Classes:
 * <ul>
 *   <li>{@link ArchaiusPropertySource}</li>
 *   <li>{@link DynamicPropertyFactory}</li>
 *   <li>{@link URLConfigurationSource}</li>
 * </ul>
 *
 * Usage:
 * <ul>
 *   <li>Configure the source file and polling intervals using Spring properties.</li>
 *   <li>Ensure this configuration is scanned by Spring's component scanning.</li>
 * </ul>
 *
 * Author: Vicky CMD
 * Since: 1.0
 * Version: 1.0
 */
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

    /**
     * Constructs an ArchaiusConfiguration instance with the specified ArchaiusPropertySource
     * and Spring Environment. Initializes the configuration with the provided property source
     * and environment context.
     *
     * @param archaiusPropertySource the Archaius property source to be used
     * @param env the Spring environment to be used for property management
     */
    public ArchaiusConfiguration(ArchaiusPropertySource archaiusPropertySource, Environment env) {
        this.archaiusPropertySource = archaiusPropertySource;
        this.env = env;
    }

    /**
     * Creates a DynamicConfiguration bean that loads application properties from a specified
     * file on the classpath. The configuration source is polled at a fixed delay, allowing
     * for dynamic updates of properties.
     *
     * @return a DynamicConfiguration instance configured with a URLConfigurationSource
     *         and a FixedDelayPollingScheduler.
     * @throws IOException if there is an error accessing the configuration file.
     */
    @Bean
    public DynamicConfiguration addApplicationPropertiesSource() throws IOException {
        URL configPropertyURL = (new ClassPathResource(fileName)).getURL();
        PolledConfigurationSource source = new URLConfigurationSource(configPropertyURL);
        return new DynamicConfiguration(source, new FixedDelayPollingScheduler(initialDelayMillis, delayMillis, ignoreDeletesFromSource));
    }

    /**
     * Creates a DynamicPropertyFactory bean that manages dynamic property configurations
     * using a combination of provided configuration sources and a default file-based source.
     * <p>
     * This method initializes a FixedDelayPollingScheduler to periodically poll the configuration
     * sources for updates. It constructs a ConcurrentCompositeConfiguration to aggregate multiple
     * DynamicConfiguration instances, each associated with a different configuration source.
     * If a map of configuration sources is provided, each source is added to the composite
     * configuration with its respective name.
     * </p>
     * <p>
     * The method also adds a default file-based configuration source using the
     * {@link #addApplicationPropertiesSource()} method. The resulting composite configuration
     * is installed into the ConfigurationManager, allowing the DynamicPropertyFactory to
     * provide access to the aggregated properties.
     * </p>
     *
     * @param configurationSourcesMap an optional map of configuration sources to be included
     *                                in the composite configuration, where the key is the
     *                                configuration name and the value is the source.
     * @return a DynamicPropertyFactory instance configured with the composite configuration.
     * @throws IOException if there is an error accessing the configuration file.
     */
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

    /**
     * Initializes the Spring environment by adding the Archaius property source
     * to the beginning of the property sources list. This ensures that Archaius
     * properties take precedence over other property sources in the Spring
     * environment. The method is executed after the bean's construction and
     * dependency injection are complete, leveraging the @PostConstruct annotation.
     * It checks if the current environment is an instance of ConfigurableEnvironment
     * and modifies its property sources accordingly.
     */
    @PostConstruct
    public void initEnvironment() {
        if (env instanceof ConfigurableEnvironment configurableEnvironment) {
            MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
            propertySources.addFirst(archaiusPropertySource);
        }
    }

}
