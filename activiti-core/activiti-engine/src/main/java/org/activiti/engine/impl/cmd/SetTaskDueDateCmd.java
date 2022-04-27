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

import java.util.Date;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


public class SetTaskDueDateCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  protected Date dueDate;

  public SetTaskDueDateCmd(String taskId, Date dueDate) {
    super(taskId);
    this.dueDate = dueDate;
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {
    task.setDueDate(dueDate);
    commandContext.getHistoryManager().recordTaskDueDateChange(task.getId(), task.getDueDate());
    commandContext.getTaskEntityManager().update(task);
    return null;
  }

}
