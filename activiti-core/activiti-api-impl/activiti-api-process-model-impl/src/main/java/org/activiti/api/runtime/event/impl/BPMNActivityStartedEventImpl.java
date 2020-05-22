/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;

public class BPMNActivityStartedEventImpl extends BPMNActivityEventImpl implements BPMNActivityStartedEvent {

    public BPMNActivityStartedEventImpl() {
    }

    public BPMNActivityStartedEventImpl(BPMNActivity entity) {
        super(entity);
    }

    @Override
    public BPMNActivityEvent.ActivityEvents getEventType() {
        return BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED;
    }

    @Override
    public String toString() {
        return "BPMNActivityStartedEventImpl{" + super.toString() + "}";
    }
}
