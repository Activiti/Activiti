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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.db.BulkDeleteable;
import org.apache.commons.lang3.StringUtils;

/**
 * Stub of the common parts of a Job. You will normally work with a subclass of JobEntity, such as {@link TimerEntity} or {@link MessageEntity}.
 *
 * @author Tom Baeyens
 * @author Nick Burch
 * @author Dave Syer
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public abstract class JobEntityImpl implements JobEntity, BulkDeleteable, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected Date duedate;

  protected String lockOwner;
  protected Date lockExpirationTime;

  protected String executionId;
  protected String processInstanceId;
  protected String processDefinitionId;

  protected boolean isExclusive = DEFAULT_EXCLUSIVE;

  protected int retries = DEFAULT_RETRIES;

  protected String jobHandlerType;
  protected String jobHandlerConfiguration;

  protected ByteArrayRef exceptionByteArrayRef;

  protected String exceptionMessage;

  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected String jobType;

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("duedate", duedate);
    persistentState.put("exceptionMessage", exceptionMessage);
    
    if (exceptionByteArrayRef != null) {
      persistentState.put("exceptionByteArrayId", exceptionByteArrayRef.getId());
    }
    
    return persistentState;
  }

  // getters and setters ////////////////////////////////////////////////////////

  public void setExecution(ExecutionEntity execution) {
    executionId = execution.getId();
    processInstanceId = execution.getProcessInstanceId();
    processDefinitionId = execution.getProcessDefinitionId();
    execution.getJobs().add(this);
  }

  public String getExceptionStacktrace() {
    if (exceptionByteArrayRef == null) {
      return null;
    }
    byte[] bytes = exceptionByteArrayRef.getBytes();
    if (bytes == null) {
      return null;
    }
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("UTF-8 is not a supported encoding");
    }
  }

  public void setExceptionStacktrace(String exception) {
    if (exceptionByteArrayRef == null) {
      exceptionByteArrayRef = new ByteArrayRef();
    }
    exceptionByteArrayRef.setValue("stacktrace", getUtf8Bytes(exception));
  }

  private byte[] getUtf8Bytes(String str) {
    if (str == null) {
      return null;
    }
    try {
      return str.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("UTF-8 is not a supported encoding");
    }
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public Date getDuedate() {
    return duedate;
  }

  public void setDuedate(Date duedate) {
    this.duedate = duedate;
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

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
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

  public String getExceptionMessage() {
    return exceptionMessage;
  }

  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = StringUtils.abbreviate(exceptionMessage, MAX_EXCEPTION_MESSAGE_LENGTH);
  }
  public String getJobType() {
    return jobType;
  }
  public void setJobType(String jobType) {
    this.jobType = jobType;
  }
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public ByteArrayRef getExceptionByteArrayRef() {
    return exceptionByteArrayRef;
  }

  @Override
  public String toString() {
    return "JobEntity [id=" + id + "]";
  }

}
