package org.activiti.api.process.model.events;


import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.model.common.event.RuntimeEvent;

public interface BPMNActivityEvent extends RuntimeEvent<BPMNActivity, BPMNActivityEvent.ActivityEvents> {

    enum ActivityEvents {

        ACTIVITY_STARTED,

        ACTIVITY_CANCELLED,

        ACTIVITY_COMPLETED

    }
}
