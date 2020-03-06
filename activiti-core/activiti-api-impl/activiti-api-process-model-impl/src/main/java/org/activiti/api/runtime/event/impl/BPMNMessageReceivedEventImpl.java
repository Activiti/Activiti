package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.model.events.BPMNMessageReceivedEvent;

public class BPMNMessageReceivedEventImpl extends RuntimeEventImpl<BPMNMessage, BPMNMessageEvent.MessageEvents> implements BPMNMessageReceivedEvent {

    public BPMNMessageReceivedEventImpl() {
    }

    public BPMNMessageReceivedEventImpl(BPMNMessage entity) {
        super(entity);
    }

    @Override
    public MessageEvents getEventType() {
        return BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED;
    }

    @Override
    public String toString() {
        return "BPMNMessageReceivedEventImpl{" + super.toString() + "}";
    }
}
