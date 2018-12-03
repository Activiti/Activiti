package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.runtime.event.impl.RuntimeEventImpl;

public abstract class BPMNActivityEventImpl extends RuntimeEventImpl<BPMNActivity, BPMNActivityEvent.ActivityEvents>
        implements BPMNActivityEvent {

    public BPMNActivityEventImpl() {
    }

    public BPMNActivityEventImpl(BPMNActivity entity) {
        super(entity);
    }


}
