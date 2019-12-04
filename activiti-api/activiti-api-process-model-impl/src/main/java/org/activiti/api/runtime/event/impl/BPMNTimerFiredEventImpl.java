package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerFiredEvent;

public class BPMNTimerFiredEventImpl extends RuntimeEventImpl<BPMNTimer, BPMNTimerEvent.TimerEvents> implements BPMNTimerFiredEvent {

    public BPMNTimerFiredEventImpl() {
    }

    public BPMNTimerFiredEventImpl(BPMNTimer entity) {
        super(entity);
    }

    @Override
    public TimerEvents getEventType() {
        return BPMNTimerEvent.TimerEvents.TIMER_FIRED;
    }

    @Override
    public String toString() {
        return "BPMNTimerFiredEventImpl{" + super.toString() + "}";
    }
}
