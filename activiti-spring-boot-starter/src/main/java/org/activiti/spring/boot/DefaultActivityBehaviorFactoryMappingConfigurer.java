package org.activiti.spring.boot;

import org.activiti.runtime.api.impl.DefaultActivityBehaviorFactoryMapping;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessExtensionService;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultActivityBehaviorFactoryMappingConfigurer implements ProcessEngineConfigurationConfigurer {

    private ProcessExtensionService  processExtensionService;

    public DefaultActivityBehaviorFactoryMappingConfigurer(ProcessExtensionService  processExtensionService){
        this.processExtensionService = processExtensionService;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new DefaultActivityBehaviorFactoryMapping(processExtensionService));
    }
}