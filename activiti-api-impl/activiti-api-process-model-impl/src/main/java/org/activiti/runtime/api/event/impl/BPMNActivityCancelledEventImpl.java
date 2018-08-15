package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;

public class BPMNActivityCancelledEventImpl extends BPMNActivityEventImpl implements BPMNActivityCancelledEvent {

    public BPMNActivityCancelledEventImpl() {
    }

    public BPMNActivityCancelledEventImpl(BPMNActivity entity) {
        super(entity);
    }

    @Override
    public ActivityEvents getEventType() {
        return ActivityEvents.ACTIVITY_CANCELLED;
    }
}
