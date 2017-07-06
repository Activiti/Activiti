/*
 * Copyright 2017 Alfresco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.query.app.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActivityStartedEvent.class, name = "ActivityStartedEvent"),
        @JsonSubTypes.Type(value = ProcessStartedEvent.class, name = "ProcessStartedEvent"),
        @JsonSubTypes.Type(value = TaskCreatedEvent.class, name = "TaskCreatedEvent"),
        @JsonSubTypes.Type(value = VariableCreatedEvent.class, name = "VariableCreatedEvent")
})
public abstract class ProcessEngineEvent {

    private Long timestamp;
    private String eventType;
    private String executionId;
    private String processDefinitionId;
    private String processInstanceId;

    public ProcessEngineEvent() {
    }

    public ProcessEngineEvent(Long timestamp,
                              String eventType,
                              String executionId,
                              String processDefinitionId,
                              String processInstanceId) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.executionId = executionId;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
    }


    public Long getTimestamp() {
        return timestamp;
    }


    public String getEventType() {
        return eventType;
    }


    public String getExecutionId() {
        return executionId;
    }


    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}
