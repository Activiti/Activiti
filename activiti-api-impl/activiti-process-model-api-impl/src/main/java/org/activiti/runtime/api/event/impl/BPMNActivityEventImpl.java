package org.activiti.runtime.api.event.impl;

import org.activiti.runtime.api.event.BPMNActivityEvent;
import org.activiti.runtime.api.model.BPMNActivity;

public abstract class BPMNActivityEventImpl extends RuntimeEventImpl<BPMNActivity, BPMNActivityEvent.ActivityEvents>
        implements BPMNActivityEvent {

    public BPMNActivityEventImpl() {
    }

    public BPMNActivityEventImpl(BPMNActivity entity) {
        super(entity);
    }
}
