package org.activiti.api.process.model.events;


import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.BPMNActivity;

public interface BPMNActivityEvent extends RuntimeEvent<BPMNActivity, BPMNActivityEvent.ActivityEvents> {

    enum ActivityEvents {

        ACTIVITY_STARTED,

        ACTIVITY_CANCELLED,

        ACTIVITY_COMPLETED
    }
}
