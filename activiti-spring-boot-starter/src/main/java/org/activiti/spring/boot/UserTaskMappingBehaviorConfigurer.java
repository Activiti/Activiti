package org.activiti.spring.boot;

import org.activiti.engine.impl.bpmn.helper.DefaultClassDelegateFactory;
import org.activiti.runtime.api.impl.DefaultActivityBehaviorFactoryMapping;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessExtensionService;

public class UserTaskMappingBehaviorConfigurer implements ProcessEngineConfigurationConfigurer {

    private DefaultClassDelegateFactory defaultClassDelegateFactory;
    private ProcessExtensionService  processExtensionService;

    public UserTaskMappingBehaviorConfigurer(DefaultClassDelegateFactory defaultClassDelegateFactory,
                                             ProcessExtensionService  processExtensionService){
        this.defaultClassDelegateFactory = defaultClassDelegateFactory;
        this.processExtensionService = processExtensionService;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new DefaultActivityBehaviorFactoryMapping(defaultClassDelegateFactory,
                                                                                                        processExtensionService));
    }
}