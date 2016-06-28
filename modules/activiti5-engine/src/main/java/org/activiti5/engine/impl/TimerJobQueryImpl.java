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

package org.activiti5.engine.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.activiti.engine.runtime.Job;
import org.activiti5.engine.ActivitiIllegalArgumentException;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.runtime.TimerJobQuery;


/**
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TimerJobQueryImpl extends AbstractQuery<TimerJobQuery, Job> implements TimerJobQuery, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected String id;
  protected String processInstanceId;
  protected String executionId;
  protected String processDefinitionId;
  protected boolean executable;
  protected boolean onlyTimers;
  protected boolean onlyMessages;
  protected Date duedateHigherThan;
  protected Date duedateLowerThan;
  protected Date duedateHigherThanOrEqual;
  protected Date duedateLowerThanOrEqual;
  protected boolean withException;
  protected String exceptionMessage;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  
  public TimerJobQueryImpl() {
  }

  public TimerJobQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public TimerJobQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TimerJobQuery jobId(String jobId) {
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("Provided job id is null");
    }
    this.id = jobId;
    return this;
  }

  public TimerJobQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Provided process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public TimerJobQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Provided process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public TimerJobQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("Provided execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  public TimerJobQuery executable() {
    executable = true;
    return this;
  }
  
  public TimerJobQuery timers() {
    if (onlyMessages) {
      throw new ActivitiIllegalArgumentException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyTimers = true;
    return this;
  }
  
  public TimerJobQuery messages() {
    if (onlyTimers) {
      throw new ActivitiIllegalArgumentException("Cannot combine onlyTimers() with onlyMessages() in the same query");
    }
    this.onlyMessages = true;
    return this;
  }
  
  public TimerJobQuery duedateHigherThan(Date date) {
    if (date == null) {
      throw new ActivitiIllegalArgumentException("Provided date is null");
    }
    this.duedateHigherThan = date;
    return this;
  }
  
  public TimerJobQuery duedateLowerThan(Date date) {
    if (date == null) {
      throw new ActivitiIllegalArgumentException("Provided date is null");
    }
    this.duedateLowerThan = date;
    return this;
  }
  
  public TimerJobQuery duedateHigherThen(Date date) {
    return duedateHigherThan(date);
  }
  
  public TimerJobQuery duedateHigherThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiIllegalArgumentException("Provided date is null");
    }
    this.duedateHigherThanOrEqual = date;
    return this;
  }
  
  public TimerJobQuery duedateLowerThen(Date date) {
    return duedateLowerThan(date);
  }
  
  public TimerJobQuery duedateLowerThenOrEquals(Date date) {
    if (date == null) {
      throw new ActivitiIllegalArgumentException("Provided date is null");
    }
    this.duedateLowerThanOrEqual = date;
    return this;
  }

  public TimerJobQuery withException() {
    this.withException = true;
    return this;
  }

  public TimerJobQuery exceptionMessage(String exceptionMessage) {
    if (exceptionMessage == null) {
      throw new ActivitiIllegalArgumentException("Provided exception message is null");
    }
    this.exceptionMessage = exceptionMessage;
    return this;
  }
  
  public TimerJobQuery jobTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("job is null");
  	}
  	this.tenantId = tenantId;
  	return this;
  }
  
  public TimerJobQuery jobTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("job is null");
  	}
  	this.tenantIdLike = tenantIdLike;
  	return this;
  }
  
  public TimerJobQuery jobWithoutTenantId() {
  	this.withoutTenantId = true;
  	return this;
  }
  
  //sorting //////////////////////////////////////////
  
  public TimerJobQuery orderByJobDuedate() {
    return orderBy(JobQueryProperty.DUEDATE);
  }
  
  public TimerJobQuery orderByExecutionId() {
    return orderBy(JobQueryProperty.EXECUTION_ID);
  }
  
  public TimerJobQuery orderByJobId() {
    return orderBy(JobQueryProperty.JOB_ID);
  }
  
  public TimerJobQuery orderByProcessInstanceId() {
    return orderBy(JobQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  public TimerJobQuery orderByJobRetries() {
    return orderBy(JobQueryProperty.RETRIES);
  }
  
  public TimerJobQuery orderByTenantId() {
  	 return orderBy(JobQueryProperty.TENANT_ID);
  }
  
  //results //////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getTimerJobEntityManager()
      .findTimerJobCountByQueryCriteria(this);
  }

  public List<Job> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getTimerJobEntityManager()
      .findTimerJobsByQueryCriteria(this, page);
  }
  
  //getters //////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public boolean getExecutable() {
    return executable;
  }
  public Date getNow() {
    return Context.getProcessEngineConfiguration().getClock().getCurrentTime();
  }
  public boolean isWithException() {
    return withException;
  }
  public String getExceptionMessage() {
    return exceptionMessage;
  }
	public String getTenantId() {
		return tenantId;
	}
	public String getTenantIdLike() {
		return tenantIdLike;
	}
	public boolean isWithoutTenantId() {
		return withoutTenantId;
	}
  
}
