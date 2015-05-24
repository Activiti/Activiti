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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class EventSubscriptionEntityManager extends AbstractEntityManager<EventSubscriptionEntity> {

  /** keep track of subscriptions created in the current command */
  protected List<SignalEventSubscriptionEntity> createdSignalSubscriptions = new ArrayList<SignalEventSubscriptionEntity>();
  protected List<CompensateEventSubscriptionEntity> createdCompensateSubscriptions = new ArrayList<CompensateEventSubscriptionEntity>();

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
    execution.getEventSubscriptionsInternal().add(subscriptionEntity);
    createdSignalSubscriptions.add(subscriptionEntity);
    return subscriptionEntity;
  }

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
    execution.getEventSubscriptionsInternal().add(subscriptionEntity);
    return subscriptionEntity;
  }
  
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
  
  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions(String executionId) {
    return getCompensateEventSubscriptions(executionId, null);
  }

  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions(String executionId, String activityId) {
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
  
  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptionsForProcessInstanceId(String processInstanceId, String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = selectEventSubscriptionsByProcessInstanceAndActivity(processInstanceId, activityId, "compensate");
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>();
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
    }
    return result;
  }

  public void deleteEventSubscription(EventSubscriptionEntity persistentObject) {
    getDbSqlSession().delete(persistentObject);
    if (persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.remove(persistentObject);
    }
  }

  public void deleteEventSubscriptionsForProcessDefinition(String processDefinitionId) {
    getDbSqlSession().delete("deleteEventSubscriptionsForProcessDefinition", processDefinitionId);
  }

  public EventSubscriptionEntity findEventSubscriptionbyId(String id) {
    return (EventSubscriptionEntity) getDbSqlSession().selectOne("selectEventSubscription", id);
  }

  public long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl) {
    final String query = "selectEventSubscriptionCountByQueryCriteria";
    return (Long) getDbSqlSession().selectOne(query, eventSubscriptionQueryImpl);
  }

  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page) {
    final String query = "selectEventSubscriptionByQueryCriteria";
    return getDbSqlSession().selectList(query, eventSubscriptionQueryImpl, page);
  }
  
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
  
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> selectEventSubscriptionsByProcessInstanceAndActivity(String processInstanceId, String activityId, String type) {
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

  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type, String activityId) {
    final String query = "selectEventSubscriptionsByExecutionTypeAndActivity";
    Map<String, String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    return getDbSqlSession().selectList(query, params);
  }

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

  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId) {
    final String query = "selectEventSubscriptionsByNameAndExecution";
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);
    params.put("executionId", executionId);
    return getDbSqlSession().selectList(query, params);
  }

  public MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName, String tenantId) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("eventName", messageName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    MessageEventSubscriptionEntity entity = (MessageEventSubscriptionEntity) getDbSqlSession().selectOne("selectMessageStartEventSubscriptionByName", params);
    return entity;
  }

  public void updateEventSubscriptionTenantId(String oldTenantId, String newTenantId) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("oldTenantId", oldTenantId);
    params.put("newTenantId", newTenantId);
    getDbSqlSession().update("updateTenantIdOfEventSubscriptions", params);
  }

}
