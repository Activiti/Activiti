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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.MessageAddedNotification;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;


/**
 * @author Tom Baeyens
 */
public class JobManager extends AbstractManager {

  public void send(MessageEntity message) {
    CommandContext commandContext = Context.getCommandContext();
    
    commandContext
      .getDbSqlSession()
      .insert(message);
    
    
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    commandContext
      .getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, new MessageAddedNotification(jobExecutor));
  }
  
  public void schedule(TimerEntity timer) {
    Date duedate = timer.getDuedate();
    if (duedate==null) {
      throw new ActivitiException("duedate is null");
    }
    
    CommandContext commandContext = Context.getCommandContext();
    
    commandContext
      .getDbSqlSession()
      .insert(timer);
    
    // Check if this timer fires before the next time the job executor will check for new timers to fire.
    // This is highly unlikely because normally waitTimeInMillis is 5000 (5 seconds)
    // and timers are usually set further in the future
    
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    int waitTimeInMillis = jobExecutor.getWaitTimeInMillis();
    if (duedate.getTime() < (ClockUtil.getCurrentTime().getTime()+waitTimeInMillis)) {
      // then notify the job executor.
      commandContext
        .getTransactionContext()
        .addTransactionListener(TransactionState.COMMITTED, new MessageAddedNotification(jobExecutor));
    }
  }

  public void cancelTimers(ExecutionEntity execution) {
    List<TimerEntity> timers = Context
      .getCommandContext()
      .getJobManager()
      .findTimersByExecutionId(execution.getId());
    
    for (TimerEntity timer: timers) {
      timer.delete();
    }
  }

  public JobEntity findJobById(String jobId) {
    return (JobEntity) getPersistenceSession().selectOne("selectJob", jobId);
  }
  
  @SuppressWarnings("unchecked")
  public List<JobEntity> findNextJobsToExecute(Page page) {
    Date now = ClockUtil.getCurrentTime();
    return getPersistenceSession().selectList("selectNextJobsToExecute", now, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getPersistenceSession().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return getPersistenceSession().selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    final String query = "org.activiti.persistence.selectJobByQueryCriteria";
    return getPersistenceSession().selectList(query, jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByConfiguration(String jobHandlerType, String jobHandlerConfiguration) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("handlerType", jobHandlerType);
    params.put("handlerConfiguration", jobHandlerConfiguration);
    return getPersistenceSession().selectList("org.activiti.persistence.selectJobsByConfiguration", params);
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getPersistenceSession().selectOne("org.activiti.persistence.selectJobCountByQueryCriteria", jobQuery);
  }

}
