package org.activiti.api.process.model.events;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.BPMNError;

public interface BPMNErrorReceivedEvent extends RuntimeEvent<BPMNError, BPMNErrorReceivedEvent.ErrorEvents> {

    enum ErrorEvents {
        ERROR_RECEIVED
    }
}
