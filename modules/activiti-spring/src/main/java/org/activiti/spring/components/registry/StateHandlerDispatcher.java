package org.activiti.spring.components.registry;

import org.springframework.util.Assert;

public class StateHandlerDispatcher {


    private StateHandlerRegistry stateHandlerRegistry;

    public StateHandlerDispatcher(StateHandlerRegistry stateHandlerRegistry) {
        this.stateHandlerRegistry = stateHandlerRegistry;
        Assert.notNull(this.stateHandlerRegistry, "stateHandlerRegistry mustn't be null");
    }


}
