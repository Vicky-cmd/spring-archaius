package io.github.vickycmd.config.sources.mongo;

import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;
import io.github.vickycmd.config.model.ApplicationProperty;
import io.github.vickycmd.config.sources.ApplicationPropertyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MongoPolledConfigurationSource is a service class that implements the PolledConfigurationSource
 * interface to provide configuration properties from a MongoDB source. This class is annotated
 * with @Service and is conditionally loaded based on the 'spring.archaius.config.source' property
 * having the value 'mongo'.
 *
 * <p>The class uses an ApplicationPropertyRepository to retrieve all application properties
 * from the database. These properties are then converted into a map where the keys are the
 * property keys and the values are the property values. This map is used to create a PollResult
 * which is returned by the poll method.
 *
 * <p>Dependencies:
 * <ul>
 *   <li>ApplicationPropertyRepository: Repository interface for accessing application properties.</li>
 *   <li>PollResult: Class from com.netflix.config used to represent the result of a poll operation.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * {@code
 * @Autowired
 * private MongoPolledConfigurationSource mongoConfigSource;
 * }
 * </pre>
 *
 * <p>Related Classes:
 * <ul>
 *   <li>ApplicationProperty: Model class representing a configuration property.</li>
 *   <li>ApplicationPropertyRepository: Interface for accessing configuration properties.</li>
 * </ul>
 *
 * <p>Author: Vicky CMD
 *
 * @see com.netflix.config.PolledConfigurationSource
 * @see com.netflix.config.PollResult
 * @see io.github.vickycmd.config.model.ApplicationProperty
 * @see io.github.vickycmd.config.sources.ApplicationPropertyRepository
 */
@Slf4j
@Service(value = "mongoConfig")
@ConditionalOnProperty(prefix = "spring.archaius.config", name = "source", havingValue = "mongo")
public class MongoPolledConfigurationSource implements PolledConfigurationSource {

    private final ApplicationPropertyRepository applicationPropertyRepository;

    /**
     * Constructs a new MongoPolledConfigurationSource with the specified
     * ApplicationPropertyRepository. This constructor is annotated with
     * {@link org.springframework.beans.factory.annotation.Autowired} to enable dependency injection of the repository, which
     * is used to retrieve application properties from the database.
     *
     * @param applicationPropertyRepository the repository for accessing
     *                                      application properties
     */
    @Autowired
    public MongoPolledConfigurationSource(ApplicationPropertyRepository applicationPropertyRepository) {
        this.applicationPropertyRepository = applicationPropertyRepository;
    }

    /**
     * Polls the configuration source for the latest properties.
     *
     * <p>This method retrieves all application properties from the
     * {@link ApplicationPropertyRepository} and converts them into a map
     * where the keys are property keys and the values are property values.
     * If no properties are found, an empty map is returned.</p>
     *
     * @param b a boolean flag indicating whether the poll is initial or not.
     * @param o an optional object parameter for additional context, not used in this implementation.
     * @return a {@link PollResult} containing the full set of properties.
     * @throws Exception if an error occurs during the polling process.
     */
    @Override
    public PollResult poll(boolean b, Object o) throws Exception {
        List<ApplicationProperty> applicationProperties = applicationPropertyRepository.findAll();
        Map<String, Object> propsMap = applicationProperties!=null?
                applicationProperties.stream()
                        .collect(Collectors.toMap(ApplicationProperty::getKey, ApplicationProperty::getValue))
                : Collections.emptyMap();
        return PollResult.createFull(propsMap);
    }
}
