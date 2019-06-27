package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.events.BPMNTimerCanceledEvent;
import org.activiti.api.process.model.events.BPMNTimerEvent;

public class BPMNTimerCanceledEventImpl extends RuntimeEventImpl<BPMNTimer, BPMNTimerEvent.TimerEvents> implements BPMNTimerCanceledEvent {

    public BPMNTimerCanceledEventImpl() {
    }

    public BPMNTimerCanceledEventImpl(BPMNTimer entity) {
        super(entity);
    }

    @Override
    public TimerEvents getEventType() {
        return BPMNTimerEvent.TimerEvents.JOB_CANCELED;
    }

    @Override
    public String toString() {
        return "BPMNTimerCanceledEventImpl{" + super.toString() + "}";
    }
}
