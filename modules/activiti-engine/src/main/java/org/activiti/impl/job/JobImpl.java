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
package org.activiti.impl.job;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.Job;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistentObject;


/**
 * Stub of the common parts of a Job.
 * You will normally work with a subclass of
 *  JobImpl, such as {@link TimerImpl} or
 *  {@link MessageImpl}.
 * @author Tom Baeyens
 * @author Nick Burch
 */
public abstract class JobImpl implements Serializable, Job, PersistentObject, Command <Void> {
  
  public static final boolean DEFAULT_EXCLUSIVE = false;
  public static final int DEFAULT_RETRIES = 3;

  private static final long serialVersionUID = 1L;
  
  protected String id;
  
  protected Date duedate;

  protected String lockOwner = null;
  protected Date lockExpirationTime = null;

  protected String executionId = null;
  protected String processInstanceId = null;

  protected boolean exclusive = DEFAULT_EXCLUSIVE;

  protected int retries = DEFAULT_RETRIES;
  protected String exception = null;
  
  protected String jobHandlerType = null;
  protected String jobHandlerConfiguration = null;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("exception", exception);
    return persistentState;
  }
  
  public Void execute(CommandContext commandContext) {
    JobHandlers jobHandlers = commandContext.getJobHandlers();
    JobHandler jobHandler = jobHandlers.getJobHandler(jobHandlerType);
    ExecutionImpl execution = null;
    if (executionId!=null) {
      execution = commandContext
        .getPersistenceSession()
        .findExecution(executionId);
    }
    jobHandler.execute(jobHandlerConfiguration, execution, commandContext);
    return null;
  }
  
  public void setExecution(ExecutionImpl execution) {
    executionId = execution.getId();
    processInstanceId = execution.getProcessInstance().getId();
  }

  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public int getRetries() {
    return retries;
  }
  public void setRetries(int retries) {
    this.retries = retries;
  }
  public String getException() {
    return exception;
  }
  public String getLockOwner() {
    return lockOwner;
  }
  public void setLockOwner(String claimedBy) {
    this.lockOwner = claimedBy;
  }
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  public void setLockExpirationTime(Date claimedUntil) {
    this.lockExpirationTime = claimedUntil;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public boolean isExclusive() {
    return exclusive;
  }
  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Date getDuedate() {
    return duedate;
  }
  public void setDuedate(Date duedate) {
    this.duedate = duedate;
  }
  public void setException(String exception) {
    this.exception = exception;
  }
  public String getJobHandlerType() {
    return jobHandlerType;
  }
  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }
  public String getJobHandlerConfiguration() {
    return jobHandlerConfiguration;
  }
  public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }
}
