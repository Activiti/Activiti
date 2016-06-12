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
package org.activiti5.engine.impl.cmd;

import java.util.List;

import org.activiti.engine.runtime.Job;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ActivitiIllegalArgumentException;
import org.activiti5.engine.ActivitiObjectNotFoundException;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.persistence.entity.JobEntity;
import org.activiti5.engine.impl.persistence.entity.SuspendedJobEntity;
import org.activiti5.engine.impl.persistence.entity.SuspensionState;
import org.activiti5.engine.impl.persistence.entity.SuspensionState.SuspensionStateUtil;
import org.activiti5.engine.impl.persistence.entity.TaskEntity;
import org.activiti5.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti5.engine.runtime.Execution;

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
    
    if(executionId == null) {
      throw new ActivitiIllegalArgumentException("ProcessInstanceId cannot be null.");
    }
    
    ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findExecutionById(executionId);

    if(executionEntity == null) {
      throw new ActivitiObjectNotFoundException("Cannot find processInstance for id '"+executionId+"'.", Execution.class);
    }
    if(!executionEntity.isProcessInstanceType()) {
      throw new ActivitiException("Cannot set suspension state for execution '"+executionId+"': not a process instance.");
    }
    
    SuspensionStateUtil.setSuspensionState(executionEntity, getNewState());
    
    // All child executions are suspended
    List<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByProcessInstanceId(executionId);
    for (ExecutionEntity childExecution : childExecutions) {
      if (!childExecution.getId().equals(executionId)) {
        SuspensionStateUtil.setSuspensionState(childExecution, getNewState());
      }
    }
    
    // All tasks are suspended
    List<TaskEntity> tasks = commandContext.getTaskEntityManager().findTasksByProcessInstanceId(executionId);
    for (TaskEntity taskEntity : tasks) {
      SuspensionStateUtil.setSuspensionState(taskEntity, getNewState());
    }
    
    if (getNewState() == SuspensionState.ACTIVE) {
      List<SuspendedJobEntity> suspendedJobs = commandContext.getSuspendedJobEntityManager().findSuspendedJobsByProcessInstanceId(executionId);
      for (SuspendedJobEntity suspendedJob : suspendedJobs) {
        if (Job.JOB_TYPE_TIMER.equals(suspendedJob.getJobType())) {
          TimerJobEntity timerJob = new TimerJobEntity(suspendedJob);
          commandContext.getTimerJobEntityManager().insert(timerJob);
          commandContext.getSuspendedJobEntityManager().delete(suspendedJob);
          
        } else {
          JobEntity job = new JobEntity(suspendedJob);
          commandContext.getJobEntityManager().insert(job);
          commandContext.getSuspendedJobEntityManager().delete(suspendedJob);
        }
      }
      
    } else {
      List<TimerJobEntity> timerJobs = commandContext.getTimerJobEntityManager().findTimerJobsByProcessInstanceId(executionId);
      for (TimerJobEntity timerJob : timerJobs) {
        SuspendedJobEntity suspendedJob = new SuspendedJobEntity(timerJob);
        commandContext.getSuspendedJobEntityManager().insert(suspendedJob);
        commandContext.getTimerJobEntityManager().delete(timerJob);
      }
      
      List<JobEntity> jobs = commandContext.getJobEntityManager().findJobsByProcessInstanceId(executionId);
      for (JobEntity job : jobs) {
        SuspendedJobEntity suspendedJob = new SuspendedJobEntity(job);
        commandContext.getSuspendedJobEntityManager().insert(suspendedJob);
        commandContext.getJobEntityManager().delete(job);
      }
    }
    
    return null;
  }

  protected abstract SuspensionState getNewState();

}
