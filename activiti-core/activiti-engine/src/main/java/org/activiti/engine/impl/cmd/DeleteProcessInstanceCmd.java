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
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;


public class DeleteProcessInstanceCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String deleteReason;

  public DeleteProcessInstanceCmd(String processInstanceId, String deleteReason) {
    this.processInstanceId = processInstanceId;
    this.deleteReason = deleteReason;
  }

  public Void execute(CommandContext commandContext) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("processInstanceId is null");
    }

    ExecutionEntity processInstanceEntity = commandContext.getExecutionEntityManager().findById(processInstanceId);

    if (processInstanceEntity == null) {
      throw new ActivitiObjectNotFoundException("No process instance found for id '" + processInstanceId + "'", ProcessInstance.class);
    }

    executeInternal(commandContext,processInstanceEntity);
    return null;
  }

  protected void executeInternal(CommandContext commandContext,ExecutionEntity processInstanceEntity) {
      commandContext.getExecutionEntityManager().deleteProcessInstance(processInstanceEntity.getProcessInstanceId(), deleteReason, false);
  }
}
