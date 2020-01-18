package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.BPMNActivityCompleted;
import org.activiti.runtime.api.model.BPMNActivity;

public class BPMNActivityCompletedEventImpl extends BPMNActivityEventImpl implements BPMNActivityCompleted {

    public BPMNActivityCompletedEventImpl() {
    }

    public BPMNActivityCompletedEventImpl(BPMNActivity entity) {
        super(entity);
    }

    @Override
    public ActivityEvents getEventType() {
        return ActivityEvents.ACTIVITY_COMPLETED;
    }
}
