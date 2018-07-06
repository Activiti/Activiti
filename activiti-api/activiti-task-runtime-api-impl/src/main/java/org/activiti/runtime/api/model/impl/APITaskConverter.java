/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model.impl;

import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.runtime.api.model.FluentTask;

public class APITaskConverter extends ListConverter<org.activiti.engine.task.Task, FluentTask> implements ModelConverter<org.activiti.engine.task.Task, FluentTask> {

    private final TaskService taskService;
    private final APIVariableInstanceConverter variableInstanceConverter;

    public APITaskConverter(TaskService taskService,
                            APIVariableInstanceConverter variableInstanceConverter) {
        this.taskService = taskService;
        this.variableInstanceConverter = variableInstanceConverter;
    }

    @Override
    public FluentTask from(org.activiti.engine.task.Task internalTask) {
        FluentTaskImpl task = new FluentTaskImpl(taskService,
                                                 variableInstanceConverter,
                                                 this,
                                                 internalTask.getId(),
                                                 internalTask.getName(),
                                                 calculateStatus(internalTask)
        );
        task.setProcessDefinitionId(internalTask.getProcessDefinitionId());
        task.setProcessInstanceId(internalTask.getProcessInstanceId());
        task.setAssignee(internalTask.getAssignee());
        task.setClaimedDate(internalTask.getClaimTime());
        task.setCreatedDate(internalTask.getCreateTime());
        task.setDueDate(internalTask.getDueDate());
        task.setDescription(internalTask.getDescription());
        task.setOwner(internalTask.getOwner());
        task.setParentTaskId(internalTask.getParentTaskId());
        task.setPriority(internalTask.getPriority());
        return task;
    }

    private FluentTask.TaskStatus calculateStatus(org.activiti.engine.task.Task source) {
        if (source instanceof TaskEntity &&
                (((TaskEntity) source).isDeleted() || ((TaskEntity) source).isCanceled())) {
            return FluentTask.TaskStatus.CANCELLED;
        } else if (source.isSuspended()) {
            return FluentTask.TaskStatus.SUSPENDED;
        } else if (source.getAssignee() != null && !source.getAssignee().isEmpty()) {
            return FluentTask.TaskStatus.ASSIGNED;
        }
        return FluentTask.TaskStatus.CREATED;
    }

}
