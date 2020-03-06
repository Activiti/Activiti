package org.activiti.runtime.api.impl;

import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.engine.RuntimeService;
import org.activiti.runtime.api.signal.SignalPayloadEventListener;
import org.springframework.context.event.EventListener;

/**
 * Default implementation of SignalPayloadEventListener that delegates 
 * Spring SignalPayload event into embedded RuntimeService.  
 * 
 */
public class RuntimeSignalPayloadEventListener implements SignalPayloadEventListener {
    
    private final RuntimeService runtimeService;
    
    public RuntimeSignalPayloadEventListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    @EventListener
    public void sendSignal(SignalPayload signalPayload) {
        runtimeService.signalEventReceived(signalPayload.getName(),
                                           signalPayload.getVariables());
    }

}
