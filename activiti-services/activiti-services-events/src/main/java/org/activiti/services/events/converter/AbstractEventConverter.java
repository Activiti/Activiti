package org.activiti.services.events.converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractEventConverter implements EventConverter{

    @Value("${spring.application.name}")
    protected String applicationName;

    public String getApplicationName() {
        return applicationName;
    }
}
