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

public interface TaskComparator {

    TaskInfo getOriginalTask();
    TaskInfo getUpdatedTask();

    boolean hasTaskNameChanged();
    boolean hasTaskDescriptionChanged();
    boolean hasTaskOwnerChanged();
    boolean hasTaskPriorityChanged();
    boolean hasTaskCategoryChanged();
    boolean hasTaskFormKeyChanged();
    boolean hasTaskParentIdChanged();
    boolean hasTaskDefinitionKeyChanged();
    boolean hasTaskAssigneeChanged();
    boolean hasTaskDueDateChanged();

    default boolean hasTaskChanged() {
        return hasTaskNameChanged()
            || hasTaskDescriptionChanged()
            || hasTaskOwnerChanged()
            || hasTaskPriorityChanged()
            || hasTaskCategoryChanged()
            || hasTaskFormKeyChanged()
            || hasTaskParentIdChanged()
            || hasTaskDefinitionKeyChanged()
            || hasTaskAssigneeChanged()
            || hasTaskDueDateChanged();
    }

}
