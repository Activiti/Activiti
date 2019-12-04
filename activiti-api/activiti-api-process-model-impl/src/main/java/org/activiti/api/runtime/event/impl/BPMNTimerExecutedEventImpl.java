package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerExecutedEvent;

public class BPMNTimerExecutedEventImpl extends RuntimeEventImpl<BPMNTimer, BPMNTimerEvent.TimerEvents> implements BPMNTimerExecutedEvent {

    public BPMNTimerExecutedEventImpl() {
    }

    public BPMNTimerExecutedEventImpl(BPMNTimer entity) {
        super(entity);
    }

    @Override
    public TimerEvents getEventType() {
        return BPMNTimerEvent.TimerEvents.TIMER_EXECUTED;
    }

    @Override
    public String toString() {
        return "BPMNTimerExecutedEventImpl{" + super.toString() + "}";
    }
}
