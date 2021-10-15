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
package org.activiti.runtime.api.event.impl;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.runtime.event.impl.RuntimeEventImpl;

public class ProcessCancelledImpl
    extends RuntimeEventImpl<ProcessInstance, ProcessRuntimeEvent.ProcessEvents>
    implements ProcessCancelledEvent {

    private String cause;

    public ProcessCancelledImpl(ProcessInstance entity, String cause) {
        super(entity);
        this.cause = cause;
    }

    @Override
    public ProcessEvents getEventType() {
        return ProcessEvents.PROCESS_CANCELLED;
    }

    @Override
    public String getCause() {
        return cause;
    }
}
