package org.activiti.spring.boot;

import org.activiti.runtime.api.impl.MappingAwareActivityBehaviorFactory;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessVariablesInitiator;

public class DefaultActivityBehaviorFactoryMappingConfigurer implements ProcessEngineConfigurationConfigurer {

    private VariablesMappingProvider variablesMappingProvider;

    private ProcessVariablesInitiator processVariablesInitiator;

    public DefaultActivityBehaviorFactoryMappingConfigurer(VariablesMappingProvider variablesMappingProvider,
                                                           ProcessVariablesInitiator processVariablesInitiator){
        this.variablesMappingProvider = variablesMappingProvider;
        this.processVariablesInitiator = processVariablesInitiator;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new MappingAwareActivityBehaviorFactory(variablesMappingProvider,
                                                                                                      processVariablesInitiator));
    }
}