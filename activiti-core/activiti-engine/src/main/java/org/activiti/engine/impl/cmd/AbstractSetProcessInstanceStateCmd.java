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

import java.util.Collection;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.impl.persistence.entity.SuspensionState.SuspensionStateUtil;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.runtime.Execution;

/**


 */
public abstract class AbstractSetProcessInstanceStateCmd implements Command<Void> {

  protected final String processInstanceId;

  public AbstractSetProcessInstanceStateCmd(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public Void execute(CommandContext commandContext) {

    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("ProcessInstanceId cannot be null.");
    }

    ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(processInstanceId);

    if (executionEntity == null) {
      throw new ActivitiObjectNotFoundException("Cannot find processInstance for id '" + processInstanceId + "'.", Execution.class);
    }
    if (!executionEntity.isProcessInstanceType()) {
      throw new ActivitiException("Cannot set suspension state for execution '" + processInstanceId + "': not a process instance.");
    }

    executeInternal(commandContext,executionEntity);

    return null;
  }

  protected void executeInternal(CommandContext commandContext,ExecutionEntity executionEntity){
      SuspensionStateUtil.setSuspensionState(executionEntity, getNewState());
      commandContext.getExecutionEntityManager().update(executionEntity, false);

      updateChildrenSuspensionState(commandContext);
      updateTaskSuspensionState(commandContext);
      suspendAllJobs(commandContext);
  }

  protected void suspendAllJobs(CommandContext commandContext){
      if (getNewState() == SuspensionState.ACTIVE) {
          List<SuspendedJobEntity> suspendedJobs = commandContext.getSuspendedJobEntityManager().findJobsByProcessInstanceId(processInstanceId);
          for (SuspendedJobEntity suspendedJob : suspendedJobs) {
              commandContext.getJobManager().activateSuspendedJob(suspendedJob);
          }

      } else {
          List<TimerJobEntity> timerJobs = commandContext.getTimerJobEntityManager().findJobsByProcessInstanceId(processInstanceId);
          for (TimerJobEntity timerJob : timerJobs) {
              commandContext.getJobManager().moveJobToSuspendedJob(timerJob);
          }

          List<JobEntity> jobs = commandContext.getJobEntityManager().findJobsByProcessInstanceId(processInstanceId);
          for (JobEntity job : jobs) {
              commandContext.getJobManager().moveJobToSuspendedJob(job);
          }
      }
  }
  protected void updateChildrenSuspensionState(CommandContext commandContext){
      Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByProcessInstanceId(processInstanceId);
      for (ExecutionEntity childExecution : childExecutions) {
          if (!childExecution.getId().equals(processInstanceId)) {
              SuspensionStateUtil.setSuspensionState(childExecution, getNewState());
              commandContext.getExecutionEntityManager().update(childExecution, false);
          }
      }
  }

  protected void updateTaskSuspensionState(CommandContext commandContext){
      List<TaskEntity> tasks = commandContext.getTaskEntityManager().findTasksByProcessInstanceId(processInstanceId);
      for (TaskEntity taskEntity : tasks) {
          SuspensionStateUtil.setSuspensionState(taskEntity, getNewState());
          commandContext.getTaskEntityManager().update(taskEntity, false);
      }
  }

  protected abstract SuspensionState getNewState();

}
