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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.runtime.Job;
import org.activiti5.engine.ActivitiIllegalArgumentException;
import org.activiti5.engine.ProcessEngineConfiguration;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.JobQueryImpl;
import org.activiti5.engine.impl.Page;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti5.engine.impl.jobexecutor.AsyncJobAddedNotification;
import org.activiti5.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class JobEntityManager extends AbstractManager {

  public void send(JobEntity message) {
  	message.insert();
  	if (Context.getProcessEngineConfiguration().getAsyncExecutor().isActive()) {
  	  hintAsyncExecutor(message);
  	}
  }
 
  public void schedule(TimerJobEntity timer) {
    Date duedate = timer.getDuedate();
    if (duedate==null) {
      throw new ActivitiIllegalArgumentException("duedate is null");
    }

    timer.insert();
  }
  
  protected void hintAsyncExecutor(Job job) {  
    AsyncExecutor asyncExecutor = Context.getProcessEngineConfiguration().getAsyncExecutor();

    // notify job executor:      
    CommandContextCloseListener commandContextCloseListener = new AsyncJobAddedNotification(job, asyncExecutor);
    Context.getCommandContext().addCloseListener(commandContextCloseListener);
  }
  
  public void cancelTimers(ExecutionEntity execution) {
    List<TimerJobEntity> timers = Context
      .getCommandContext()
      .getJobEntityManager()
      .findTimersByExecutionId(execution.getId());
    
    for (TimerJobEntity timer: timers) {
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
  public List<JobEntity> findJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs) {
  	return getDbSqlSession().selectList("selectJobsByLockOwner", lockOwner, start, maxNrOfJobs);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectJobsByExecutionId", executionId);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectJobsByProcessInstanceId", processInstanceId);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    Map<String,Object> params = new HashMap<String, Object>();
    params.put("pid", processInstanceId);
    params.put("now", Context.getProcessEngineConfiguration().getClock().getCurrentTime());
    return getDbSqlSession().selectList("selectExclusiveJobsToExecute", params);
  }


  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getDbSqlSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimersByExecutionId(String executionId) {
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
