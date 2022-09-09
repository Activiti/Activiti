package org.activiti.engine.impl.dynamic;


import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.*;

/**
 * @author LoveMyOrange
 */
public class MoveExecutionEntityContainer {

    protected List<ExecutionEntity> executions;
    protected List<String> moveToActivityIds;
    protected boolean moveToParentProcess;
    protected boolean moveToSubProcessInstance;
    protected boolean directExecutionMigration;
    protected String callActivityId;
    protected Integer callActivitySubProcessVersion;
    protected CallActivity callActivity;
    protected String subProcessDefKey;
    protected ProcessDefinition subProcessDefinition;
    protected BpmnModel subProcessModel;
    protected BpmnModel processModel;
    protected ExecutionEntity superExecution;
    protected String newAssigneeId;
    protected Map<String, ExecutionEntity> continueParentExecutionMap = new HashMap<>();
    protected Map<String, FlowElementMoveEntry> moveToFlowElementMap = new LinkedHashMap<>();
    protected List<String> newExecutionIds = new ArrayList<>();

    public MoveExecutionEntityContainer(List<ExecutionEntity> executions, List<String> moveToActivityIds) {
        this.executions = executions;
        this.moveToActivityIds = moveToActivityIds;
    }

    public List<ExecutionEntity> getExecutions() {
        return executions;
    }

    public List<String> getMoveToActivityIds() {
        return moveToActivityIds;
    }

    public boolean isMoveToParentProcess() {
        return moveToParentProcess;
    }

    public void setMoveToParentProcess(boolean moveToParentProcess) {
        this.moveToParentProcess = moveToParentProcess;
    }

    public boolean isMoveToSubProcessInstance() {
        return moveToSubProcessInstance;
    }

    public void setMoveToSubProcessInstance(boolean moveToSubProcessInstance) {
        this.moveToSubProcessInstance = moveToSubProcessInstance;
    }

    public boolean isDirectExecutionMigration() {
        return directExecutionMigration;
    }

    public void setDirectExecutionMigration(boolean directMigrateUserTask) {
        this.directExecutionMigration = directMigrateUserTask;
    }

    public String getCallActivityId() {
        return callActivityId;
    }

    public void setCallActivityId(String callActivityId) {
        this.callActivityId = callActivityId;
    }

    public Integer getCallActivitySubProcessVersion() {
        return callActivitySubProcessVersion;
    }

    public void setCallActivitySubProcessVersion(Integer callActivitySubProcessVersion) {
        this.callActivitySubProcessVersion = callActivitySubProcessVersion;
    }

    public CallActivity getCallActivity() {
        return callActivity;
    }

    public void setCallActivity(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    public String getSubProcessDefKey() {
        return subProcessDefKey;
    }

    public void setSubProcessDefKey(String subProcessDefKey) {
        this.subProcessDefKey = subProcessDefKey;
    }

    public ProcessDefinition getSubProcessDefinition() {
        return subProcessDefinition;
    }

    public void setSubProcessDefinition(ProcessDefinition subProcessDefinition) {
        this.subProcessDefinition = subProcessDefinition;
    }

    public BpmnModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(BpmnModel processModel) {
        this.processModel = processModel;
    }

    public BpmnModel getSubProcessModel() {
        return subProcessModel;
    }

    public void setSubProcessModel(BpmnModel subProcessModel) {
        this.subProcessModel = subProcessModel;
    }

    public ExecutionEntity getSuperExecution() {
        return superExecution;
    }

    public void setNewAssigneeId(String newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }

    public String getNewAssigneeId() {
        return newAssigneeId;
    }

    public void setSuperExecution(ExecutionEntity superExecution) {
        this.superExecution = superExecution;
    }

    public void addContinueParentExecution(String executionId, ExecutionEntity continueParentExecution) {
        continueParentExecutionMap.put(executionId, continueParentExecution);
    }

    public ExecutionEntity getContinueParentExecution(String executionId) {
        return continueParentExecutionMap.get(executionId);
    }

    public void addMoveToFlowElement(String activityId, FlowElementMoveEntry flowElementMoveEntry) {
        moveToFlowElementMap.put(activityId, flowElementMoveEntry);
    }

    public void addMoveToFlowElement(String activityId, FlowElement originalFlowElement, FlowElement newFlowElement) {
        moveToFlowElementMap.put(activityId, new FlowElementMoveEntry(originalFlowElement, newFlowElement));
    }

    public void addMoveToFlowElement(String activityId, FlowElement originalFlowElement) {
        moveToFlowElementMap.put(activityId, new FlowElementMoveEntry(originalFlowElement, originalFlowElement));
    }

    public FlowElementMoveEntry getMoveToFlowElement(String activityId) {
        return moveToFlowElementMap.get(activityId);
    }

    public List<FlowElementMoveEntry> getMoveToFlowElements() {
        return new ArrayList<>(moveToFlowElementMap.values());
    }

    public List<String> getNewExecutionIds() {
        return newExecutionIds;
    }

    public boolean hasNewExecutionId(String executionId) {
        return newExecutionIds.contains(executionId);
    }

    public void setNewExecutionIds(List<String> newExecutionIds) {
        this.newExecutionIds = newExecutionIds;
    }

    public void addNewExecutionId(String executionId) {
        this.newExecutionIds.add(executionId);
    }

    public static class FlowElementMoveEntry {

        protected FlowElement originalFlowElement;
        protected FlowElement newFlowElement;

        public FlowElementMoveEntry(FlowElement originalFlowElement, FlowElement newFlowElement) {
            this.originalFlowElement = originalFlowElement;
            this.newFlowElement = newFlowElement;
        }

        public FlowElement getOriginalFlowElement() {
            return originalFlowElement;
        }

        public FlowElement getNewFlowElement() {
            return newFlowElement;
        }
    }
}

