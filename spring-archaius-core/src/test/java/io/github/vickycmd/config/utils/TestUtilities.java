package io.github.vickycmd.config.utils;

import com.netflix.config.*;
import org.slf4j.Logger;

public class TestUtilities {

    public static void triggerConfigReload(PolledConfigurationSource source, FixedDelayPollingScheduler scheduler, Logger logger) {
        ConcurrentCompositeConfiguration cfg = (ConcurrentCompositeConfiguration) DynamicPropertyFactory.getBackingConfigurationSource();
        if (cfg != null)
        {
            if (source != null && scheduler != null) {
                cfg.getConfigurations().forEach(config -> {
                    if (config instanceof DynamicConfiguration dynamicConfig) {
                        dynamicConfig.startPolling(source, scheduler);
                    }
                });
            } else {
                logger.error("Invalid Scheduler/Source Configurations");
            }
        }
    }
}
