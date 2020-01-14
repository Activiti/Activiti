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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;

/**
 * Mock task builder.
 */
public class MockTaskBuilder {

    private Task task;

    private MockTaskBuilder(Class<? extends Task> taskClass) {
        task = mock(taskClass);
    }

    public static MockTaskBuilder taskBuilder() {
        return new MockTaskBuilder(Task.class);
    }

    public static MockTaskBuilder taskEntityBuilder() {
        return new MockTaskBuilder(TaskEntity.class);
    }

    public MockTaskBuilder withId(String id) {
        when(task.getId()).thenReturn(id);
        return this;
    }

    public MockTaskBuilder withOwner(String owner) {
        when(task.getOwner()).thenReturn(owner);
        return this;
    }

    public MockTaskBuilder withAssignee(String assignee) {
        when(task.getAssignee()).thenReturn(assignee);
        return this;
    }

    public MockTaskBuilder withName(String name) {
        when(task.getName()).thenReturn(name);
        return this;
    }

    public MockTaskBuilder withDescription(String description) {
        when(task.getDescription()).thenReturn(description);
        return this;
    }

    public MockTaskBuilder withCreatedDate(Date createdDate) {
        when(task.getCreateTime()).thenReturn(createdDate);
        return this;
    }

    public MockTaskBuilder withClaimedDate(Date claimedDate) {
        when(task.getClaimTime()).thenReturn(claimedDate);
        return this;
    }

    public MockTaskBuilder withDueDate(Date dueDate) {
        when(task.getDueDate()).thenReturn(dueDate);
        return this;
    }

    public MockTaskBuilder withPriority(int priority) {
        when(task.getPriority()).thenReturn(priority);
        return this;
    }

    public MockTaskBuilder withProcessDefinitionId(String processDefinitionId) {
        when(task.getProcessDefinitionId()).thenReturn(processDefinitionId);
        return this;
    }

    public MockTaskBuilder withProcessInstanceId(String processInstanceId) {
        when(task.getProcessInstanceId()).thenReturn(processInstanceId);
        return this;
    }

    public MockTaskBuilder withParentTaskId(String parentTaskId) {
        when(task.getParentTaskId()).thenReturn(parentTaskId);
        return this;
    }

    public MockTaskBuilder withSuspended(boolean suspended) {
        when(task.isSuspended()).thenReturn(suspended);
        return this;
    }

    public MockTaskBuilder withCancelled(boolean cancelled) {
        if (task instanceof TaskEntity) {
            when(((TaskEntity) task).isCanceled()).thenReturn(cancelled);
        }
        return this;
    }

    public MockTaskBuilder withDeleted(boolean deleted) {
        if (task instanceof TaskEntity) {
            when(((TaskEntity) task).isDeleted()).thenReturn(deleted);
        }
        return this;
    }

    public MockTaskBuilder withFormKey(String formKey) {
        when(task.getFormKey()).thenReturn(formKey);
        return this;
    }
    
    public MockTaskBuilder withTaskDefinitionKey(String taskDefinitionKey) {
        when(task.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
        return this;
    }

    public MockTaskBuilder withAppVersion(Integer appVersion) {
        when(task.getAppVersion()).thenReturn(appVersion);
        return this;
    }

    public MockTaskBuilder withBusinessKey(String businessKey) {
        when(task.getBusinessKey()).thenReturn(businessKey);
        return this;
    }

    public Task build() {
        return task;
    }
}
