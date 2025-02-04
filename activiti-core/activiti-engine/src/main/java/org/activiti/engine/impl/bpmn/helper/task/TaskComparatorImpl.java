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

import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.function.Function;

public class TaskComparatorImpl implements TaskComparator {

    private TaskInfo originalTask;
    private TaskInfo updatedTask;

    public void setOriginalTask(TaskInfo task) {
        this.originalTask = copyInformationFromTaskInfo(task);
    }
    public void setUpdatedTask(TaskInfo task) {
        if (originalTask==null) {
            throw new IllegalArgumentException("an originalTask is needed before setting an updatedTask");
        }
        this.updatedTask = copyInformationFromTaskInfo(task);
    }

    public TaskInfo getOriginalTask() {
        return originalTask;
    }

    public TaskInfo getUpdatedTask() {
        return updatedTask;
    }

    public boolean hasTaskNameChanged() {
        return hasStringFieldChanged(TaskInfo::getName);
    }

    public boolean hasTaskDefinitionKeyChanged() {
        return hasStringFieldChanged(TaskInfo::getTaskDefinitionKey);
    }

    public boolean hasTaskAssigneeChanged() {
        return hasStringFieldChanged(TaskInfo::getAssignee);
    }

    public boolean hasTaskDueDateChanged() {
        return hasDateFieldChanged(TaskInfo::getDueDate);
    }
    public boolean hasTaskDescriptionChanged() {
        return hasStringFieldChanged(TaskInfo::getDescription);
    }
    public boolean hasTaskOwnerChanged() {
        return hasStringFieldChanged(TaskInfo::getOwner);
    }
    public boolean hasTaskPriorityChanged() {
        return hasIntegerFieldChanged(TaskInfo::getPriority);
    }
    public boolean hasTaskCategoryChanged() {
        return hasStringFieldChanged(TaskInfo::getCategory);
    }
    public boolean hasTaskFormKeyChanged() {
        return hasStringFieldChanged(TaskInfo::getFormKey);
    }
    public boolean hasTaskParentIdChanged() {
        return hasStringFieldChanged(TaskInfo::getParentTaskId);
    }

    private boolean hasStringFieldChanged(Function<TaskInfo, String> comparableTaskGetter) {
        if (originalTask!=null && updatedTask!=null) {
            return !StringUtils.equals(comparableTaskGetter.apply(originalTask), comparableTaskGetter.apply(updatedTask));
        }
        return false;
    }
    private boolean hasIntegerFieldChanged(Function<TaskInfo, Integer> comparableTaskGetter) {
        if (originalTask!=null && updatedTask!=null) {
            return comparableTaskGetter.apply(originalTask) != comparableTaskGetter.apply(updatedTask);
        }
        return false;
    }

    private boolean hasDateFieldChanged(Function<TaskInfo, Date> comparableTaskGetter) {
        if (originalTask!=null && updatedTask!=null) {
            Date originalDate = comparableTaskGetter.apply(originalTask);
            Date newDate = comparableTaskGetter.apply(updatedTask);

            return (originalDate == null && newDate != null)
                || (originalDate != null && newDate == null)
                || (originalDate != null && !originalDate.equals(newDate));
        }
        return false;
    }

    private TaskInfo copyInformationFromTaskInfo(TaskInfo task) {
        if (task!=null) {
            TaskEntityImpl duplicatedTask = new TaskEntityImpl();

            duplicatedTask.setName(task.getName());
            duplicatedTask.setDueDate(task.getDueDate());
            duplicatedTask.setDescription(task.getDescription());
            duplicatedTask.setId(task.getId());
            duplicatedTask.setOwner(task.getOwner());
            duplicatedTask.setPriority(task.getPriority());
            duplicatedTask.setCategory(task.getCategory());
            duplicatedTask.setFormKey(task.getFormKey());
            duplicatedTask.setAssignee(task.getAssignee());
            duplicatedTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
            duplicatedTask.setParentTaskId(task.getParentTaskId());

            return duplicatedTask;
        }
        throw new IllegalArgumentException("task must be non-null");
    }
}
