package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNTimer;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.BPMNTimerExecutionSuccessEvent;

public class BPMNTimerExecutionSuccessEventImpl extends RuntimeEventImpl<BPMNTimer, BPMNTimerEvent.TimerEvents> implements BPMNTimerExecutionSuccessEvent {

    public BPMNTimerExecutionSuccessEventImpl() {
    }

    public BPMNTimerExecutionSuccessEventImpl(BPMNTimer entity) {
        super(entity);
    }

    @Override
    public TimerEvents getEventType() {
        return BPMNTimerEvent.TimerEvents.JOB_EXECUTION_SUCCESS;
    }

    @Override
    public String toString() {
        return "BPMNTimerExecutionSuccessEventImpl{" + super.toString() + "}";
    }
}
