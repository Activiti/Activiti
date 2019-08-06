package org.activiti.engine.impl.delegate;


public interface ThrowMessageDelegateFactory {
    
    default ThrowMessageDelegate create() {
        return new DefaultThrowMessageJavaDelegate();
    };

}
