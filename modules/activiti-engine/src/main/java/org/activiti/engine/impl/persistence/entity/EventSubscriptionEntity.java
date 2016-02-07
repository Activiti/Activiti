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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * @author Daniel Meyer
 */
public abstract class EventSubscriptionEntity implements PersistentObject, HasRevision, Serializable {

  private static final long serialVersionUID = 1L;
  
  // persistent state ///////////////////////////
  protected String id;
  protected int revision = 1;
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
  protected ActivityImpl activity;  
  
  /////////////////////////////////////////////
  
  public EventSubscriptionEntity() { 
    this.created = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
  }

  public EventSubscriptionEntity(ExecutionEntity executionEntity) {
    this();
    setExecution(executionEntity);
    setActivity(execution.getActivity());
    this.processInstanceId = executionEntity.getProcessInstanceId();
    this.processDefinitionId = executionEntity.getProcessDefinitionId();
  }
  
  // processing /////////////////////////////
  
  public void eventReceived(Serializable payload, boolean processASync) {
    if(processASync) {
      scheduleEventAsync(payload);
    } else {
      processEventSync(payload);
    }
  }
  
  protected void processEventSync(Object payload) {
    EventHandler eventHandler = Context.getProcessEngineConfiguration().getEventHandler(eventType);
    if (eventHandler == null) {
      throw new ActivitiException("Could not find eventhandler for event of type '" + eventType + "'.");
    }    
    eventHandler.handleEvent(this, payload, Context.getCommandContext());
  }
  
  protected void scheduleEventAsync(Serializable payload) {
    
    final CommandContext commandContext = Context.getCommandContext();

    MessageEntity message = new MessageEntity();
    message.setJobHandlerType(ProcessEventJobHandler.TYPE);
    message.setJobHandlerConfiguration(id);
    message.setTenantId(getTenantId());
    
    GregorianCalendar expireCal = new GregorianCalendar();
    ProcessEngineConfiguration processEngineConfig = Context.getCommandContext().getProcessEngineConfiguration();
    expireCal.setTime(processEngineConfig.getClock().getCurrentTime());
    expireCal.add(Calendar.SECOND, processEngineConfig.getLockTimeAsyncJobWaitTime());
    message.setLockExpirationTime(expireCal.getTime());

    // TODO: support payload
//    if(payload != null) {
//      message.setEventPayload(payload);
//    }
    
    commandContext.getJobEntityManager().send(message);
  }
  
  // persistence behavior /////////////////////

  public void delete() {
    Context.getCommandContext()
      .getEventSubscriptionEntityManager()
      .deleteEventSubscription(this);
    removeFromExecution();
  }
  
  public void insert() {
    Context.getCommandContext()
      .getEventSubscriptionEntityManager()
      .insert(this);
    addToExecution();   
  }
  
 // referential integrity -> ExecutionEntity ////////////////////////////////////
  
  protected void addToExecution() {
    // add reference in execution
    ExecutionEntity execution = getExecution();
    if(execution != null) {
      execution.addEventSubscription(this);
    }
  }
  
  protected void removeFromExecution() {
    // remove reference in execution
    ExecutionEntity execution = getExecution();
    if(execution != null) {
      execution.removeEventSubscription(this);
    }
  }
  
  public Object getPersistentState() {
    HashMap<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("executionId", executionId);
    persistentState.put("configuration", configuration);
    persistentState.put("processDefinitionId", processDefinitionId);
    return persistentState;
  }
  
  // getters & setters ////////////////////////////
    
  public ExecutionEntity getExecution() {
    if(execution == null && executionId != null) {
      execution = Context.getCommandContext()
              .getExecutionEntityManager()
              .findExecutionById(executionId);
    }
    return execution;
  }
    
  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
    if(execution != null) {
      this.executionId = execution.getId();
    }
  }
    
  public ActivityImpl getActivity() {
    if(activity == null && activityId != null) {
      ExecutionEntity execution = getExecution();
      if(execution != null) {
        ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
        activity = processDefinition.findActivity(activityId);
      }
    }
    return activity;
  }
    
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if(activity != null) {
      this.activityId = activity.getId();
    }
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
  
  public int getRevisionNext() {
    return revision +1;
  }

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
    EventSubscriptionEntity other = (EventSubscriptionEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
  
}
