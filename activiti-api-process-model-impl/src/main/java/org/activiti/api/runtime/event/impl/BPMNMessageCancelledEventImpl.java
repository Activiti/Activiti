package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.events.BPMNMessageCancelledEvent;
import org.activiti.api.process.model.events.BPMNMessageEvent;

public class BPMNMessageCancelledEventImpl extends RuntimeEventImpl<BPMNMessage, BPMNMessageEvent.MessageEvents> implements BPMNMessageCancelledEvent {

    public BPMNMessageCancelledEventImpl() {
    }

    public BPMNMessageCancelledEventImpl(BPMNMessage entity) {
        super(entity);
        
        setProcessInstanceId(entity.getProcessInstanceId());
        setProcessDefinitionId(entity.getProcessDefinitionId());
    }

    @Override
    public MessageEvents getEventType() {
        return BPMNMessageEvent.MessageEvents.MESSAGE_CANCELLED;
    }

    @Override
    public String toString() {
        return "BPMNMessageCancelledEventImpl{" + super.toString() + "}";
    }
}
