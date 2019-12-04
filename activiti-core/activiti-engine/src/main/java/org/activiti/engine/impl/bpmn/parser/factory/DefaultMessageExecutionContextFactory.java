package org.activiti.engine.impl.bpmn.parser.factory;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.el.ExpressionManager;

public class DefaultMessageExecutionContextFactory implements MessageExecutionContextFactory {
    
    
    public DefaultMessageExecutionContextFactory() {
        super();
    }
    
    @Override
    public MessageExecutionContext create(MessageEventDefinition messageEventDefinition,
                                          MessagePayloadMappingProvider messagePayloadMappingProvider,
                                          ExpressionManager expressionManager) {
        
        return new DefaultMessageExecutionContext(messageEventDefinition, 
                                                  expressionManager, 
                                                  messagePayloadMappingProvider);
    }
    
}
