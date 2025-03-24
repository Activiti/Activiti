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

package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.task.TaskUpdater;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;

import java.io.Serializable;

/**

 */
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

        TaskUpdater taskUpdater = new TaskUpdater(commandContext);
        taskUpdater.updateTask(originalTaskEntity, task);
      return commandContext.getTaskEntityManager().update(task);
    }

    return null;
  }

}
