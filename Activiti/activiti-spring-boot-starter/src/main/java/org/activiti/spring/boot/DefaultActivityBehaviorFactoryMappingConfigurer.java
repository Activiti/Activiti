package org.activiti.spring.boot;

import org.activiti.engine.impl.event.EventSubscriptionPayloadMappingProvider;
import org.activiti.runtime.api.impl.MappingAwareActivityBehaviorFactory;
import org.activiti.runtime.api.impl.VariablesMappingProvider;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.process.ProcessVariablesInitiator;

public class DefaultActivityBehaviorFactoryMappingConfigurer implements ProcessEngineConfigurationConfigurer {

    private VariablesMappingProvider variablesMappingProvider;

    private ProcessVariablesInitiator processVariablesInitiator;
    
    private final EventSubscriptionPayloadMappingProvider eventSubscriptionPayloadMappingProvider;

    public DefaultActivityBehaviorFactoryMappingConfigurer(VariablesMappingProvider variablesMappingProvider,
                                                           ProcessVariablesInitiator processVariablesInitiator,
                                                           EventSubscriptionPayloadMappingProvider eventSubscriptionPayloadMappingProvider){
        this.variablesMappingProvider = variablesMappingProvider;
        this.processVariablesInitiator = processVariablesInitiator;
        this.eventSubscriptionPayloadMappingProvider = eventSubscriptionPayloadMappingProvider;
    }
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration){
        processEngineConfiguration.setEventSubscriptionPayloadMappingProvider(eventSubscriptionPayloadMappingProvider);

        processEngineConfiguration.setActivityBehaviorFactory(new MappingAwareActivityBehaviorFactory(variablesMappingProvider,
                                                                                                      processVariablesInitiator));
    }
}