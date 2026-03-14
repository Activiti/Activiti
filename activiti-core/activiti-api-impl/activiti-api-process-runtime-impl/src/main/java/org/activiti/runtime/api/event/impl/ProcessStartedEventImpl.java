/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.runtime.event.impl.RuntimeEventImpl;

public class ProcessStartedEventImpl
    extends RuntimeEventImpl<ProcessInstance, ProcessRuntimeEvent.ProcessEvents>
    implements ProcessStartedEvent {

    private String nestedProcessDefinitionId;
    private String nestedProcessInstanceId;
    private String linkedProcessInstanceId;
    private String linkedProcessInstanceType;

    public ProcessStartedEventImpl(ProcessInstance entity) {
        super(entity);
    }

    public void setNestedProcessDefinitionId(String nestedProcessDefinitionId) {
        this.nestedProcessDefinitionId = nestedProcessDefinitionId;
    }

    @Override
    public String getNestedProcessDefinitionId() {
        return nestedProcessDefinitionId;
    }

    public void setNestedProcessInstanceId(String nestedProcessInstanceId) {
        this.nestedProcessInstanceId = nestedProcessInstanceId;
    }

    @Override
    public String getNestedProcessInstanceId() {
        return nestedProcessInstanceId;
    }

    public void setLinkedProcessInstanceId(String linkedProcessInstanceId) {
        this.linkedProcessInstanceId = linkedProcessInstanceId;
    }

    @Override
    public String getLinkedProcessInstanceId() {
        return linkedProcessInstanceId;
    }

    public void setLinkedProcessInstanceType(String linkedProcessInstanceType) {
        this.linkedProcessInstanceType = linkedProcessInstanceType;
    }

    @Override
    public String getLinkedProcessInstanceType() {
        return linkedProcessInstanceType;
    }

    @Override
    public ProcessEvents getEventType() {
        return ProcessEvents.PROCESS_STARTED;
    }

    @Override
    public String toString() {
        return (
            "ProcessStartedEventImpl{" +
            super.toString() +
            "nestedProcessDefinitionId='" +
            nestedProcessDefinitionId +
            '\'' +
            ", nestedProcessInstanceId='" +
            nestedProcessInstanceId +
            '\'' +
            ", linkedProcessInstanceId='" +
            linkedProcessInstanceId +
            '\'' +
            ", linkedProcessInstanceType='" +
            linkedProcessInstanceType +
            '\'' +
            '}'
        );
    }
}
