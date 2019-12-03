package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerRetriesDecrementedEvent;

public class BPMNTimerRetriesDecrementedEventImpl extends RuntimeEventImpl<BPMNTimer, BPMNTimerEvent.TimerEvents> implements BPMNTimerRetriesDecrementedEvent {

    public BPMNTimerRetriesDecrementedEventImpl() {
    }

    public BPMNTimerRetriesDecrementedEventImpl(BPMNTimer entity) {
        super(entity);
    }

    @Override
    public TimerEvents getEventType() {
        return BPMNTimerEvent.TimerEvents.TIMER_RETRIES_DECREMENTED;
    }

    @Override
    public String toString() {
        return "BPMNTimerRetriesDecrementedEventImpl{" + super.toString() + "}";
    }
}
