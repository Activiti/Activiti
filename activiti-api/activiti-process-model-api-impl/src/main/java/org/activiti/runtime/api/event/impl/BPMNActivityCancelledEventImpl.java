package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.BPMNActivityCancelled;
import org.activiti.runtime.api.model.BPMNActivity;

public class BPMNActivityCancelledEventImpl extends BPMNActivityEventImpl implements BPMNActivityCancelled {

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
