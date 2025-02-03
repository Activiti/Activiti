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
package org.activiti.engine.impl.bpmn.helper.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.task.TaskInfo;

import java.util.Date;

public class ComparableTask {

    private Class<?> originalTaskClass;
    private String id;
    private String name;
    private Date dueDate;
    private String description;
    private String owner;
    private int priority;
    private String category;
    private String formKey;
    private String assignee;
    private String taskDefinitionKey;
    private String parentTaskId;

    public ComparableTask(DelegateTask task) {
        if (task!=null) {
            this.originalTaskClass = DelegateTask.class;
            this.id = task.getId();
            this.name = task.getName();
            this.dueDate = task.getDueDate();
            this.description = task.getDescription();
            this.owner = task.getOwner();
            this.priority = task.getPriority();
            this.category = task.getCategory();
            this.formKey = task.getFormKey();
            this.assignee = task.getAssignee();
            this.taskDefinitionKey = task.getTaskDefinitionKey();
            this.parentTaskId = null;  // DelegateTask doesn't have ParentTaskId
        }
    }

    public ComparableTask(TaskInfo task) {
        if (task!=null) {
            this.originalTaskClass = TaskInfo.class;
            this.id = task.getId();
            this.name = task.getName();
            this.dueDate = task.getDueDate();
            this.description = task.getDescription();
            this.owner = task.getOwner();
            this.priority = task.getPriority();
            this.category = task.getCategory();
            this.formKey = task.getFormKey();
            this.assignee = task.getAssignee();
            this.taskDefinitionKey = task.getTaskDefinitionKey();
            this.parentTaskId = task.getParentTaskId();
        }
    }

    public Class<?> getOriginalTaskClass() {
        return originalTaskClass;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public int getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getFormKey() {
        return formKey;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }
}
