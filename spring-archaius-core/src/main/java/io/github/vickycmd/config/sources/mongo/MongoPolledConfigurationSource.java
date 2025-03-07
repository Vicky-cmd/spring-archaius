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

@Slf4j
@Service(value = "mongoConfig")
@ConditionalOnProperty(prefix = "spring.archaius.config", name = "source", havingValue = "mongo")
public class MongoPolledConfigurationSource implements PolledConfigurationSource {

    private final ApplicationPropertyRepository applicationPropertyRepository;

    @Autowired
    public MongoPolledConfigurationSource(ApplicationPropertyRepository applicationPropertyRepository) {
        this.applicationPropertyRepository = applicationPropertyRepository;
    }

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
