package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerScheduledEvent;

public class BPMNTimerScheduledEventImpl extends RuntimeEventImpl<BPMNTimer, BPMNTimerEvent.TimerEvents> implements BPMNTimerScheduledEvent {

    public BPMNTimerScheduledEventImpl() {
    }

    public BPMNTimerScheduledEventImpl(BPMNTimer entity) {
        super(entity);
    }

    @Override
    public TimerEvents getEventType() {
        return BPMNTimerEvent.TimerEvents.TIMER_SCHEDULED;
    }

    @Override
    public String toString() {
        return "BPMNTimerScheduledEventImpl{" + super.toString() + "}";
    }
}
