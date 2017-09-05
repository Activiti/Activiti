/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.audit.events;

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
import org.activiti.services.api.events.ProcessEngineEvent;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActivityStartedEventEntity.class, name = ActivityStartedEventEntity.ACTIVITY_STARTED_EVENT),
        @JsonSubTypes.Type(value = ActivityCompletedEventEntity.class, name = ActivityCompletedEventEntity.ACTIVITY_COMPLETED_EVENT),
        @JsonSubTypes.Type(value = ActivityCancelledEventEntity.class, name = ActivityCancelledEventEntity.ACTIVITY_CANCELLED_EVENT),
        @JsonSubTypes.Type(value = ProcessStartedEventEntity.class, name = ProcessStartedEventEntity.PROCESS_STARTED_EVENT),
        @JsonSubTypes.Type(value = ProcessCompletedEventEntity.class, name = ProcessCompletedEventEntity.PROCESS_COMPLETED_EVENT),
        @JsonSubTypes.Type(value = ProcessCancelledEventEntity.class, name = ProcessCancelledEventEntity.PROCESS_CANCELLED_EVENT),
        @JsonSubTypes.Type(value = TaskCreatedEventEntity.class, name = TaskCreatedEventEntity.TASK_CREATED_EVENT),
        @JsonSubTypes.Type(value = TaskAssignedEventEntity.class, name = TaskAssignedEventEntity.TASK_ASSIGNED_EVENT),
        @JsonSubTypes.Type(value = TaskCompletedEventEntity.class, name = TaskCompletedEventEntity.TASK_COMPLETED_EVENT),
        @JsonSubTypes.Type(value = VariableCreatedEventEntity.class, name = VariableCreatedEventEntity.VARIABLE_CREATED_EVENT),
        @JsonSubTypes.Type(value = VariableUpdatedEventEntity.class, name = VariableUpdatedEventEntity.VARIABLE_UPDATED_EVENT),
        @JsonSubTypes.Type(value = VariableDeletedEventEntity.class, name = VariableDeletedEventEntity.VARIABLE_DELETED_EVENT),
        @JsonSubTypes.Type(value = SequenceFlowTakenEventEntity.class, name = SequenceFlowTakenEventEntity.SEQUENCE_FLOW_TAKEN_EVENT)
})
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
public abstract class ProcessEngineEventEntity implements ProcessEngineEvent {

    @Id
    @GeneratedValue
    private Long id;

    private Long timestamp;
    private String eventType;
    private String executionId;
    private String processDefinitionId;
    private String processInstanceId;
    private String applicationName;

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }
}
