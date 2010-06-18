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
public class JobImpl implements Serializable, PersistentObject, Command <Void> {
  
  private static final long serialVersionUID = 1L;
  
  private String id;

  private String lockOwner = null;
  private Date lockExpirationTime = null;

  private String executionId = null;
  private String processInstanceId = null;

  private boolean exclusive = false;

  private int retries = 3;
  private String exception = null;
  
  private String jobCommandType = null;
  private String jobCommandConfiguration = null;

  private Date dueDate = null;
  private String repeat = null;
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("exception", exception);
    persistentState.put("dueDate", dueDate);
    return persistentState;
  }
  
  public Void execute(CommandContext commandContext) {
    Map<String, JobHandler> jobHandlers = commandContext.getJobCommands();
    JobHandler jobHandler = jobHandlers.get(jobCommandType);
    ExecutionImpl execution = null;
    if (executionId!=null) {
      execution = commandContext
        .getPersistenceSession()
        .findExecution(executionId);
    }
    jobHandler.execute(jobCommandConfiguration, execution, commandContext);
    return null;
  }

//  public void setLastException(Exception exception) {
//    StringWriter sw = new StringWriter();
//    PrintWriter pw = new PrintWriter(sw);
//    exception.printStackTrace(pw);
//    pw.flush();
//    sw.flush();
//    this.exception = exception.toString() + 
//        "\n" + sw.toString();
//  }

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
  public void setException(String exception) {
    this.exception = exception;
  }

  
  public String getJobCommandType() {
    return jobCommandType;
  }

  
  public void setJobCommandType(String jobCommandType) {
    this.jobCommandType = jobCommandType;
  }

  
  public String getJobCommandConfiguration() {
    return jobCommandConfiguration;
  }

  
  public void setJobCommandConfiguration(String jobCommandConfiguration) {
    this.jobCommandConfiguration = jobCommandConfiguration;
  }
  
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public String getRepeat() {
    return repeat;
  }
  public void setRepeat(String repeat) {
    this.repeat = repeat;
  }
}
