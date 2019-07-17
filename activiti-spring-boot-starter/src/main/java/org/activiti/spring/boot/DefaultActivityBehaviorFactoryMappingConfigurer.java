package org.activiti.spring.boot;

import org.activiti.runtime.api.impl.MappingAwareActivityBehaviorFactory;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.ProcessVariablesInitiator;

public class DefaultActivityBehaviorFactoryMappingConfigurer implements ProcessEngineConfigurationConfigurer {

    private ProcessExtensionService  processExtensionService;
    private ProcessVariablesInitiator processVariablesInitiator;

    public DefaultActivityBehaviorFactoryMappingConfigurer(ProcessExtensionService  processExtensionService,ProcessVariablesInitiator processVariablesInitiator){
        this.processExtensionService = processExtensionService;
        this.processVariablesInitiator = processVariablesInitiator;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new MappingAwareActivityBehaviorFactory(processExtensionService, processVariablesInitiator));
    }
}