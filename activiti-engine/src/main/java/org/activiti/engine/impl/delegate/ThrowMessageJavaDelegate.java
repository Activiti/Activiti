package org.activiti.engine.impl.delegate;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;

public class ThrowMessageJavaDelegate implements ThrowMessageDelegate {
    
    private final Class<? extends ThrowMessageDelegate> clazz;
    private final List<FieldDeclaration> fieldDeclarations;
    
    public ThrowMessageJavaDelegate(Class<? extends ThrowMessageDelegate> clazz,
                                    List<FieldDeclaration> fieldDeclarations) {
        this.clazz = clazz;
        this.fieldDeclarations = fieldDeclarations;
    }

    @Override
    public boolean send(DelegateExecution execution, ThrowMessage message) {

        Object delegate = (ThrowMessageDelegate) ClassDelegate.defaultInstantiateDelegate(clazz, fieldDeclarations);
        
        if(ThrowMessageDelegate.class.isInstance(delegate)) {
            return ThrowMessageDelegate.class.cast(delegate)
                                             .send(execution, message);
        }
        
        return false;
    }
}