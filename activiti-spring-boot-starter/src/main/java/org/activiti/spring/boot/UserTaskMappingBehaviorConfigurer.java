package org.activiti.spring.boot;

import org.activiti.engine.impl.bpmn.helper.DefaultClassDelegateFactory;
import org.activiti.runtime.api.impl.DefaultActivityBehaviorFactoryMapping;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessExtensionService;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserTaskMappingBehaviorConfigurer implements ProcessEngineConfigurationConfigurer {

    private ProcessExtensionService  processExtensionService;

    public UserTaskMappingBehaviorConfigurer(ProcessExtensionService  processExtensionService){
        this.processExtensionService = processExtensionService;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new DefaultActivityBehaviorFactoryMapping(processExtensionService));
    }
}