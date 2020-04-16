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

import static java.util.Collections.emptyList;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class APITaskConverter extends ListConverter<org.activiti.engine.task.Task, Task> implements ModelConverter<org.activiti.engine.task.Task, Task> {

    private final TaskService taskService;

    @Autowired
    public APITaskConverter(TaskService taskService){
        this.taskService = taskService;
    }

    @Override
    public Task from(org.activiti.engine.task.Task internalTask) {
        return from(internalTask,
                    calculateStatus(internalTask));
    }

    public Task fromWithCandidates(org.activiti.engine.task.Task internalTask) {
        TaskImpl task = (TaskImpl) from(internalTask,
                                        calculateStatus(internalTask));
        extractCandidateUsersAndGroups(internalTask, task);
        return task;
    }

    public Task from(org.activiti.engine.task.Task internalTask,
                     Task.TaskStatus status) {
        TaskImpl task = new TaskImpl(internalTask.getId(),
                                     internalTask.getName(),
                                     status);
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
        task.setFormKey(internalTask.getFormKey());
        task.setTaskDefinitionKey(internalTask.getTaskDefinitionKey());
        task.setAppVersion(Objects.toString(internalTask.getAppVersion(), null));
        task.setBusinessKey(internalTask.getBusinessKey());

        return task;
    }

    private void extractCandidateUsersAndGroups(org.activiti.engine.task.Task source, TaskImpl destination) {
        List<IdentityLink> candidates = taskService.getIdentityLinksForTask(source.getId());
        destination.setCandidateGroups(extractCandidatesBy(candidates, IdentityLink::getGroupId));
        destination.setCandidateUsers(extractCandidatesBy(candidates, IdentityLink::getUserId));
    }

    private List<String> extractCandidatesBy(List<IdentityLink> candidates, Function<IdentityLink, String> extractor) {
        List<String> result = emptyList();
        if (candidates != null) {
            result = candidates
                             .stream()
                             .filter(candidate -> IdentityLinkType.CANDIDATE.equals(candidate.getType()))
                             .map(extractor::apply)
                             .filter(Objects::nonNull)
                             .collect(Collectors.toList());
        }
        return result;
    }

    private Task.TaskStatus calculateStatus(org.activiti.engine.task.Task source) {
        if (source instanceof TaskEntity &&
            (((TaskEntity) source).isDeleted() || ((TaskEntity) source).isCanceled())) {
            return Task.TaskStatus.CANCELLED;
        } else if (source.isSuspended()) {
            return Task.TaskStatus.SUSPENDED;
        } else if (source.getAssignee() != null && !source.getAssignee().isEmpty()) {
            return Task.TaskStatus.ASSIGNED;
        }
        return Task.TaskStatus.CREATED;
    }
}
