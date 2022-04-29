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

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;

/**


 */
public class GetTaskVariableCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String variableName;
  protected boolean isLocal;

  public GetTaskVariableCmd(String taskId, String variableName, boolean isLocal) {
    this.taskId = taskId;
    this.variableName = variableName;
    this.isLocal = isLocal;
  }

  public Object execute(CommandContext commandContext) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("taskId is null");
    }
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }

    TaskEntity task = commandContext.getTaskEntityManager().findById(taskId);

    if (task == null) {
      throw new ActivitiObjectNotFoundException("task " + taskId + " doesn't exist", Task.class);
    }

    Object value;

    if (isLocal) {
      value = task.getVariableLocal(variableName, false);
    } else {
      value = task.getVariable(variableName, false);
    }

    return value;
  }
}
