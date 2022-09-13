package org.activiti.engine.impl.dynamic;


import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ProcessInstanceChangeState {

    protected String processInstanceId;
    protected ProcessDefinition processDefinitionToMigrateTo;
    protected Map<String, Object> processVariables = new HashMap<>();
    protected Map<String, Map<String, Object>> localVariables = new HashMap<>();
    protected Map<String, List<ExecutionEntity>> processInstanceActiveEmbeddedExecutions;
    protected List<MoveExecutionEntityContainer> moveExecutionEntityContainers;
    protected HashMap<String, ExecutionEntity> createdEmbeddedSubProcess = new HashMap<>();
    protected HashMap<StartEvent, ExecutionEntity> pendingEventSubProcessesStartEvents = new HashMap<>();
    protected String jumpReason;


    public ProcessInstanceChangeState() {
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getJumpReason() {
        return jumpReason;
    }

    public ProcessInstanceChangeState setJumpReason(String jumpReason) {
        this.jumpReason = jumpReason;
        return this;
    }

    public ProcessInstanceChangeState setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public Optional<ProcessDefinition> getProcessDefinitionToMigrateTo() {
        return Optional.ofNullable(processDefinitionToMigrateTo);
    }

    public ProcessInstanceChangeState setProcessDefinitionToMigrateTo(ProcessDefinition processDefinitionToMigrateTo) {
        this.processDefinitionToMigrateTo = processDefinitionToMigrateTo;
        return this;
    }

    public boolean isMigrateToProcessDefinition() {
        return getProcessDefinitionToMigrateTo().isPresent();
    }

    public Map<String, Object> getProcessInstanceVariables() {
        return processVariables;
    }

    public ProcessInstanceChangeState setProcessInstanceVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
        return this;
    }

    public Map<String, Map<String, Object>> getLocalVariables() {
        return localVariables;
    }

    public ProcessInstanceChangeState setLocalVariables(Map<String, Map<String, Object>> localVariables) {
        this.localVariables = localVariables;
        return this;
    }

    public List<MoveExecutionEntityContainer> getMoveExecutionEntityContainers() {
        return moveExecutionEntityContainers;
    }

    public ProcessInstanceChangeState setMoveExecutionEntityContainers(List<MoveExecutionEntityContainer> moveExecutionEntityContainers) {
        this.moveExecutionEntityContainers = moveExecutionEntityContainers;
        return this;
    }

    public HashMap<String, ExecutionEntity> getCreatedEmbeddedSubProcesses() {
        return createdEmbeddedSubProcess;
    }

    public Optional<ExecutionEntity> getCreatedEmbeddedSubProcessByKey(String key) {
        return Optional.ofNullable(createdEmbeddedSubProcess.get(key));
    }

    public void addCreatedEmbeddedSubProcess(String key, ExecutionEntity executionEntity) {
        this.createdEmbeddedSubProcess.put(key, executionEntity);
    }

    public Map<String, List<ExecutionEntity>> getProcessInstanceActiveEmbeddedExecutions() {
        return processInstanceActiveEmbeddedExecutions;
    }

    public ProcessInstanceChangeState setProcessInstanceActiveEmbeddedExecutions(Map<String, List<ExecutionEntity>> processInstanceActiveEmbeddedExecutions) {
        this.processInstanceActiveEmbeddedExecutions = processInstanceActiveEmbeddedExecutions;
        return this;
    }

    public HashMap<StartEvent, ExecutionEntity> getPendingEventSubProcessesStartEvents() {
        return pendingEventSubProcessesStartEvents;
    }

    public void addPendingEventSubProcessStartEvent(StartEvent startEvent, ExecutionEntity eventSubProcessParent) {
        this.pendingEventSubProcessesStartEvents.put(startEvent, eventSubProcessParent);
    }

}

