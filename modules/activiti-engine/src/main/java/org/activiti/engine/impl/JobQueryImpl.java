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

package org.activiti.engine.impl;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.JobQueryProperty;


/**
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class JobQueryImpl extends AbstractQuery<JobQuery, Job> implements JobQuery {
  
  protected String id;
  protected String processInstanceId;
  protected String executionId;
  protected boolean retriesLeft;
  protected boolean executable;
  protected boolean onlyTimers;
  protected boolean onlyMessages;
  protected Date duedateHigherThen;
  protected Date duedateLowerThen;
  protected Date duedateHigherThenOrEqual;
  protected Date duedateLowerThenOrEqual;
  protected JobQueryProperty orderProperty;
  
  public JobQueryImpl() {
  }

  public JobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public JobQuery jobId(String jobId) {
    if (jobId == null) {
      throw new ActivitiException("Provided job id is null");
    }
    this.id = jobId;
    return this;
  }

  public JobQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Provided process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public JobQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Provided execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  public JobQuery withRetriesLeft() {
    retriesLeft = true;
    return this;
  }

  public JobQuery executable() {
    executable = true;
    return this;
  }
  
  public JobQuery onlyTimers() {
    if (onlyMessages) {
      throw new ActivitiException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyTimers = true;
    return this;
  }
  
  public JobQuery onlyMessages() {
    if (onlyTimers) {
      throw new ActivitiException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyMessages = true;
    return this;
  }
  
  public JobQuery duedateHigherThen(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateHigherThen = date;
    return this;
  }
  
  public JobQuery duedateHigherThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateHigherThenOrEqual = date;
    return this;
  }
  
  public JobQuery duedateLowerThen(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateLowerThen = date;
    return this;
  }
  
  public JobQuery duedateLowerThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiException("Provided date is null");
    }
    this.duedateLowerThenOrEqual = date;
    return this;
  }
  
  //sorting //////////////////////////////////////////
  
  public JobQuery orderByDuedate() {
    return orderBy(JobQueryProperty.DUEDATE);
  }
  
  public JobQuery orderByExecutionId() {
    return orderBy(JobQueryProperty.EXECUTION_ID);
  }
  
  public JobQuery orderByJobId() {
    return orderBy(JobQueryProperty.JOB_ID);
  }
  
  public JobQuery orderByProcessInstanceId() {
    return orderBy(JobQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  public JobQuery orderByRetries() {
    return orderBy(JobQueryProperty.RETRIES);
  }
  
  public JobQuery orderBy(QueryProperty property) {
    if(!(property instanceof JobQueryProperty)) {
      throw new ActivitiException("Only JobQueryProperty can be used with orderBy");
    }
    this.orderProperty = (JobQueryProperty) property;
    return this;
  }
  
  public JobQuery asc() {
    return direction(Direction.ASCENDING);
  }
  
  public JobQuery desc() {
    return direction(Direction.DESCENDING);
  }
  
  public JobQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }
  
  //results //////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getRuntimeSession()
      .findJobCountByQueryCriteria(this);
  }

  public List<Job> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getRuntimeSession()
      .findJobsByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }

  
  //getters //////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public boolean getRetriesLeft() {
    return retriesLeft;
  }
  public boolean getExecutable() {
    return executable;
  }
  public Date getNow() {
    return ClockUtil.getCurrentTime();
  }
}
