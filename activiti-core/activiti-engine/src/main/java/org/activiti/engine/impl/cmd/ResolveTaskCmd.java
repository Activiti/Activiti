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

import java.util.Map;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.DelegationState;

/**


 */
public class ResolveTaskCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  protected Map<String, Object> variables;
  protected Map<String, Object> transientVariables;

  public ResolveTaskCmd(String taskId, Map<String, Object> variables) {
    super(taskId);
    this.variables = variables;
  }

  public ResolveTaskCmd(String taskId, Map<String, Object> variables, Map<String, Object> transientVariables) {
    this(taskId, variables);
    this.transientVariables = transientVariables;
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {
    if (variables != null) {
      task.setVariables(variables);
    }
    if (transientVariables != null) {
      task.setTransientVariables(transientVariables);
    }

    task.setDelegationState(DelegationState.RESOLVED);
    commandContext.getTaskEntityManager().changeTaskAssignee(task, task.getOwner());

    return null;
  }

  @Override
  protected String getSuspendedTaskException() {
    return "Cannot resolve a suspended task";
  }

}
