/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.cmd;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.impl.persistence.entity.SuspensionState.SuspensionStateUtil;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.runtime.Execution;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public abstract class AbstractSetProcessInstanceStateCmd implements Command<Void> {

  protected final String executionId;

  public AbstractSetProcessInstanceStateCmd(String executionId) {
    this.executionId = executionId;
  }

  public Void execute(CommandContext commandContext) {

    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("ProcessInstanceId cannot be null.");
    }

    ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(executionId);

    if (executionEntity == null) {
      throw new ActivitiObjectNotFoundException("Cannot find processInstance for id '" + executionId + "'.", Execution.class);
    }
    if (!executionEntity.isProcessInstanceType()) {
      throw new ActivitiException("Cannot set suspension state for execution '" + executionId + "': not a process instance.");
    }
    
    if (Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, executionEntity.getProcessDefinitionId())) {
      if (getNewState() == SuspensionState.ACTIVE) {
        commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler().activateProcessInstance(executionId);
      } else {
        commandContext.getProcessEngineConfiguration().getActiviti5CompatibilityHandler().suspendProcessInstance(executionId);
      }
      return null;
    }

    SuspensionStateUtil.setSuspensionState(executionEntity, getNewState());
    commandContext.getExecutionEntityManager().update(executionEntity, false);

    // All child executions are suspended
    Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByProcessInstanceId(executionId);
    for (ExecutionEntity childExecution : childExecutions) {
      if (!childExecution.getId().equals(executionId)) {
        SuspensionStateUtil.setSuspensionState(childExecution, getNewState());
        commandContext.getExecutionEntityManager().update(childExecution, false);
      }
    }

    // All tasks are suspended
    List<TaskEntity> tasks = commandContext.getTaskEntityManager().findTasksByProcessInstanceId(executionId);
    for (TaskEntity taskEntity : tasks) {
      SuspensionStateUtil.setSuspensionState(taskEntity, getNewState());
      commandContext.getTaskEntityManager().update(taskEntity, false);
    }

    return null;
  }

  protected abstract SuspensionState getNewState();

}
