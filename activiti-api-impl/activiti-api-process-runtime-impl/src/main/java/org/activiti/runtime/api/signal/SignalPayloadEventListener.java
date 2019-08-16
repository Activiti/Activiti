package org.activiti.runtime.api.signal;

import org.activiti.api.process.model.payloads.SignalPayload;

/**
 * SignalPayloadEventListener handler interface.
 */
public interface SignalPayloadEventListener {
    public void sendSignal(SignalPayload signalPayload);
}
