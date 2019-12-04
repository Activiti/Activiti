package org.activiti.engine.impl.delegate;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.DelegateExpressionUtil;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;

public class ThrowMessageDelegateExpression implements ThrowMessageDelegate {
    
    private final Expression delegateExpression;
    private final List<FieldDeclaration> fieldDeclarations;
    
    public ThrowMessageDelegateExpression(Expression delegateExpression,
                                          List<FieldDeclaration> fieldDeclarations) {
        this.delegateExpression = delegateExpression;
        this.fieldDeclarations = fieldDeclarations;
    }

    @Override
    public boolean send(DelegateExecution execution, ThrowMessage message) {
        
        Object delegate = DelegateExpressionUtil.resolveDelegateExpression(delegateExpression, 
                                                                           execution, 
                                                                           fieldDeclarations);        
        if(ThrowMessageDelegate.class.isInstance(delegate)) {
            return ThrowMessageDelegate.class.cast(delegate)
                                             .send(execution, message);
        }
        
        return false;
    }
}