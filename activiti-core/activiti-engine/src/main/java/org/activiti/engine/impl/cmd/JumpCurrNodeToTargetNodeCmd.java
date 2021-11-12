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
package org.activiti.engine.impl.cmd;

import org.activiti.bpmn.model.*;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Zhu Dunfeng
 * @description Process node free jump command
 * <p>
 * Switch from a common task node to a common task node.
 * The common task node jumps to the multi-instance task node.
 * The multi-instance task node jumps to the multi-instance task node.
 * The multi-instance task node jumps to a common task node
 * Rules about gateway usage:
 * (1) If the process involves gateways,Nodes outside the gateway cannot jump to nodes inside the gateway.
 * (2) If the process involves gateways,Nodes in the same execution instance within the gateway can jump to each other.
 * (3) If the process involves gateways,Nodes outside the gateway can jump to each other.
 * In other cases, the process does not end properly.
 */
public class JumpCurrNodeToTargetNodeCmd implements Command<Execution>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JumpCurrNodeToTargetNodeCmd.class);
    private static final long serialVersionUID = 1L;

    String processInstanceId;

    private String nodeId;

    private String targetNodeId;

    public JumpCurrNodeToTargetNodeCmd(String processInstanceId, String nodeId, String targetNodeId) {
        this.processInstanceId = processInstanceId;
        this.nodeId = nodeId;
        this.targetNodeId = targetNodeId;
    }

    @Override
    public Execution execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
        RepositoryService repositoryService = processEngineConfiguration.getRepositoryService();

        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        HistoryManager historyManager = commandContext.getHistoryManager();
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

        List<TaskEntity> taskEntityList = taskEntityManager.findTasksByProcessInstanceId(processInstanceId);
        taskEntityList = taskEntityList.stream().filter(taskEntity -> nodeId.equals(taskEntity.getTaskDefinitionKey())).collect(Collectors.toList());
        TaskEntity currTaskEntity = null;
        if (taskEntityList != null && taskEntityList.size() > 0) {
            currTaskEntity = taskEntityList.get(0);
        } else {
            throw new ActivitiException("The current node has no task and cannot be jumped");
        }
        String activityId = currTaskEntity.getTaskDefinitionKey();
        ExecutionEntity execution = currTaskEntity.getExecution();
        //processInstanceId
        String parentExecutionId = execution.getProcessInstanceId();
        ExecutionEntity parentExecutionEntity = executionEntityManager.findById(processInstanceId);

        BpmnModel bpmnModel = repositoryService.getBpmnModel(execution.getProcessDefinitionId());

        //The current node info.
        FlowElement currFlowElement = bpmnModel.getFlowElement(nodeId);
        Activity currActivityElement = (Activity) currFlowElement;
        MultiInstanceLoopCharacteristics currMultiInstanceLoopCharacteristics = currActivityElement.getLoopCharacteristics();
        SubProcess currSubProcess = currActivityElement.getSubProcess();

        //The target node info.
        FlowElement targetFlowElement = bpmnModel.getFlowElement(targetNodeId);
        Activity miActivityElement = (Activity) targetFlowElement;
        MultiInstanceLoopCharacteristics targetMultiInstanceLoopCharacteristics = miActivityElement.getLoopCharacteristics();
        SubProcess targetSubProcess = miActivityElement.getSubProcess();

        //Determine whether the target node is multi-instance,
        // if target node is multi-instance,
        // It will proceed according to its miExecution.
        if (Objects.nonNull(targetMultiInstanceLoopCharacteristics)) {
            ExecutionEntity miExecution = searchForMultiInstanceActivity(activityId, parentExecutionId,
                executionEntityManager);

            //If the current node is not multi-instance and the jump node is multi-instance.
            //We need to create a child root execution instance for the target node.
            if (miExecution == null) {
                miExecution = executionEntityManager.createChildExecution(parentExecutionEntity);
                executionEntityManager.deleteExecutionAndRelatedData(execution, "");
            }
            ExecutionEntity childExecution = miExecution;

            LOGGER.info(currFlowElement.getName() + "-Jump to-" + targetFlowElement.getName());
            historyManager.recordActivityEnd(execution, "Jump to-" + targetFlowElement.getName());
            for (TaskEntity taskEntity : taskEntityList) {
                historyManager.recordTaskEnd(taskEntity.getId(), "Jump to-" + targetFlowElement.getName());
                taskEntityManager.delete(taskEntity);
            }

            miExecution.setActive(true);
            miExecution.setScope(false);
            childExecution.setCurrentFlowElement(miActivityElement);
            commandContext.getAgenda().planContinueMultiInstanceOperation(childExecution);
            return childExecution;
        } else {
            //Determine whether the current node is multi-instance,
            // if current node is multi-instance,
            // It will proceed according to its miExecution.
            if (Objects.nonNull(currMultiInstanceLoopCharacteristics)) {
                ExecutionEntity currMiExecution = searchForMultiInstanceActivity(activityId, parentExecutionId,
                    executionEntityManager);
                executionEntityManager.deleteChildExecutions(currMiExecution, "");
                execution = currMiExecution;
            }

            execution.setCurrentFlowElement(targetFlowElement);
            execution.setActive(true);
            execution.setScope(false);
            //If the current node is a normal node of the subProcess, the target node is a normal node.
            if (currSubProcess != null && targetSubProcess == null) {
                ExecutionEntity currSubProcessExecution = execution.getParent();
                executionEntityManager.deleteChildExecutions(currSubProcessExecution, "");
                currSubProcessExecution.setCurrentFlowElement(targetFlowElement);
                execution = currSubProcessExecution;
            }

            //If the current node is a normal node, the target is a normal node in the subProcess.
            if (currSubProcess == null && targetSubProcess != null) {
                ExecutionEntity childExecution = executionEntityManager.createChildExecution(execution);
                FlowElement flowElement = targetSubProcess.getFlowElement(targetNodeId);
                UserTask userTask = ((UserTask) flowElement);
                userTask.setExtensionId(childExecution.getId());
                userTask.setParentContainer(targetSubProcess);
                childExecution.setCurrentFlowElement(userTask);
                execution.setCurrentFlowElement(targetSubProcess);
                childExecution.setParent(execution);
                execution = childExecution;
            }

            LOGGER.info(currFlowElement.getName() + "-Jump to-" + targetFlowElement.getName());
            historyManager.recordActivityEnd(execution, "Jump to-" + targetFlowElement.getName());
            for (TaskEntity taskEntity : taskEntityList) {
                historyManager.recordTaskEnd(taskEntity.getId(), "Jump to-" + targetFlowElement.getName());
                taskEntityManager.delete(taskEntity);
            }

            commandContext.getAgenda().planContinueProcessOperation(execution);

            return execution;
        }
    }


    protected ExecutionEntity searchForMultiInstanceActivity(String activityId, String parentExecutionId,
                                                             ExecutionEntityManager executionEntityManager) {
        // Find all child instances of the current parent execution instance
        List<ExecutionEntity> childExecutions = executionEntityManager
            .findChildExecutionsByParentExecutionId(parentExecutionId);

        ExecutionEntity miExecution = null;
        for (ExecutionEntity childExecution : childExecutions) {
            // Gets the miExecution of the child execution instance based on the current active ID
            if (activityId.equals(childExecution.getActivityId()) && childExecution.isMultiInstanceRoot()) {
                if (miExecution != null) {
                    throw new ActivitiException(
                        "Multiple multi instance executions found for activity id " + activityId);
                }
                miExecution = childExecution;
            }
            // Recursive search
            ExecutionEntity childMiExecution = searchForMultiInstanceActivity(activityId, childExecution.getId(),
                executionEntityManager);
            if (childMiExecution != null) {
                if (miExecution != null) {
                    throw new ActivitiException(
                        "Multiple multi instance executions found for activity id " + activityId);
                }
                miExecution = childMiExecution;
            }
        }

        return miExecution;
    }
}
