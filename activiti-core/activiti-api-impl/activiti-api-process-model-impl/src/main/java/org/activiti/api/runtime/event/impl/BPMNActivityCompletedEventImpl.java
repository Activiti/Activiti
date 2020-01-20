package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;

public class BPMNActivityCompletedEventImpl extends BPMNActivityEventImpl implements BPMNActivityCompletedEvent {

    public BPMNActivityCompletedEventImpl() {
    }

    public BPMNActivityCompletedEventImpl(BPMNActivity entity) {
        super(entity);
    }

    @Override
    public ActivityEvents getEventType() {
        return ActivityEvents.ACTIVITY_COMPLETED;
    }

    @Override
    public String toString() {
        return "BPMNActivityCompletedEventImpl{" + super.toString() + "}";
    }
}
