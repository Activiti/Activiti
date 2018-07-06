package org.activiti.runtime.api.event;

import org.activiti.runtime.api.model.BPMNActivity;

public interface BPMNActivityEvent extends RuntimeEvent<BPMNActivity, BPMNActivityEvent.ActivityEvents> {

    enum ActivityEvents {

        ACTIVITY_STARTED,

        ACTIVITY_CANCELLED,

        ACTIVITY_COMPLETED

    }
}
