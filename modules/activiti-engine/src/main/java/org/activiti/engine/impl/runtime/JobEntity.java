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
package org.activiti.engine.impl.runtime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.Job;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.JobHandlers;

/**
 * Stub of the common parts of a Job. You will normally work with a subclass of
 * JobEntity, such as {@link TimerEntity} or {@link MessageEntity}.
 *
 * @author Tom Baeyens
 * @author Nick Burch
 * @author Dave Syer
 */
public abstract class JobEntity implements Serializable, Job, PersistentObject {

  public static final boolean DEFAULT_EXCLUSIVE = false;
  public static final int DEFAULT_RETRIES = 3;

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected Date duedate;

  protected String lockOwner = null;
  protected Date lockExpirationTime = null;

  protected String executionId = null;
  protected String processInstanceId = null;

  protected boolean isExclusive = DEFAULT_EXCLUSIVE;

  protected int retries = DEFAULT_RETRIES;
  protected String exception = null;

  protected String jobHandlerType = null;
  protected String jobHandlerConfiguration = null;

  public void execute(CommandContext commandContext) {
    RuntimeSession runtimeSession = commandContext.getRuntimeSession();
    ExecutionEntity execution = null;
    if (executionId != null) {
      execution = runtimeSession.findExecutionById(executionId);
    }

    JobHandlers jobHandlers = commandContext.getProcessEngineConfiguration().getJobHandlers();
    JobHandler jobHandler = jobHandlers.getJobHandler(jobHandlerType);

    jobHandler.execute(jobHandlerConfiguration, execution, commandContext);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("exception", exception);
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  public void setExecution(ExecutionEntity execution) {
    executionId = execution.getId();
    processInstanceId = execution.getProcessInstanceId();
  }

  // getters and setters //////////////////////////////////////////////////////

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
    return isExclusive;
  }
  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
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
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
}
