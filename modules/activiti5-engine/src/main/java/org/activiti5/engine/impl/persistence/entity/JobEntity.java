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
package org.activiti5.engine.impl.persistence.entity;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.jobexecutor.JobHandler;

/**
 * Job entity for persisting executable jobs.
 *
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class JobEntity extends AbstractJobEntity {

  private static final long serialVersionUID = 1L;

  protected String lockOwner = null;
  protected Date lockExpirationTime = null;
  
  public JobEntity() {}
  
  public JobEntity(AbstractJobEntity te) {
    this.id = te.getId();
    this.jobType = te.getJobType();
    this.revision = te.getRevision();
    this.jobHandlerConfiguration = te.getJobHandlerConfiguration();
    this.jobHandlerType = te.getJobHandlerType();
    this.isExclusive = te.isExclusive();
    this.repeat = te.getRepeat();
    this.retries = te.getRetries();
    this.endDate = te.getEndDate();
    this.executionId = te.getExecutionId();
    this.processInstanceId = te.getProcessInstanceId();
    this.processDefinitionId = te.getProcessDefinitionId();
    this.exceptionMessage = te.getExceptionMessage();
    setExceptionStacktrace(te.getExceptionStacktrace());

    // Inherit tenant
    this.tenantId = te.getTenantId();
  }

  public void execute(CommandContext commandContext) {
    ExecutionEntity execution = null;
    if (executionId != null) {
      execution = commandContext.getExecutionEntityManager().findExecutionById(executionId);
    }
    
    Map<String, JobHandler> jobHandlers = Context.getProcessEngineConfiguration().getJobHandlers();
    JobHandler jobHandler = jobHandlers.get(jobHandlerType);
    jobHandler.execute(this, jobHandlerConfiguration, execution, commandContext);
    delete();
    
    if (repeat != null) {
      TimerJobEntity timerRepeatJob = new TimerJobEntity(this);
      timerRepeatJob.scheduleNewTimer(commandContext);
    }
  }
  
  public void insert() {
    Context.getCommandContext()
      .getDbSqlSession()
      .insert(this);
    
    // add link to execution
    if (executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionEntityManager()
        .findExecutionById(executionId);
      execution.addJob(this);
      
      // Inherit tenant if (if applicable)
      if (execution.getTenantId() != null) {
      	setTenantId(execution.getTenantId());
      }
    }
    
    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, this));
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, this));
    }
  }
  
  public void delete() {
    Context.getCommandContext()
      .getDbSqlSession()
      .delete(this);

    // Also delete the job's exception byte array
    exceptionByteArrayRef.delete();

    // remove link to execution
    if (executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionEntityManager()
        .findExecutionById(executionId);
      execution.removeJob(this);
    }

    if(Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, this));
    }
  }

  public void setExecution(ExecutionEntity execution) {
    super.setExecution(execution);
    execution.addJob(this);
  }
  
  @SuppressWarnings("unchecked")
  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) super.getPersistentState();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    return persistentState;
  }
  
	public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }

  @Override
  public String toString() {
    return "JobEntity [id=" + id + "]";
  }
  
}
