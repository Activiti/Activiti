package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.BPMNActivityStarted;
import org.activiti.runtime.api.model.BPMNActivity;

public class BPMNActivityStartedEventImpl extends BPMNActivityEventImpl implements BPMNActivityStarted {

    public BPMNActivityStartedEventImpl() {
    }

    public BPMNActivityStartedEventImpl(BPMNActivity entity) {
        super(entity);
    }

    @Override
    public ActivityEvents getEventType() {
        return ActivityEvents.ACTIVITY_STARTED;
    }
}
