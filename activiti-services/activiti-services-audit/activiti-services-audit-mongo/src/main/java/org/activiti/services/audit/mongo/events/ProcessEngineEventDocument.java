package org.activiti.services.audit.mongo.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.querydsl.core.annotations.QueryEntity;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActivityStartedEventDocument.class, name = ActivityStartedEventDocument.ACTIVITY_STARTED_EVENT),
        @JsonSubTypes.Type(value = ActivityCompletedEventDocument.class, name = ActivityCompletedEventDocument.ACTIVITY_COMPLETED_EVENT),
        @JsonSubTypes.Type(value = ActivityCancelledEventDocument.class, name = ActivityCancelledEventDocument.ACTIVITY_CANCELLED_EVENT),
        @JsonSubTypes.Type(value = ProcessStartedEventDocument.class, name = ProcessStartedEventDocument.PROCESS_STARTED_EVENT),
        @JsonSubTypes.Type(value = ProcessCompletedEventDocument.class, name = ProcessCompletedEventDocument.PROCESS_COMPLETED_EVENT),
        @JsonSubTypes.Type(value = ProcessCancelledEventDocument.class, name = ProcessCancelledEventDocument.PROCESS_CANCELLED_EVENT),
        @JsonSubTypes.Type(value = TaskCreatedEventDocument.class, name = TaskCreatedEventDocument.TASK_CREATED_EVENT),
        @JsonSubTypes.Type(value = TaskAssignedEventDocument.class, name = TaskAssignedEventDocument.TASK_ASSIGNED_EVENT),
        @JsonSubTypes.Type(value = TaskCompletedEventDocument.class, name = TaskCompletedEventDocument.TASK_COMPLETED_EVENT),
        @JsonSubTypes.Type(value = VariableCreatedEventDocument.class, name = VariableCreatedEventDocument.VARIABLE_CREATED_EVENT),
        @JsonSubTypes.Type(value = VariableUpdatedEventDocument.class, name = VariableUpdatedEventDocument.VARIABLE_UPDATED_EVENT),
        @JsonSubTypes.Type(value = VariableDeletedEventDocument.class, name = VariableDeletedEventDocument.VARIABLE_DELETED_EVENT),
        @JsonSubTypes.Type(value = SequenceFlowTakenEventDocument.class, name = SequenceFlowTakenEventDocument.SEQUENCE_FLOW_TAKEN_EVENT)
})
@QueryEntity
@Document(collection = "act_evt_log")
public class ProcessEngineEventDocument implements ProcessEngineEvent {

    @Id
    private String id;

    private Long timestamp;
    private String eventType;
    private String executionId;
    private String processDefinitionId;
    private String processInstanceId;
    private String applicationName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
