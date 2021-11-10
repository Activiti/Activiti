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

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.engine.ActivitiException;
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

/**
 * Process node free jump command
 *
 *  Using rules:
 *  (1) If the process involves gateways,Nodes outside the gateway cannot jump to nodes inside the gateway.
 *  (2) If the process involves gateways,Nodes in the same execution instance within the gateway can jump to each other.
 *  (3) If the process involves gateways,Nodes outside the gateway can jump to each other.
 *  In other cases, the process does not end properly.
 *
 * @author Zhu Dunfeng
 */
public class JumpCurrNodeToTargetNodeCmd implements Command<Execution>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JumpCurrNodeToTargetNodeCmd.class);
    private static final long serialVersionUID = 1L;

    private String taskId;

    private String targetNodeId;

    public JumpCurrNodeToTargetNodeCmd(String taskId, String targetNodeId) {
        this.taskId = taskId;
        this.targetNodeId = targetNodeId;
    }

    @Override
    public Execution execute(CommandContext commandContext) {
        ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
        RepositoryService repositoryService = processEngineConfiguration.getRepositoryService();

        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        HistoryManager historyManager = commandContext.getHistoryManager();
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();

        TaskEntity currTaskEntity = taskEntityManager.findById(taskId);
        String activityId = currTaskEntity.getTaskDefinitionKey();
        ExecutionEntity execution = currTaskEntity.getExecution();
        //processInstanceId
        String parentExecutionId = execution.getProcessInstanceId();


        BpmnModel bpmnModel = repositoryService.getBpmnModel(execution.getProcessDefinitionId());

        //current node info
        FlowElement currFlowElement = bpmnModel.getFlowElement(currTaskEntity.getTaskDefinitionKey());
        Activity currActivityElement = (Activity) currFlowElement;
        MultiInstanceLoopCharacteristics currMultiInstanceLoopCharacteristics = currActivityElement.getLoopCharacteristics();


        //target node info
        FlowElement targetFlowElement = bpmnModel.getFlowElement(targetNodeId);
        Activity miActivityElement = (Activity) targetFlowElement;
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = miActivityElement.getLoopCharacteristics();

        //Determine whether the target node is multi-instance,
        // if target node is multi-instance,
        // It will advance according to the strength of its miExecution
        ExecutionEntity childExecution = null;
        if (Objects.nonNull(multiInstanceLoopCharacteristics)) {
            ExecutionEntity miExecution = searchForMultiInstanceActivity(activityId, parentExecutionId,
                    executionEntityManager);

            //if current node isn't  multi-instance
            if (miExecution == null) {
                ExecutionEntity parentExecutionEntity = executionEntityManager.findById(currTaskEntity.getProcessInstanceId());
                miExecution = executionEntityManager.createChildExecution(parentExecutionEntity);
                executionEntityManager.deleteExecutionAndRelatedData(execution,"");
            }
            childExecution = miExecution;
            childExecution.setCurrentFlowElement(miExecution.getCurrentFlowElement());


            LOGGER.info("Jump to-" + targetFlowElement.getName());
            historyManager.recordActivityEnd(execution, "Jump to-" + targetFlowElement.getName());
            historyManager.recordTaskEnd(taskId, "Jump to-" + targetFlowElement.getName());
            taskEntityManager.delete(taskId);

            if (Objects.nonNull(multiInstanceLoopCharacteristics)) {
                miExecution.setActive(true);
                miExecution.setScope(false);

                childExecution.setCurrentFlowElement(miActivityElement);
                commandContext.getAgenda().planContinueMultiInstanceOperation(childExecution);
            }
        } else {
            //Determine whether the current node is multi-instance,
            // if current node is multi-instance,
            // It will advance according to the strength of its miExecution
            if (Objects.nonNull(currMultiInstanceLoopCharacteristics)) {
                ExecutionEntity currMiExecution = searchForMultiInstanceActivity(activityId, parentExecutionId,
                        executionEntityManager);
                executionEntityManager.deleteChildExecutions(currMiExecution, "");
                execution.setCurrentFlowElement(currMiExecution.getCurrentFlowElement());
                execution = currMiExecution;
            }

            execution.setCurrentFlowElement(targetFlowElement);
            execution.setActive(true);
            execution.setScope(false);

            LOGGER.info("Jump to-" + targetFlowElement.getName());
            historyManager.recordActivityEnd(execution, "Jump to-" + targetFlowElement.getName());
            historyManager.recordTaskEnd(taskId, "Jump to-" + targetFlowElement.getName());
            taskEntityManager.delete(taskId);

            commandContext.getAgenda().planContinueProcessOperation(execution);
            childExecution = execution;
        }

        return childExecution;
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
