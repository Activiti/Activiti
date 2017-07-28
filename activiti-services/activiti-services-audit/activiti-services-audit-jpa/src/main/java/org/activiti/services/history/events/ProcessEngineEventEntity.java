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

package org.activiti.services.history.events;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActivityStartedEventEntity.class, name = "ActivityStartedEvent"),
        @JsonSubTypes.Type(value = ActivityCompletedEventEntity.class, name = "ActivityCompletedEvent"),
        @JsonSubTypes.Type(value = ProcessStartedEventEntity.class, name = "ProcessStartedEvent"),
        @JsonSubTypes.Type(value = ProcessCompletedEventEntity.class, name = "ProcessCompletedEvent"),
        @JsonSubTypes.Type(value = TaskCreatedEventEntity.class, name = "TaskCreatedEvent"),
        @JsonSubTypes.Type(value = TaskAssignedEventEntity.class, name = "TaskAssignedEvent"),
        @JsonSubTypes.Type(value = TaskCompletedEventEntity.class, name = "TaskCompletedEvent"),
        @JsonSubTypes.Type(value = VariableCreatedEventEntity.class, name = "VariableCreatedEvent"),
        @JsonSubTypes.Type(value = VariableUpdatedEventEntity.class, name = "VariableUpdatedEvent"),
        @JsonSubTypes.Type(value = VariableDeletedEventEntity.class, name = "VariableDeletedEvent"),
        @JsonSubTypes.Type(value = SequenceFlowTakenEventEntity.class, name = "SequenceFlowTakenEvent")
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
public abstract class ProcessEngineEventEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long timestamp;
    private String eventType;
    private String executionId;
    private String processDefinitionId;
    private String processInstanceId;

    public ProcessEngineEventEntity() {
    }

    public ProcessEngineEventEntity(Long id, Long timestamp,
                                    String eventType,
                                    String executionId,
                                    String processDefinitionId,
                                    String processInstanceId) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

}
