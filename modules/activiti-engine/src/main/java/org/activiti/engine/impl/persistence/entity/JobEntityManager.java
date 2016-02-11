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

package org.activiti.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.AsyncJobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.runtime.Job;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class JobEntityManager extends AbstractManager {

  public void send(MessageEntity message) {
  	
  	ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
  	
  	if (processEngineConfiguration.isAsyncExecutorEnabled()) {
  	
  		// If the async executor is enabled, we need to set the duedate of the job to the current date + the default lock time. 
  		// This is cope with the case where the async job executor or the process engine goes down
  		// before executing the job. This way, other async job executors can pick the job up after the max lock time.
  		Date dueDate = new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() 
  				+ processEngineConfiguration.getAsyncExecutor().getAsyncJobLockTimeInMillis());
  		message.setDuedate(dueDate);
  		message.setLockExpirationTime(null); // was set before, but to be quickly picked up needs to be set to null
  		
  	} else if (!processEngineConfiguration.isJobExecutorActivate()) {
  		
  		// If the async executor is disabled AND there is no old school job executor,
  		// The job needs to be picked up as soon as possible. So the due date is now set to the current time
  		message.setDuedate(processEngineConfiguration.getClock().getCurrentTime());
  		message.setLockExpirationTime(null); // was set before, but to be quickly picked up needs to be set to null
  	}
  	
    message.insert();
    if (processEngineConfiguration.isAsyncExecutorEnabled()) {
      hintAsyncExecutor(message);
    } else {
      hintJobExecutor(message);
    }
  }
 
  public void schedule(TimerEntity timer) {
    Date duedate = timer.getDuedate();
    if (duedate==null) {
      throw new ActivitiIllegalArgumentException("duedate is null");
    }

    timer.insert();

    ProcessEngineConfiguration engineConfiguration = Context.getProcessEngineConfiguration();
    if (engineConfiguration.isAsyncExecutorEnabled() == false && 
        timer.getDuedate().getTime() <= (engineConfiguration.getClock().getCurrentTime().getTime())) {

      hintJobExecutor(timer);
    }
  }
  
  /*"Not used anymore. Will be removed in a future release." */
  @Deprecated()
  public void retryAsyncJob(JobEntity job) {
    AsyncExecutor asyncExecutor = Context.getProcessEngineConfiguration().getAsyncExecutor();
    try {
    	
    	// If a job has to be retried, we wait for a certain amount of time,
    	// otherwise the job will be continuously be retried without delay (and thus seriously stressing the database).
	    Thread.sleep(asyncExecutor.getRetryWaitTimeInMillis());
	    
    } catch (InterruptedException e) {
    }
    asyncExecutor.executeAsyncJob(job);
  }
  
  protected void hintAsyncExecutor(JobEntity job) {  
    AsyncExecutor asyncExecutor = Context.getProcessEngineConfiguration().getAsyncExecutor();

    // notify job executor:      
    TransactionListener transactionListener = new AsyncJobAddedNotification(job, asyncExecutor);
    Context.getCommandContext()
      .getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }
  
  protected void hintJobExecutor(JobEntity job) {  
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();

    // notify job executor:      
    TransactionListener transactionListener = new JobAddedNotification(jobExecutor);
    Context.getCommandContext()
      .getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, transactionListener);
  }

  public void cancelTimers(ExecutionEntity execution) {
    List<TimerEntity> timers = Context
      .getCommandContext()
      .getJobEntityManager()
      .findTimersByExecutionId(execution.getId());
    
    for (TimerEntity timer: timers) {
      if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, timer));
      }
      timer.delete();
    }
  }

  public JobEntity findJobById(String jobId) {
    return (JobEntity) getDbSqlSession().selectOne("selectJob", jobId);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextJobsToExecute(Page page) {
    ProcessEngineConfiguration processEngineConfig = Context.getProcessEngineConfiguration();
    Date now = processEngineConfig.getClock().getCurrentTime();
    return getDbSqlSession().selectList("selectNextJobsToExecute", now, page);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextTimerJobsToExecute(Page page) {
    ProcessEngineConfiguration processEngineConfig = Context.getProcessEngineConfiguration();
    Date now = processEngineConfig.getClock().getCurrentTime();
    return getDbSqlSession().selectList("selectNextTimerJobsToExecute", now, page);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findAsyncJobsDueToExecute(Page page) {
    ProcessEngineConfiguration processEngineConfig = Context.getProcessEngineConfiguration();
    Date now = processEngineConfig.getClock().getCurrentTime();
    return getDbSqlSession().selectList("selectAsyncJobsDueToExecute", now, page);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs) {
  	return getDbSqlSession().selectList("selectJobsByLockOwner", lockOwner, start, maxNrOfJobs);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectJobsByExecutionId", executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("pid", processInstanceId);
    params.put("now", Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    return getDbSqlSession().selectList("selectExclusiveJobsToExecute", params);
  }


  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "selectJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     return getDbSqlSession().selectList("selectJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
  	 Map<String, String> params = new HashMap<String, String>(3);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionKey", processDefinitionKey);
     params.put("tenantId", tenantId);
     return getDbSqlSession().selectList("selectJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
  	 Map<String, String> params = new HashMap<String, String>(2);
     params.put("handlerType", jobHandlerType);
     params.put("processDefinitionId", processDefinitionId);
     return getDbSqlSession().selectList("selectJobByTypeAndProcessDefinitionId", params);
  }
  
  public void unacquireJob(String jobId) {
  	Map<String, Object> params = new HashMap<String, Object>(2);
  	params.put("id", jobId);
  	params.put("dueDate", new Date(getProcessEngineConfiguration().getClock().getCurrentTime().getTime()));
  	getDbSqlSession().update("unacquireJob", params);
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectJobCountByQueryCriteria", jobQuery);
  }
  
  public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
  	HashMap<String, Object> params = new HashMap<String, Object>();
  	params.put("deploymentId", deploymentId);
  	params.put("tenantId", newTenantId);
  	getDbSqlSession().update("updateJobTenantIdForDeployment", params);
  }
  
  public int updateJobLockForAllJobs(String lockOwner, Date expirationTime) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("lockOwner", lockOwner);
    params.put("lockExpirationTime", expirationTime);
    params.put("dueDate", Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    return getDbSqlSession().update("updateJobLockForAllJobs", params);
  }
  
}
