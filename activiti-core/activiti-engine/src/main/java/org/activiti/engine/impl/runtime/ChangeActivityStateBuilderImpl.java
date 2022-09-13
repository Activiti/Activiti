package org.activiti.engine.impl.runtime;


import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.cmd.ChangeActivityStateCmd;
import org.activiti.engine.runtime.ChangeActivityStateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChangeActivityStateBuilderImpl implements ChangeActivityStateBuilder {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected RuntimeServiceImpl runtimeService;

    protected String processInstanceId;
    protected List<MoveExecutionIdContainer> moveExecutionIdList = new ArrayList<>();
    protected List<MoveActivityIdContainer> moveActivityIdList = new ArrayList<>();
    protected Map<String, Object> processVariables = new HashMap<>();
    protected Map<String, Map<String, Object>> localVariables = new HashMap<>();
    protected String jumpReason;

    public ChangeActivityStateBuilderImpl() {
    }

    public ChangeActivityStateBuilderImpl(RuntimeServiceImpl runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public ChangeActivityStateBuilder processInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveExecutionToActivityId(String executionId, String activityId) {
        return moveExecutionToActivityId(executionId, activityId, null);
    }

    public ChangeActivityStateBuilder moveExecutionToActivityId(String executionId, String activityId, String newAssigneeId) {
        moveExecutionIdList.add(new MoveExecutionIdContainer(executionId, activityId, newAssigneeId));
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveExecutionsToSingleActivityId(List<String> executionIds, String activityId) {
        return moveExecutionsToSingleActivityId(executionIds, activityId, null);
    }

    public ChangeActivityStateBuilder moveExecutionsToSingleActivityId(List<String> executionIds, String activityId, String newAssigneeId) {
        moveExecutionIdList.add(new MoveExecutionIdContainer(executionIds, activityId, newAssigneeId));
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveSingleExecutionToActivityIds(String executionId, List<String> activityIds) {
        return moveSingleExecutionToActivityIds(executionId, activityIds, null);
    }

    public ChangeActivityStateBuilder moveSingleExecutionToActivityIds(String executionId, List<String> activityIds, String newAssigneeId) {
        moveExecutionIdList.add(new MoveExecutionIdContainer(executionId, activityIds, newAssigneeId));
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveActivityIdTo(String currentActivityId, String newActivityId) {
        return moveActivityIdTo(currentActivityId, newActivityId, null);
    }

    public ChangeActivityStateBuilder moveActivityIdTo(String currentActivityId, String newActivityId, String newAssigneeId) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Change activity processInstanceId:[{}]  from: [{}] to: [{}] assigneeId: [{}], start", processInstanceId, currentActivityId, newActivityId, newAssigneeId);
        }
        moveActivityIdList.add(new MoveActivityIdContainer(currentActivityId, newActivityId, newAssigneeId));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Change activity processInstanceId:[{}] from: [{}] to: [{}] assigneeId: [{}], end", processInstanceId, currentActivityId, newActivityId, newAssigneeId);
        }
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveActivityIdsToSingleActivityId(List<String> activityIds, String activityId) {
        return moveActivityIdsToSingleActivityId(activityIds, activityId, null);
    }

    public ChangeActivityStateBuilder moveActivityIdsToSingleActivityId(List<String> activityIds, String activityId, String newAssigneeId) {
        moveActivityIdList.add(new MoveActivityIdContainer(activityIds, activityId, newAssigneeId));
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveSingleActivityIdToActivityIds(String currentActivityId, List<String> newActivityIds) {
        return moveSingleActivityIdToActivityIds(currentActivityId, newActivityIds, null);
    }

    public ChangeActivityStateBuilder moveSingleActivityIdToActivityIds(String currentActivityId, List<String> newActivityIds, String newAssigneeId) {
        moveActivityIdList.add(new MoveActivityIdContainer(currentActivityId, newActivityIds, newAssigneeId));
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveActivityIdToParentActivityId(String currentActivityId, String newActivityId) {
        return moveActivityIdToParentActivityId(currentActivityId, newActivityId, null);
    }

    public ChangeActivityStateBuilder moveActivityIdToParentActivityId(String currentActivityId, String newActivityId, String newAssigneeId) {
        MoveActivityIdContainer moveActivityIdContainer = new MoveActivityIdContainer(currentActivityId, newActivityId, newAssigneeId);
        moveActivityIdContainer.setMoveToParentProcess(true);
        moveActivityIdList.add(moveActivityIdContainer);
        return this;
    }

    public ChangeActivityStateBuilder moveActivityIdsToParentActivityId(List<String> currentActivityIds, String newActivityId, String newAssigneeId) {
        MoveActivityIdContainer moveActivityIdContainer = new MoveActivityIdContainer(currentActivityIds, newActivityId, newAssigneeId);
        moveActivityIdContainer.setMoveToParentProcess(true);
        moveActivityIdList.add(moveActivityIdContainer);
        return this;
    }

    public ChangeActivityStateBuilder moveSingleActivityIdToParentActivityIds(String currentActivityId, List<String> newActivityIds) {
        MoveActivityIdContainer moveActivityIdContainer = new MoveActivityIdContainer(currentActivityId, newActivityIds);
        moveActivityIdContainer.setMoveToParentProcess(true);
        moveActivityIdList.add(moveActivityIdContainer);
        return this;
    }

    @Override
    public ChangeActivityStateBuilder moveActivityIdToSubProcessInstanceActivityId(String currentActivityId, String newActivityId, String callActivityId) {
        return moveActivityIdToSubProcessInstanceActivityId(currentActivityId, newActivityId, callActivityId, null, null);
    }

    @Override
    public ChangeActivityStateBuilder moveActivityIdToSubProcessInstanceActivityId(String currentActivityId, String newActivityId, String callActivityId, Integer subProcessDefinitionVersion) {
        return moveActivityIdToSubProcessInstanceActivityId(currentActivityId, newActivityId, callActivityId, subProcessDefinitionVersion, null);
    }

    public ChangeActivityStateBuilder moveActivityIdToSubProcessInstanceActivityId(String currentActivityId, String newActivityId, String callActivityId, Integer callActivitySubProcessVersion, String newAssigneeId) {
        MoveActivityIdContainer moveActivityIdContainer = new MoveActivityIdContainer(currentActivityId, newActivityId, newAssigneeId);
        moveActivityIdContainer.setMoveToSubProcessInstance(true);
        moveActivityIdContainer.setCallActivityId(callActivityId);
        moveActivityIdContainer.setCallActivitySubProcessVersion(callActivitySubProcessVersion);
        moveActivityIdList.add(moveActivityIdContainer);
        return this;
    }

    public ChangeActivityStateBuilder moveActivityIdsToSubProcessInstanceActivityId(List<String> activityIds, String newActivityId, String callActivityId, Integer callActivitySubProcessVersion, String newAssigneeId) {
        MoveActivityIdContainer moveActivityIdsContainer = new MoveActivityIdContainer(activityIds, newActivityId, newAssigneeId);
        moveActivityIdsContainer.setMoveToSubProcessInstance(true);
        moveActivityIdsContainer.setCallActivityId(callActivityId);
        moveActivityIdsContainer.setCallActivitySubProcessVersion(callActivitySubProcessVersion);
        moveActivityIdList.add(moveActivityIdsContainer);
        return this;
    }

    public ChangeActivityStateBuilder moveSingleActivityIdToSubProcessInstanceActivityIds(String currentActivityId, List<String> newActivityIds, String callActivityId, Integer callActivitySubProcessVersion) {
        MoveActivityIdContainer moveActivityIdsContainer = new MoveActivityIdContainer(currentActivityId, newActivityIds);
        moveActivityIdsContainer.setMoveToSubProcessInstance(true);
        moveActivityIdsContainer.setCallActivityId(callActivityId);
        moveActivityIdsContainer.setCallActivitySubProcessVersion(callActivitySubProcessVersion);
        moveActivityIdList.add(moveActivityIdsContainer);
        return this;
    }

    @Override
    public ChangeActivityStateBuilder processVariable(String processVariableName, Object processVariableValue) {
        if (this.processVariables == null) {
            this.processVariables = new HashMap<>();
        }

        this.processVariables.put(processVariableName, processVariableValue);
        return this;
    }

    @Override
    public ChangeActivityStateBuilder processVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
        return this;
    }

    @Override
    public ChangeActivityStateBuilder localVariable(String startActivityId, String localVariableName, Object localVariableValue) {
        if (this.localVariables == null) {
            this.localVariables = new HashMap<>();
        }

        Map<String, Object> localVariableMap = null;
        if (localVariables.containsKey(startActivityId)) {
            localVariableMap = localVariables.get(startActivityId);
        } else {
            localVariableMap = new HashMap<>();
        }

        localVariableMap.put(localVariableName, localVariableValue);

        this.localVariables.put(startActivityId, localVariableMap);

        return this;
    }

    @Override
    public ChangeActivityStateBuilder localVariables(String startActivityId, Map<String, Object> localVariables) {
        if (this.localVariables == null) {
            this.localVariables = new HashMap<>();
        }

        this.localVariables.put(startActivityId, localVariables);

        return this;
    }

    @Override
    public ChangeActivityStateBuilder jumpReason(String jumpReason) {
        this.jumpReason = jumpReason;
        return this;
    }

    @Override
    public void changeState() {
        if (runtimeService == null) {
            throw new ActivitiException("RuntimeService cannot be null, Obtain your builder instance from the RuntimeService to access this feature");
        }

        changeActivityState(this);
    }

    public void changeActivityState(ChangeActivityStateBuilderImpl changeActivityStateBuilder) {
        runtimeService.getCommandExecutor().execute(new ChangeActivityStateCmd(changeActivityStateBuilder));
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public List<MoveExecutionIdContainer> getMoveExecutionIdList() {
        return moveExecutionIdList;
    }

    public List<MoveActivityIdContainer> getMoveActivityIdList() {
        return moveActivityIdList;
    }

    public Map<String, Object> getProcessInstanceVariables() {
        return processVariables;
    }

    public Map<String, Map<String, Object>> getLocalVariables() {
        return localVariables;
    }

    public String getJumpReason() {
        return jumpReason;
    }

    public void setJumpReason(String jumpReason) {
        this.jumpReason = jumpReason;
    }
}
