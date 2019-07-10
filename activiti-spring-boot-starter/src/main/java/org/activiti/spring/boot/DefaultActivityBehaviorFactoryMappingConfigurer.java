package org.activiti.spring.boot;

import org.activiti.runtime.api.impl.DefaultActivityBehaviorFactoryMapping;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.ProcessVariablesInitiator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultActivityBehaviorFactoryMappingConfigurer implements ProcessEngineConfigurationConfigurer {

    private ProcessExtensionService  processExtensionService;
    private ProcessVariablesInitiator processVariablesInitiator;

    public DefaultActivityBehaviorFactoryMappingConfigurer(ProcessExtensionService  processExtensionService,ProcessVariablesInitiator processVariablesInitiator){
        this.processExtensionService = processExtensionService;
        this.processVariablesInitiator = processVariablesInitiator;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new DefaultActivityBehaviorFactoryMapping(processExtensionService,processVariablesInitiator));
    }
}