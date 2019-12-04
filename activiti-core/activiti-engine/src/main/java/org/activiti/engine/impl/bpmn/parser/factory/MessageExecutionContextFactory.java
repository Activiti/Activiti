package org.activiti.engine.impl.bpmn.parser.factory;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.el.ExpressionManager;

public interface MessageExecutionContextFactory {
    
    MessageExecutionContext create(MessageEventDefinition messageEventDefinition,
                                   MessagePayloadMappingProvider messagePayloadMappingProvider,
                                   ExpressionManager expressionManager);

}
