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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class EventSubscriptionEntityManagerImpl extends AbstractEntityManager<EventSubscriptionEntity> implements EventSubscriptionEntityManager {

  /** keep track of subscriptions created in the current command */
  protected List<SignalEventSubscriptionEntity> createdSignalSubscriptions = new ArrayList<SignalEventSubscriptionEntity>();
  protected List<CompensateEventSubscriptionEntity> createdCompensateSubscriptions = new ArrayList<CompensateEventSubscriptionEntity>();
  
  @Override
  public void insert(EventSubscriptionEntity eventSubScriptionEntity) {
    super.insert(eventSubScriptionEntity);
    addToExecution(eventSubScriptionEntity);
  }

  @Override
  public SignalEventSubscriptionEntity insertSignalEvent(SignalEventDefinition signalEventDefinition, Signal signal, ExecutionEntity execution) {
    SignalEventSubscriptionEntity subscriptionEntity = new SignalEventSubscriptionEntity();
    subscriptionEntity.setExecution(execution);
    if (signal != null) {
      subscriptionEntity.setEventName(signal.getName());
      if (signal.getScope() != null) {
        subscriptionEntity.setConfiguration(signal.getScope());
      }
    } else {
      subscriptionEntity.setEventName(signalEventDefinition.getSignalRef());
    }

    subscriptionEntity.setActivityId(execution.getCurrentActivityId());
    subscriptionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
    if (execution.getTenantId() != null) {
      subscriptionEntity.setTenantId(execution.getTenantId());
    }
    insert(subscriptionEntity);
    execution.getEventSubscriptions().add(subscriptionEntity);
    createdSignalSubscriptions.add(subscriptionEntity);
    return subscriptionEntity;
  }

  @Override
  public MessageEventSubscriptionEntity insertMessageEvent(MessageEventDefinition messageEventDefinition, ExecutionEntity execution) {
    MessageEventSubscriptionEntity subscriptionEntity = new MessageEventSubscriptionEntity();
    subscriptionEntity.setExecution(execution);
    subscriptionEntity.setEventName(messageEventDefinition.getMessageRef());

    subscriptionEntity.setActivityId(execution.getCurrentActivityId());
    subscriptionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
    if (execution.getTenantId() != null) {
      subscriptionEntity.setTenantId(execution.getTenantId());
    }
    insert(subscriptionEntity);
    execution.getEventSubscriptions().add(subscriptionEntity);
    return subscriptionEntity;
  }
  
  @Override
  public CompensateEventSubscriptionEntity insertCompensationEvent(ExecutionEntity execution, String activityId) {
    CompensateEventSubscriptionEntity eventSubscription = new CompensateEventSubscriptionEntity();
    eventSubscription.setExecution(execution);
    eventSubscription.setActivityId(activityId);
    if (execution.getTenantId() != null) {
      eventSubscription.setTenantId(execution.getTenantId());
    }
    insert(eventSubscription);
    createdCompensateSubscriptions.add(eventSubscription);
    return eventSubscription;
  }
  
  @Override
  public List<CompensateEventSubscriptionEntity> findCompensateEventSubscriptionsByExecutionId(String executionId) {
    return findCompensateEventSubscriptionsByExecutionIdAndActivityId(executionId, null);
  }

  @Override
  public List<CompensateEventSubscriptionEntity> findCompensateEventSubscriptionsByExecutionIdAndActivityId(String executionId, String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = findEventSubscriptionsByExecutionAndType(executionId, "compensate");
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>();
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        if (activityId == null || activityId.equals(eventSubscriptionEntity.getActivityId())) {
          result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
        }
      }
    }
    return result;
  }
  
  @Override
  public List<CompensateEventSubscriptionEntity> findCompensateEventSubscriptionsByProcessInstanceIdAndActivityId(String processInstanceId, String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = findEventSubscriptionsByProcessInstanceAndActivityId(processInstanceId, activityId, "compensate");
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>();
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
    }
    return result;
  }
  
  @Override
  public void delete(EventSubscriptionEntity eventSubscriptionEntity) {
    super.delete(eventSubscriptionEntity);
    
    if (eventSubscriptionEntity instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.remove(eventSubscriptionEntity);
    }
  }
  
  protected void addToExecution(EventSubscriptionEntity eventSubscriptionEntity) {
    // add reference in execution
    ExecutionEntity execution = getExecution(eventSubscriptionEntity);
    if (execution != null) {
      execution.getEventSubscriptions().add(eventSubscriptionEntity);
    }
  }
  
  protected void removeFromExecution(EventSubscriptionEntity eventSubscriptionEntity) {
    // remove reference in execution
    ExecutionEntity execution = getExecution(eventSubscriptionEntity);
    if (execution != null) {
      execution.getExecutions().remove(eventSubscriptionEntity);
    }
  }
  
  protected ExecutionEntity getExecution(EventSubscriptionEntity eventSubscriptionEntity) {
    if (eventSubscriptionEntity.getExecution() != null) {
      return eventSubscriptionEntity.getExecution();
    } else if (eventSubscriptionEntity.getExecutionId() != null) {
      return Context.getCommandContext().getExecutionEntityManager().findExecutionById(eventSubscriptionEntity.getExecutionId());
    }
    return null;
  }

  @Override
  public void deleteEventSubscriptionsForProcessDefinition(String processDefinitionId) {
    getDbSqlSession().delete("deleteEventSubscriptionsForProcessDefinition", processDefinitionId);
  }

  @Override
  public EventSubscriptionEntity findEventSubscriptionbyId(String id) {
    return (EventSubscriptionEntity) getDbSqlSession().selectOne("selectEventSubscription", id);
  }

  @Override
  public long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl) {
    final String query = "selectEventSubscriptionCountByQueryCriteria";
    return (Long) getDbSqlSession().selectOne(query, eventSubscriptionQueryImpl);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page) {
    final String query = "selectEventSubscriptionByQueryCriteria";
    return getDbSqlSession().selectList(query, eventSubscriptionQueryImpl, page);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<MessageEventSubscriptionEntity> findMessageEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName) {
    final String query = "selectMessageEventSubscriptionsByProcessInstanceAndEventName";
    Map<String, String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventName", eventName);
    Set<MessageEventSubscriptionEntity> selectList = new HashSet<MessageEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));

    // add events created in this command (not visible yet in query)
    /*for (MessageEventSubscriptionEntity entity : created) {
      if (processInstanceId.equals(entity.getProcessInstanceId()) && eventName.equals(entity.getEventName())) {
        selectList.add(entity);
      }
    }*/

    return new ArrayList<MessageEventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName, String tenantId) {
    final String query = "selectSignalEventSubscriptionsByEventName";

    Set<SignalEventSubscriptionEntity> selectList = null;
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventName", eventName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
      selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));
    } else {
      selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));
    }

    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if (eventName.equals(entity.getEventName())) {
        selectList.add(entity);
      }
    }

    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName) {
    final String query = "selectSignalEventSubscriptionsByProcessInstanceAndEventName";
    Map<String, String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventName", eventName);
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));

    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if (processInstanceId.equals(entity.getProcessInstanceId()) && eventName.equals(entity.getEventName())) {
        selectList.add(entity);
      }
    }

    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByExecution(String executionId) {
    final String query = "selectSignalEventSubscriptionsByExecution";
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, executionId));

    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if (executionId.equals(entity.getExecutionId())) {
        selectList.add((SignalEventSubscriptionEntity) entity);
      }
    }

    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(String name, String executionId) {
    final String query = "selectSignalEventSubscriptionsByNameAndExecution";
    Map<String, String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventName", name);
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));

    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if (executionId.equals(entity.getExecutionId()) && name.equals(entity.getEventName())) {
        selectList.add(entity);
      }
    }

    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(String executionId, String type) {
    final String query = "selectEventSubscriptionsByExecutionAndType";
    Map<String, String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    Set<EventSubscriptionEntity> selectList = new HashSet<EventSubscriptionEntity>(getDbSqlSession().selectList(query, params));
    
    // add events created in this command (not visible yet in query)
    if ("signal".equals(type)) {
      for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
        if (executionId.equals(entity.getExecutionId())) {
          selectList.add(entity);
        }
      }
    } else if ("compensate".equals(type)) {
      for (CompensateEventSubscriptionEntity entity : createdCompensateSubscriptions) {
        if (executionId.equals(entity.getExecutionId())) {
          selectList.add(entity);
        }
      }
    }

    return new ArrayList<EventSubscriptionEntity>(selectList);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByProcessInstanceAndActivityId(String processInstanceId, String activityId, String type) {
    final String query = "selectEventSubscriptionsByProcessInstanceTypeAndActivity";
    Map<String, String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    Set<EventSubscriptionEntity> selectList = new HashSet<EventSubscriptionEntity>(getDbSqlSession().selectList(query, params));
    
    // add events created in this command (not visible yet in query)
    if ("signal".equals(type)) {
      for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
        if (processInstanceId.equals(entity.getProcessInstanceId()) && activityId.equals(entity.getActivityId())) {
          selectList.add(entity);
        }
      }
    } else if ("compensate".equals(type)) {
      for (CompensateEventSubscriptionEntity entity : createdCompensateSubscriptions) {
        if (processInstanceId.equals(entity.getProcessInstanceId()) && activityId.equals(entity.getActivityId())) {
          selectList.add(entity);
        }
      }
    }

    return new ArrayList<EventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecution(String executionId) {
    final String query = "selectEventSubscriptionsByExecution";
    Set<EventSubscriptionEntity> selectList = new HashSet<EventSubscriptionEntity>(getDbSqlSession().selectList(query, executionId));
    
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if (executionId.equals(entity.getExecutionId())) {
        selectList.add(entity);
      }
    }
    
    for (CompensateEventSubscriptionEntity entity : createdCompensateSubscriptions) {
      if (executionId.equals(entity.getExecutionId())) {
        selectList.add(entity);
      }
    }
    
    return new ArrayList<EventSubscriptionEntity>(selectList);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type, String activityId) {
    final String query = "selectEventSubscriptionsByExecutionTypeAndActivity";
    Map<String, String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    return getDbSqlSession().selectList(query, params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByConfiguration(String type, String configuration, String tenantId) {
    final String query = "selectEventSubscriptionsByConfiguration";
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("configuration", configuration);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    return getDbSqlSession().selectList(query, params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByName(String type, String eventName, String tenantId) {
    final String query = "selectEventSubscriptionsByName";
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    return getDbSqlSession().selectList(query, params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId) {
    final String query = "selectEventSubscriptionsByNameAndExecution";
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);
    params.put("executionId", executionId);
    return getDbSqlSession().selectList(query, params);
  }

  @Override
  public MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName, String tenantId) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventName", messageName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    MessageEventSubscriptionEntity entity = (MessageEventSubscriptionEntity) getDbSqlSession().selectOne("selectMessageStartEventSubscriptionByName", params);
    return entity;
  }

  @Override
  public void updateEventSubscriptionTenantId(String oldTenantId, String newTenantId) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("oldTenantId", oldTenantId);
    params.put("newTenantId", newTenantId);
    getDbSqlSession().update("updateTenantIdOfEventSubscriptions", params);
  }
  
  
  // Processing /////////////////////////////////////////////////////////////
  
  @Override
  public void eventReceived(EventSubscriptionEntity eventSubscriptionEntity, Object payload, boolean processASync) {
    if (processASync) {
      scheduleEventAsync(eventSubscriptionEntity, payload);
    } else {
      processEventSync(eventSubscriptionEntity, payload);
    }
  }

  protected void processEventSync(EventSubscriptionEntity eventSubscriptionEntity, Object payload) {
    
    // A compensate event needs to be deleted before the handlers are called
    if (eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
      delete(eventSubscriptionEntity);
    }
    
    EventHandler eventHandler = Context.getProcessEngineConfiguration().getEventHandler(eventSubscriptionEntity.getEventType());
    if (eventHandler == null) {
      throw new ActivitiException("Could not find eventhandler for event of type '" + eventSubscriptionEntity.getEventType() + "'.");
    }
    eventHandler.handleEvent(eventSubscriptionEntity, payload, Context.getCommandContext());
  }

  protected void scheduleEventAsync(EventSubscriptionEntity eventSubscriptionEntity, Object payload) {

    final CommandContext commandContext = Context.getCommandContext();

    MessageEntity message = new MessageEntity();
    message.setJobHandlerType(ProcessEventJobHandler.TYPE);
    message.setJobHandlerConfiguration(eventSubscriptionEntity.getId());
    message.setTenantId(eventSubscriptionEntity.getTenantId());

    GregorianCalendar expireCal = new GregorianCalendar();
    ProcessEngineConfiguration processEngineConfig = Context.getCommandContext().getProcessEngineConfiguration();
    expireCal.setTime(processEngineConfig.getClock().getCurrentTime());
    expireCal.add(Calendar.SECOND, processEngineConfig.getLockTimeAsyncJobWaitTime());
    message.setLockExpirationTime(expireCal.getTime());

    // TODO: support payload
    // if(payload != null) {
    // message.setEventPayload(payload);
    // }

    commandContext.getJobEntityManager().send(message);
  }
  

}
