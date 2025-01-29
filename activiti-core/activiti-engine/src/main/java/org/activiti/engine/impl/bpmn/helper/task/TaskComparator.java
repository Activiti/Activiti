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

public interface TaskComparator<Entity> {

    void setOriginalTask(Entity originalTask);
    Entity getOriginalTask();

    boolean hasTaskNameChanged(Entity newTask);
    boolean hasTaskDescriptionChanged(Entity newTask);
    boolean hasTaskOwnerChanged(Entity newTask);
    boolean hasTaskPriorityChanged(Entity newTask);
    boolean hasTaskCategoryChanged(Entity newTask);
    boolean hasTaskFormKeyChanged(Entity newTask);
    boolean hasTaskParentIdChanged(Entity newTask);
    boolean hasTaskDefinitionKeyChanged(Entity newTask);
    boolean hasTaskAssigneeChanged(Entity newTask);
    boolean hasTaskDueDateChanged(Entity newTask);

    default boolean hasTaskChanged(Entity newTask) {
        return hasTaskNameChanged(newTask)
            || hasTaskDescriptionChanged(newTask)
            || hasTaskOwnerChanged(newTask)
            || hasTaskPriorityChanged(newTask)
            || hasTaskCategoryChanged(newTask)
            || hasTaskFormKeyChanged(newTask)
            || hasTaskParentIdChanged(newTask)
            || hasTaskDefinitionKeyChanged(newTask)
            || hasTaskAssigneeChanged(newTask)
            || hasTaskDueDateChanged(newTask);
    }

}
