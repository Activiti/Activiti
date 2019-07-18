package org.activiti.spring.boot;

import org.activiti.runtime.api.impl.MappingAwareActivityBehaviorFactory;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultActivityBehaviorFactoryMappingConfigurer implements ProcessEngineConfigurationConfigurer {

    private VariablesMappingProvider variablesMappingProvider;

    public DefaultActivityBehaviorFactoryMappingConfigurer(VariablesMappingProvider variablesMappingProvider){
        this.variablesMappingProvider = variablesMappingProvider;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setActivityBehaviorFactory(new MappingAwareActivityBehaviorFactory(variablesMappingProvider));
    }
}