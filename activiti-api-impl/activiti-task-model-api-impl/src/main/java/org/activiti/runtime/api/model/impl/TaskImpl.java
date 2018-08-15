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

import java.util.Date;
import java.util.Objects;

import org.activiti.runtime.api.model.Task;

public class TaskImpl implements Task {

    private String id;
    private String name;
    private Task.TaskStatus status;
    private String owner;
    private String assignee;
    private String description;
    private Date createdDate;
    private Date claimedDate;
    private Date dueDate;
    private int priority;
    private String processDefinitionId;
    private String processInstanceId;
    private String parentTaskId;

    public TaskImpl() {
    }

    public TaskImpl(String id,
                    String name,
                    Task.TaskStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }



    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public TaskStatus getStatus() {
        return status;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskImpl task = (TaskImpl) o;
        return priority == task.priority &&
                Objects.equals(id,
                               task.id) &&
                Objects.equals(owner,
                               task.owner) &&
                Objects.equals(assignee,
                               task.assignee) &&
                Objects.equals(name,
                               task.name) &&
                Objects.equals(description,
                               task.description) &&
                Objects.equals(createdDate,
                               task.createdDate) &&
                Objects.equals(claimedDate,
                               task.claimedDate) &&
                Objects.equals(dueDate,
                               task.dueDate) &&
                Objects.equals(processDefinitionId,
                               task.processDefinitionId) &&
                Objects.equals(processInstanceId,
                               task.processInstanceId) &&
                Objects.equals(parentTaskId,
                               task.parentTaskId) &&
                status == task.status;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id,
                            owner,
                            assignee,
                            name,
                            description,
                            createdDate,
                            claimedDate,
                            dueDate,
                            priority,
                            processDefinitionId,
                            processInstanceId,
                            parentTaskId,
                            status);
    }

    @Override
    public String toString() {
        return "TaskImpl{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", assignee='" + assignee + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", claimedDate=" + claimedDate +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", parentTaskId='" + parentTaskId + '\'' +
                ", status=" + status +
                '}';
    }
}
