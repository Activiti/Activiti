package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNMessage;
import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.model.events.BPMNMessageWaitingEvent;

public class BPMNMessageWaitingEventImpl extends RuntimeEventImpl<BPMNMessage, BPMNMessageEvent.MessageEvents> implements BPMNMessageWaitingEvent {

    public BPMNMessageWaitingEventImpl() {
    }

    public BPMNMessageWaitingEventImpl(BPMNMessage entity) {
        super(entity);
    }

    @Override
    public MessageEvents getEventType() {
        return BPMNMessageEvent.MessageEvents.MESSAGE_WAITING;
    }

    @Override
    public String toString() {
        return "BPMNMessageWaitingEventImpl{" + super.toString() + "}";
    }
}
