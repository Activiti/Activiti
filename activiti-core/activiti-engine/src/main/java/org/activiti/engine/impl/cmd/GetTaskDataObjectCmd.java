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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.impl.DataObjectImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.runtime.DataObject;
import org.activiti.engine.task.Task;

public class GetTaskDataObjectCmd implements Command<DataObject>, Serializable {

    private static final long serialVersionUID = 1L;
    protected String taskId;
    protected String variableName;
    protected String locale;
    protected boolean withLocalizationFallback;

    public GetTaskDataObjectCmd(String taskId, String variableName) {
        this.taskId = taskId;
        this.variableName = variableName;
    }

    public GetTaskDataObjectCmd(
        String taskId,
        String variableName,
        String locale,
        boolean withLocalizationFallback
    ) {
        this.taskId = taskId;
        this.variableName = variableName;
        this.locale = locale;
        this.withLocalizationFallback = withLocalizationFallback;
    }

    public DataObject execute(CommandContext commandContext) {
        if (taskId == null) {
            throw new ActivitiIllegalArgumentException("taskId is null");
        }
        if (variableName == null) {
            throw new ActivitiIllegalArgumentException("variableName is null");
        }

        TaskEntity task = commandContext
            .getTaskEntityManager()
            .findById(taskId);

        if (task == null) {
            throw new ActivitiObjectNotFoundException(
                "task " + taskId + " doesn't exist",
                Task.class
            );
        }

        DataObject dataObject = null;
        VariableInstance variableEntity = task.getVariableInstance(
            variableName,
            false
        );

        String localizedName = null;
        String localizedDescription = null;

        if (variableEntity != null) {
            ExecutionEntity executionEntity = commandContext
                .getExecutionEntityManager()
                .findById(variableEntity.getExecutionId());
            while (!executionEntity.isScope()) {
                executionEntity = executionEntity.getParent();
            }

            BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(
                executionEntity.getProcessDefinitionId()
            );
            ValuedDataObject foundDataObject = null;
            if (executionEntity.getParentId() == null) {
                for (ValuedDataObject dataObjectDefinition : bpmnModel
                    .getMainProcess()
                    .getDataObjects()) {
                    if (
                        dataObjectDefinition
                            .getName()
                            .equals(variableEntity.getName())
                    ) {
                        foundDataObject = dataObjectDefinition;
                        break;
                    }
                }
            } else {
                SubProcess subProcess = (SubProcess) bpmnModel.getFlowElement(
                    executionEntity.getActivityId()
                );
                for (ValuedDataObject dataObjectDefinition : subProcess.getDataObjects()) {
                    if (
                        dataObjectDefinition
                            .getName()
                            .equals(variableEntity.getName())
                    ) {
                        foundDataObject = dataObjectDefinition;
                        break;
                    }
                }
            }

            if (locale != null && foundDataObject != null) {
                ObjectNode languageNode = Context.getLocalizationElementProperties(
                    locale,
                    foundDataObject.getId(),
                    task.getProcessDefinitionId(),
                    withLocalizationFallback
                );

                if (languageNode != null) {
                    JsonNode nameNode = languageNode.get(
                        DynamicBpmnConstants.LOCALIZATION_NAME
                    );
                    if (nameNode != null) {
                        localizedName = nameNode.asText();
                    }
                    JsonNode descriptionNode = languageNode.get(
                        DynamicBpmnConstants.LOCALIZATION_DESCRIPTION
                    );
                    if (descriptionNode != null) {
                        localizedDescription = descriptionNode.asText();
                    }
                }
            }

            if (foundDataObject != null) {
                dataObject =
                    new DataObjectImpl(
                        variableEntity.getName(),
                        variableEntity.getValue(),
                        foundDataObject.getDocumentation(),
                        foundDataObject.getType(),
                        localizedName,
                        localizedDescription,
                        foundDataObject.getId()
                    );
            }
        }

        return dataObject;
    }
}
