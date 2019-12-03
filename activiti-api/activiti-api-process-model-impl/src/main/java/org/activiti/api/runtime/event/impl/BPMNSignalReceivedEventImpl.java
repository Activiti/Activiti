package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNSignal;
import org.activiti.api.process.model.events.BPMNSignalEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;

public class BPMNSignalReceivedEventImpl extends RuntimeEventImpl<BPMNSignal, BPMNSignalEvent.SignalEvents> implements BPMNSignalReceivedEvent {

    public BPMNSignalReceivedEventImpl() {
    }

    public BPMNSignalReceivedEventImpl(BPMNSignal entity) {
        super(entity);
    }

    @Override
    public SignalEvents getEventType() {
        return BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED;
    }

    @Override
    public String toString() {
        return "BPMNSignalReceivedEventImpl{" + super.toString() + "}";
    }
}
