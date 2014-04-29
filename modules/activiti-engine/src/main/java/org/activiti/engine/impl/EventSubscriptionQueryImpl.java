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

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;


/**
 * @author Daniel Meyer
 */
public class EventSubscriptionQueryImpl extends AbstractQuery<EventSubscriptionQueryImpl, EventSubscriptionEntity> implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String eventSubscriptionId;
  protected String eventName;
  protected String eventType;
  protected String executionId;
  protected String processInstanceId;
  protected String activityId;
  protected String tenantId;

  public EventSubscriptionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public EventSubscriptionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public EventSubscriptionQueryImpl eventSubscriptionId(String id) {
    if (eventSubscriptionId == null) {
      throw new ActivitiIllegalArgumentException("Provided svent subscription id is null");
    }
    this.eventSubscriptionId = id;
    return this;
  }

  public EventSubscriptionQueryImpl eventName(String eventName) {
    if (eventName == null) {
      throw new ActivitiIllegalArgumentException("Provided event name is null");
    }
    this.eventName = eventName;
    return this;
  }

  public EventSubscriptionQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("Provided execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  public EventSubscriptionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Provided process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }

  public EventSubscriptionQueryImpl activityId(String activityId) {
    if (activityId == null) {
      throw new ActivitiIllegalArgumentException("Provided activity id is null");
    }
    this.activityId = activityId;
    return this;
  }
  
  public EventSubscriptionQueryImpl eventType(String eventType) {
    if (eventType == null) {
      throw new ActivitiIllegalArgumentException("Provided event type is null");
    }
    this.eventType = eventType;
    return this;
  }
  
  public String getTenantId() {
		return tenantId;
	}

	public EventSubscriptionQueryImpl tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	public EventSubscriptionQueryImpl orderByCreated() {
    return orderBy(EventSubscriptionQueryProperty.CREATED);
  }
  
  //results //////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionEntityManager()
      .findEventSubscriptionCountByQueryCriteria(this);
  }

  @Override
  public List<EventSubscriptionEntity> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionEntityManager()
      .findEventSubscriptionsByQueryCriteria(this,page);
  }
  
  //getters //////////////////////////////////////////
  
   
  public String getEventSubscriptionId() {
    return eventSubscriptionId;
  }
  public String getEventName() {
    return eventName;
  }
  public String getEventType() {
    return eventType;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getActivityId() {
    return activityId;
  }

}
