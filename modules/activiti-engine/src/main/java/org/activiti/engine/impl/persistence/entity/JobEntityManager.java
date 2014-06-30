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
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.ExclusiveJobAddedNotification;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutorContext;
import org.activiti.engine.impl.jobexecutor.MessageAddedNotification;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.runtime.Job;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class JobEntityManager extends AbstractManager {

  public void send(MessageEntity message) {
    message.insert();
    hintJobExecutor(message);    
  }
 
  public void schedule(TimerEntity timer) {
    timer.insert();

    updateJobExecutor(timer);
  }

  /**
   * In the case when timer has changed we have to update JobExecutor as well.
   *
   * @param timer - changed timer
   */
  public void updateJobExecutor(TimerEntity timer) {
    // Check if this timer fires before the next time the job executor will check for new timers to fire.
    // This is highly unlikely because normally waitTimeInMillis is 5000 (5 seconds)
    // and timers are usually set further in the future
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    int waitTimeInMillis = jobExecutor.getWaitTimeInMillis();
    if (timer.getDuedate().getTime() < (Context.getProcessEngineConfiguration().getClock().getCurrentTime().getTime()+waitTimeInMillis)) {
      hintJobExecutor(timer);
    }
  }

  protected static void hintJobExecutor(JobEntity job) {
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
    TransactionListener transactionListener;
    if(job.isExclusive() 
            && jobExecutorContext != null 
            && jobExecutorContext.isExecutingExclusiveJob()) {
      // lock job & add to the queue of the current processor
      Date currentTime = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
      job.setLockExpirationTime(new Date(currentTime.getTime() + jobExecutor.getLockTimeInMillis()));
      job.setLockOwner(jobExecutor.getLockOwner());
      transactionListener = new ExclusiveJobAddedNotification(job.getId());      
    } else {
      // notify job executor:      
      transactionListener = new MessageAddedNotification(jobExecutor);
    }
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
      timer.delete();
    }
  }

  public JobEntity findJobById(String jobId) {
    return (JobEntity) getDbSqlSession().selectOne("selectJob", jobId);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextJobsToExecute(Page page) {
    Date now = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
    return getDbSqlSession().selectList("selectNextJobsToExecute", now, page);
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
  public List<Job> findJobsByConfiguration(String jobHandlerType, String jobHandlerConfiguration) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("handlerType", jobHandlerType);
    params.put("handlerConfiguration", jobHandlerConfiguration);
    return getDbSqlSession().selectList("selectJobsByConfiguration", params);
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

}
