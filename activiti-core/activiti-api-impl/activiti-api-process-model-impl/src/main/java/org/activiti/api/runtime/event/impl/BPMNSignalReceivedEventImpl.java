/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.BPMNSignal;
import org.activiti.api.process.model.events.BPMNSignalEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;

public class BPMNSignalReceivedEventImpl extends RuntimeEventImpl<BPMNSignal, BPMNSignalEvent.SignalEvents> implements BPMNSignalReceivedEvent {

    public BPMNSignalReceivedEventImpl() {
    }

    public BPMNSignalReceivedEventImpl(BPMNSignal entity) {
        super(entity);
    }

    @Override
    public SignalEvents getEventType() {
        return BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED;
    }

    @Override
    public String toString() {
        return "BPMNSignalReceivedEventImpl{" + super.toString() + "}";
    }
}
