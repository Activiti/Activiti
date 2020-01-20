package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.model.events.BPMNMessageSentEvent;

public class BPMNMessageSentEventImpl extends RuntimeEventImpl<BPMNMessage, BPMNMessageEvent.MessageEvents> implements BPMNMessageSentEvent {

    public BPMNMessageSentEventImpl() {
    }

    public BPMNMessageSentEventImpl(BPMNMessage entity) {
        super(entity);
    }

    @Override
    public MessageEvents getEventType() {
        return BPMNMessageEvent.MessageEvents.MESSAGE_SENT;
    }

    @Override
    public String toString() {
        return "BPMNMessageSentEventImpl{" + super.toString() + "}";
    }
}
