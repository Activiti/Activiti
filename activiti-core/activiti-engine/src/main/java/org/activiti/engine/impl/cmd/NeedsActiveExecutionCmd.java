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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;


public abstract class NeedsActiveExecutionCmd<T> implements Command<T>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String executionId;

  public NeedsActiveExecutionCmd(String executionId) {
    this.executionId = executionId;
  }

  public T execute(CommandContext commandContext) {
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("executionId is null");
    }

    ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(executionId);

    if (execution == null) {
      throw new ActivitiObjectNotFoundException("execution " + executionId + " doesn't exist", Execution.class);
    }

    if (execution.isSuspended()) {
      throw new ActivitiException(getSuspendedExceptionMessage());
    }

    return execute(commandContext, execution);
  }

  /**
   * Subclasses should implement this method. The provided {@link ExecutionEntity} is guaranteed to be active (ie. not suspended).
   */
  protected abstract T execute(CommandContext commandContext, ExecutionEntity execution);

  /**
   * Subclasses can override this to provide a more detailed exception message that will be thrown when the execution is suspended.
   */
  protected String getSuspendedExceptionMessage() {
    return "Cannot execution operation because execution '" + executionId + "' is suspended";
  }

}
