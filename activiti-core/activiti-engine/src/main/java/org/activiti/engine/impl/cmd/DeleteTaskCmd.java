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

import java.io.Serializable;
import java.util.Collection;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


public class DeleteTaskCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected Collection<String> taskIds;
  protected boolean cascade;
  protected String deleteReason;
  protected boolean cancel;

  public DeleteTaskCmd(String taskId, String deleteReason, boolean cascade) {
    this(taskId, deleteReason, cascade, false);

  }

  public DeleteTaskCmd(String taskId, String deleteReason, boolean cascade, boolean cancel) {
    this.taskId = taskId;
    this.cascade = cascade;
    this.deleteReason = deleteReason;
    this.cancel = cancel;
  }

  public DeleteTaskCmd(Collection<String> taskIds, String deleteReason, boolean cascade) {
      this(taskIds,deleteReason,cascade,false);
  }

  public DeleteTaskCmd(Collection<String> taskIds, String deleteReason, boolean cascade, boolean cancel) {
    this.taskIds = taskIds;
    this.cascade = cascade;
    this.deleteReason = deleteReason;
    this.cancel = cancel;
  }

  public Void execute(CommandContext commandContext) {
    if (taskId != null) {
      deleteTask(commandContext, taskId);
    } else if (taskIds != null) {
      for (String taskId : taskIds) {
        deleteTask(commandContext, taskId);
      }
    } else {
      throw new ActivitiIllegalArgumentException("taskId and taskIds are null");
    }

    return null;
  }

  protected void deleteTask(CommandContext commandContext, String taskId) {
    commandContext.getTaskEntityManager().deleteTask(taskId, deleteReason, cascade, cancel);
  }
}
