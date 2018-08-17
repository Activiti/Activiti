package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.events.BPMNActivityEvent;

public abstract class BPMNActivityEventImpl extends RuntimeEventImpl<BPMNActivity, BPMNActivityEvent.ActivityEvents>
        implements BPMNActivityEvent {

    public BPMNActivityEventImpl() {
    }

    public BPMNActivityEventImpl(BPMNActivity entity) {
        super(entity);
    }
}
