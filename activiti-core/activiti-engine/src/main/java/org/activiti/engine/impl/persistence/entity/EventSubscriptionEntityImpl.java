/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.activiti.engine.impl.context.Context;

/**


 */
public abstract class EventSubscriptionEntityImpl extends AbstractEntity implements EventSubscriptionEntity, Serializable {

  private static final long serialVersionUID = 1L;

  // persistent state ///////////////////////////
  protected String eventType;
  protected String eventName;
  protected String executionId;
  protected String processInstanceId;
  protected String activityId;
  protected String configuration;
  protected Date created;
  protected String processDefinitionId;
  protected String tenantId;

  // runtime state /////////////////////////////
  protected ExecutionEntity execution;

  public EventSubscriptionEntityImpl() {
    this.created = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
  }

  public Object getPersistentState() {
    HashMap<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("executionId", executionId);
    persistentState.put("configuration", configuration);
    return persistentState;
  }

  // getters & setters ////////////////////////////

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public ExecutionEntity getExecution() {
    if (execution == null && executionId != null) {
      execution = Context.getCommandContext().getExecutionEntityManager().findById(executionId);
    }
    return execution;
  }

  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
    if (execution != null) {
      this.executionId = execution.getId();
      this.processInstanceId = execution.getProcessInstanceId();
    }
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventSubscriptionEntityImpl other = (EventSubscriptionEntityImpl) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
