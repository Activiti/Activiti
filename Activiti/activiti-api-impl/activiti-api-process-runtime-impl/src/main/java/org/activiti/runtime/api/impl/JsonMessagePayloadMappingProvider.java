package org.activiti.runtime.api.impl;

import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.el.ExpressionManager;

public class JsonMessagePayloadMappingProvider implements MessagePayloadMappingProvider {
    
    private final Event bpmnEvent;
    private final MessageEventDefinition messageEventDefinition;
    private final ExpressionManager expressionManager;
    private final VariablesMappingProvider variablesMappingProvider;

    public JsonMessagePayloadMappingProvider(Event bpmnEvent,
                                             MessageEventDefinition messageEventDefinition,
                                             ExpressionManager expressionManager,
                                             VariablesMappingProvider variablesMappingProvider) {
        this.bpmnEvent = bpmnEvent;
        this.messageEventDefinition = messageEventDefinition;
        this.expressionManager = expressionManager;
        this.variablesMappingProvider = variablesMappingProvider;
    }
    
    public Optional<Map<String, Object>> getMessagePayload(DelegateExecution execution) {
        return Optional.of(variablesMappingProvider.calculateInputVariables(execution))
                       .filter(payload -> !payload.isEmpty());
    }
    
    public Event getBpmnEvent() {
        return bpmnEvent;
    }
    
    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }
    
    public ExpressionManager getExpressionManager() {
        return expressionManager;
    }
    
    public VariablesMappingProvider getVariablesMappingProvider() {
        return variablesMappingProvider;
    }
}
