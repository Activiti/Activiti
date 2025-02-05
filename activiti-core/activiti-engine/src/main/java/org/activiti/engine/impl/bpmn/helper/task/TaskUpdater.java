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

import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.TaskInfo;

public class TaskUpdater {

    private final CommandContext commandContext;
    private final boolean broadcastEvents;

    public TaskUpdater(CommandContext commandContext) {
        this(commandContext, true);
    }

    public TaskUpdater(CommandContext commandContext, boolean broadcastEvents) {
        this.commandContext = commandContext;
        this.broadcastEvents = broadcastEvents;
    }

    public void updateTask(TaskInfo originalTask, TaskInfo task) {
        TaskComparatorImpl taskComparator = new TaskComparatorImpl();
        taskComparator.setOriginalTask(originalTask);
        taskComparator.setUpdatedTask(task);

        recordTaskUpdated(taskComparator);

        TaskEntity taskEntity = (TaskEntity) task;
        if (taskComparator.hasTaskOwnerChanged()) {
            if (task.getProcessInstanceId() != null) {
                commandContext.getIdentityLinkEntityManager().involveUser(taskEntity.getProcessInstance(), task.getOwner(), IdentityLinkType.PARTICIPANT);
            }
            commandContext.getHistoryManager().recordTaskOwnerChange(task.getId(), task.getOwner());
        }
        if (taskComparator.hasTaskAssigneeChanged()) {
            if (task.getProcessInstanceId() != null) {
                commandContext.getIdentityLinkEntityManager().involveUser(taskEntity.getProcessInstance(), task.getAssignee(), IdentityLinkType.PARTICIPANT);
            }
            commandContext.getHistoryManager().recordTaskAssigneeChange(task.getId(), task.getAssignee());

            if (broadcastEvents) {
                commandContext.getProcessEngineConfiguration().getListenerNotificationHelper().executeTaskListeners(taskEntity, TaskListener.EVENTNAME_ASSIGNMENT);
            }
            commandContext.getHistoryManager().recordTaskAssignment(taskEntity);

            if (broadcastEvents && commandContext.getEventDispatcher().isEnabled()) {
                commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_ASSIGNED, task));
            }
        }
    }

    private void recordTaskUpdated(TaskComparatorImpl taskComparator) {
        // Only update history if history is enabled
        if (commandContext.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            TaskInfo updatedTask = taskComparator.getUpdatedTask();
            if (taskComparator.hasTaskNameChanged()) {
                commandContext.getHistoryManager().recordTaskNameChange(updatedTask.getId(), updatedTask.getName());
            }
            if (taskComparator.hasTaskDescriptionChanged()) {
                commandContext.getHistoryManager().recordTaskDescriptionChange(updatedTask.getId(), updatedTask.getDescription());
            }
            if (taskComparator.hasTaskDueDateChanged()) {
                commandContext.getHistoryManager().recordTaskDueDateChange(updatedTask.getId(), updatedTask.getDueDate());
            }
            if (taskComparator.hasTaskPriorityChanged()) {
                commandContext.getHistoryManager().recordTaskPriorityChange(updatedTask.getId(), updatedTask.getPriority());
            }
            if (taskComparator.hasTaskCategoryChanged()) {
                commandContext.getHistoryManager().recordTaskCategoryChange(updatedTask.getId(), updatedTask.getCategory());
            }
            if (taskComparator.hasTaskFormKeyChanged()) {
                commandContext.getHistoryManager().recordTaskFormKeyChange(updatedTask.getId(), updatedTask.getFormKey());
            }
            if (taskComparator.hasTaskParentIdChanged()) {
                commandContext.getHistoryManager().recordTaskParentTaskIdChange(updatedTask.getId(), updatedTask.getParentTaskId());
            }
            if (taskComparator.hasTaskDefinitionKeyChanged()) {
                commandContext.getHistoryManager().recordTaskDefinitionKeyChange(updatedTask.getId(), updatedTask.getTaskDefinitionKey());
            }
        }
    }
}
