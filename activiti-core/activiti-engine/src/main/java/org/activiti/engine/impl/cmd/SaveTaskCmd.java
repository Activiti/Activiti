/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;


public class SaveTaskCmd implements Command<Task>, Serializable {

  private static final long serialVersionUID = 1L;

  protected TaskEntity task;

  public SaveTaskCmd(Task task) {
    this.task = (TaskEntity) task;
  }

  public Task execute(CommandContext commandContext) {
    if (task == null) {
      throw new ActivitiIllegalArgumentException("task is null");
    }

    if (task.getRevision() == 0) {
      commandContext.getTaskEntityManager().insert(task, null);

      if (commandContext.getEventDispatcher().isEnabled()) {
        commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_CREATED, task));
        if (task.getAssignee() != null) {
          commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_ASSIGNED, task));
        }
      }

    } else {

      TaskInfo originalTaskEntity = null;
      if (commandContext.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
        originalTaskEntity = commandContext.getHistoricTaskInstanceEntityManager().findById(task.getId());
      }

      if (originalTaskEntity == null) {
        originalTaskEntity = commandContext.getTaskEntityManager().findById(task.getId());
      }

      String originalName = originalTaskEntity.getName();
      String originalAssignee = originalTaskEntity.getAssignee();
      String originalOwner = originalTaskEntity.getOwner();
      String originalDescription = originalTaskEntity.getDescription();
      Date originalDueDate = originalTaskEntity.getDueDate();
      int originalPriority = originalTaskEntity.getPriority();
      String originalCategory = originalTaskEntity.getCategory();
      String originalFormKey = originalTaskEntity.getFormKey();
      String originalParentTaskId = originalTaskEntity.getParentTaskId();
      String originalTaskDefinitionKey = originalTaskEntity.getTaskDefinitionKey();

      // Only update history if history is enabled
      if (commandContext.getProcessEngineConfiguration().getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

        if (!StringUtils.equals(originalName, task.getName())) {
          commandContext.getHistoryManager().recordTaskNameChange(task.getId(), task.getName());
        }
        if (!StringUtils.equals(originalDescription, task.getDescription())) {
          commandContext.getHistoryManager().recordTaskDescriptionChange(task.getId(), task.getDescription());
        }
        if ((originalDueDate == null && task.getDueDate() != null)
            || (originalDueDate != null && task.getDueDate() == null)
            || (originalDueDate != null && !originalDueDate.equals(task.getDueDate()))) {
          commandContext.getHistoryManager().recordTaskDueDateChange(task.getId(), task.getDueDate());
        }
        if (originalPriority != task.getPriority()) {
          commandContext.getHistoryManager().recordTaskPriorityChange(task.getId(), task.getPriority());
        }
        if (!StringUtils.equals(originalCategory, task.getCategory())) {
          commandContext.getHistoryManager().recordTaskCategoryChange(task.getId(), task.getCategory());
        }
        if (!StringUtils.equals(originalFormKey, task.getFormKey())) {
          commandContext.getHistoryManager().recordTaskFormKeyChange(task.getId(), task.getFormKey());
        }
        if (!StringUtils.equals(originalParentTaskId, task.getParentTaskId())) {
          commandContext.getHistoryManager().recordTaskParentTaskIdChange(task.getId(), task.getParentTaskId());
        }
        if (!StringUtils.equals(originalTaskDefinitionKey, task.getTaskDefinitionKey())) {
          commandContext.getHistoryManager().recordTaskDefinitionKeyChange(task.getId(), task.getTaskDefinitionKey());
        }

      }

      if (!StringUtils.equals(originalOwner, task.getOwner())) {
        if (task.getProcessInstanceId() != null) {
          commandContext.getIdentityLinkEntityManager().involveUser(task.getProcessInstance(), task.getOwner(), IdentityLinkType.PARTICIPANT);
        }
        commandContext.getHistoryManager().recordTaskOwnerChange(task.getId(), task.getOwner());
      }
      if (!StringUtils.equals(originalAssignee, task.getAssignee())) {
        if (task.getProcessInstanceId() != null) {
          commandContext.getIdentityLinkEntityManager().involveUser(task.getProcessInstance(), task.getAssignee(), IdentityLinkType.PARTICIPANT);
        }
        commandContext.getHistoryManager().recordTaskAssigneeChange(task.getId(), task.getAssignee());

        commandContext.getProcessEngineConfiguration().getListenerNotificationHelper().executeTaskListeners(task, TaskListener.EVENTNAME_ASSIGNMENT);
        commandContext.getHistoryManager().recordTaskAssignment(task);

        if (commandContext.getEventDispatcher().isEnabled()) {
          commandContext.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_ASSIGNED, task));
        }

      }

      return commandContext.getTaskEntityManager().update(task);

    }


    return null;
  }

}
