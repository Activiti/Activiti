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

import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.function.Function;

public class TaskComparatorImpl implements TaskComparator {

    private ComparableTask originalTask;
    private ComparableTask updatedTask;

    public void setOriginalTask(TaskInfo task) {
        this.originalTask = new ComparableTask(task);
    }
    public void setOriginalTask(ComparableTask comparableTask) {
        this.originalTask = comparableTask;
    }
    public void setUpdatedTask(TaskInfo task) {
        if (originalTask==null) {
            throw new IllegalArgumentException("an originalTask is needed before setting an updatedTask");
        }
        this.updatedTask = new ComparableTask(task);
    }
    public void setUpdatedTask(ComparableTask comparableTask) {
        if (originalTask==null) {
            throw new IllegalArgumentException("an originalTask is needed before setting an updatedTask");
        }
        this.updatedTask = comparableTask;
    }

    public ComparableTask getOriginalTask() {
        return originalTask;
    }

    public ComparableTask getUpdatedTask() {
        return updatedTask;
    }

    public boolean hasTaskNameChanged() {
        return hasStringFieldChanged(ComparableTask::getName);
    }

    public boolean hasTaskDefinitionKeyChanged() {
        return hasStringFieldChanged(ComparableTask::getTaskDefinitionKey);
    }

    public boolean hasTaskAssigneeChanged() {
        return hasStringFieldChanged(ComparableTask::getAssignee);
    }

    public boolean hasTaskDueDateChanged() {
        return hasDateFieldChanged(ComparableTask::getDueDate);
    }
    public boolean hasTaskDescriptionChanged() {
        return hasStringFieldChanged(ComparableTask::getDescription);
    }
    public boolean hasTaskOwnerChanged() {
        return hasStringFieldChanged(ComparableTask::getOwner);
    }
    public boolean hasTaskPriorityChanged() {
        return hasIntegerFieldChanged(ComparableTask::getPriority);
    }
    public boolean hasTaskCategoryChanged() {
        return hasStringFieldChanged(ComparableTask::getCategory);
    }
    public boolean hasTaskFormKeyChanged() {
        return hasStringFieldChanged(ComparableTask::getFormKey);
    }
    public boolean hasTaskParentIdChanged() {
        return hasStringFieldChanged(ComparableTask::getParentTaskId);
    }

    private boolean hasStringFieldChanged(Function<ComparableTask, String> comparableTaskGetter) {
        if (originalTask!=null && updatedTask!=null) {
            if (!StringUtils.equals(comparableTaskGetter.apply(originalTask), comparableTaskGetter.apply(updatedTask) )) {
                return true;
            }
        }
        return false;
    }
    private boolean hasIntegerFieldChanged(Function<ComparableTask, Integer> comparableTaskGetter) {
        if (originalTask!=null && updatedTask!=null) {
            if (comparableTaskGetter.apply(originalTask)!=comparableTaskGetter.apply(updatedTask)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDateFieldChanged(Function<ComparableTask, Date> comparableTaskGetter) {
        if (originalTask!=null && updatedTask!=null) {
            Date originalDate = comparableTaskGetter.apply(originalTask);
            Date newDate = comparableTaskGetter.apply(updatedTask);

            if ((originalDate == null && newDate != null)
                || (originalDate != null && newDate == null)
                || (originalDate != null && !originalDate.equals(newDate))) {
                return true;
            }
        }
        return false;
    }
}
