package org.activiti.runtime.api.impl;

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProviderFactory;
import org.activiti.engine.impl.el.ExpressionManager;

public class JsonMessagePayloadMappingProviderFactory implements MessagePayloadMappingProviderFactory {
    
    private final VariablesMappingProvider variablesMappingProvider;

    public JsonMessagePayloadMappingProviderFactory(VariablesMappingProvider variablesMappingProvider) {
        this.variablesMappingProvider = variablesMappingProvider;
    }

    @Override
    public MessagePayloadMappingProvider create(Event bpmnEvent,
                                                MessageEventDefinition messageEventDefinition,
                                                ExpressionManager expressionManager) {
        return new JsonMessagePayloadMappingProvider(bpmnEvent,
                                                     messageEventDefinition,
                                                     expressionManager,
                                                     variablesMappingProvider);
    }

}
