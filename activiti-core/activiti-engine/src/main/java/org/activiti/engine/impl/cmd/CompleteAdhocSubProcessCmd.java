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

import org.activiti.bpmn.model.AdhocSubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;

import java.io.Serializable;
import java.util.List;


public class CompleteAdhocSubProcessCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String executionId;

  public CompleteAdhocSubProcessCmd(String executionId) {
    this.executionId = executionId;
  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    ExecutionEntity execution = executionEntityManager.findById(executionId);
    if (execution == null) {
      throw new ActivitiObjectNotFoundException("No execution found for id '" + executionId + "'", ExecutionEntity.class);
    }

    if (!(execution.getCurrentFlowElement() instanceof AdhocSubProcess)) {
      throw new ActivitiException("The current flow element of the requested execution is not an ad-hoc sub process");
    }

    List<? extends ExecutionEntity> childExecutions = execution.getExecutions();
    if (childExecutions.size() > 0) {
      throw new ActivitiException("Ad-hoc sub process has running child executions that need to be completed first");
    }

    ExecutionEntity outgoingFlowExecution = executionEntityManager.createChildExecution(execution.getParent());
    outgoingFlowExecution.setCurrentFlowElement(execution.getCurrentFlowElement());

    executionEntityManager.deleteExecutionAndRelatedData(execution, null);

    Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(outgoingFlowExecution, true);

    return null;
  }

}
