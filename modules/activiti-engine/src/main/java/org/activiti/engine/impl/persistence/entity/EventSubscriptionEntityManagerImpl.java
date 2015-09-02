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
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class EventSubscriptionEntityManagerImpl extends AbstractEntityManager<EventSubscriptionEntity> implements EventSubscriptionEntityManager {
  
  @SuppressWarnings("unchecked")
  private static final List<Class<? extends EventSubscriptionEntity>> ENTITY_SUBCLASSES = 
      Arrays.asList(MessageEventSubscriptionEntity.class, SignalEventSubscriptionEntity.class, CompensateEventSubscriptionEntity.class);
  
  @Override
  public Class<EventSubscriptionEntity> getManagedEntity() {
    return EventSubscriptionEntity.class;
  }
  
  @Override
  public List<Class<? extends EventSubscriptionEntity>> getManagedEntitySubClasses() {
    return ENTITY_SUBCLASSES;
  }

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
  
  protected void addToExecution(EventSubscriptionEntity eventSubscriptionEntity) {
    // add reference in execution
    ExecutionEntity execution = eventSubscriptionEntity.getExecution();
    if (execution != null) {
      execution.getEventSubscriptions().add(eventSubscriptionEntity);
    }
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
  public List<MessageEventSubscriptionEntity> findMessageEventSubscriptionsByProcessInstanceAndEventName(final String processInstanceId, final String eventName) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventName", eventName);
    return toMessageEventSubscriptionEntityList(getList("selectMessageEventSubscriptionsByProcessInstanceAndEventName", params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(MessageEventSubscriptionEntity.EVENT_TYPE)
            && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(eventName)
            && eventSubscriptionEntity.getProcessInstanceId() != null && eventSubscriptionEntity.getProcessInstanceId().equals(processInstanceId);
      }
      
    }, true));
    
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(final String eventName, final String tenantId) {
    final String query = "selectSignalEventSubscriptionsByEventName";

    final Map<String, String> params = new HashMap<String, String>();
    params.put("eventName", eventName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    
    List<EventSubscriptionEntity> result = getList(query, params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(SignalEventSubscriptionEntity.EVENT_TYPE)
            && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(eventName)
            && (eventSubscriptionEntity.getExecutionId() == null || (eventSubscriptionEntity.getExecutionId() != null && eventSubscriptionEntity.getExecution() != null && eventSubscriptionEntity.getExecution().getSuspensionState() == SuspensionState.ACTIVE.getStateCode()) )
            && ( (params.containsKey("tenantId") && tenantId.equals(eventSubscriptionEntity.getTenantId())) || (!params.containsKey("tenantId") && StringUtils.isEmpty(eventSubscriptionEntity.getTenantId())) );
      }
    }, true);
    
    return toSignalEventSubscriptionEntityList(result);
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByProcessInstanceAndEventName(final String processInstanceId, final String eventName) {
    final String query = "selectSignalEventSubscriptionsByProcessInstanceAndEventName";
    Map<String, String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventName", eventName);
    
    return toSignalEventSubscriptionEntityList(getList(query, params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(SignalEventSubscriptionEntity.EVENT_TYPE)
            && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(eventName)
            && eventSubscriptionEntity.getProcessInstanceId() != null && eventSubscriptionEntity.getProcessInstanceId().equals(processInstanceId);
      }
      
    }, true));
    
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByExecution(final String executionId) {
    return toSignalEventSubscriptionEntityList(getList("selectSignalEventSubscriptionsByExecution", executionId, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(SignalEventSubscriptionEntity.EVENT_TYPE)
            &&  eventSubscriptionEntity.getExecutionId() != null && eventSubscriptionEntity.getExecutionId().equals(executionId);
      }
      
    }, true));
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(final String name, final String executionId) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventName", name);
    return toSignalEventSubscriptionEntityList(getList("selectSignalEventSubscriptionsByNameAndExecution", params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(SignalEventSubscriptionEntity.EVENT_TYPE)
            && eventSubscriptionEntity.getExecutionId() != null && eventSubscriptionEntity.getExecutionId().equals(executionId)
            && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(name);
      }
      
    }, true));
    
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(final String executionId, final String type) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    
    return getList("selectEventSubscriptionsByExecutionAndType", params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(type)
            && eventSubscriptionEntity.getExecutionId() != null && eventSubscriptionEntity.getExecutionId().equals(executionId);
      }
      
    }, true);
  }
  
  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByProcessInstanceAndActivityId(final String processInstanceId, final String activityId, final String type) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    
    return getList("selectEventSubscriptionsByProcessInstanceTypeAndActivity", params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(type)
            && eventSubscriptionEntity.getProcessInstanceId() != null && eventSubscriptionEntity.getProcessInstanceId().equals(processInstanceId)
            && eventSubscriptionEntity.getActivityId() != null && eventSubscriptionEntity.getActivityId().equals(activityId);
      }
      
    }, true);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecution(final String executionId) {
    return getList("selectEventSubscriptionsByExecution", executionId, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        return eventSubscriptionEntity.getExecutionId() != null && eventSubscriptionEntity.getExecutionId().equals(executionId);
      }
      
    }, true);
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
  public List<EventSubscriptionEntity> findEventSubscriptionsByName(final String type, final String eventName, final String tenantId) {

    Map<String, String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    
    return getList("selectEventSubscriptionsByName", params, new CachedEntityMatcher<EventSubscriptionEntity>() {
      
      @Override
      public boolean isRetained(EventSubscriptionEntity eventSubscriptionEntity) {
        if (eventSubscriptionEntity.getEventType() != null && eventSubscriptionEntity.getEventType().equals(type)
            && eventSubscriptionEntity.getEventName() != null && eventSubscriptionEntity.getEventName().equals(eventName)) {
          if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
            return eventSubscriptionEntity.getTenantId() != null && eventSubscriptionEntity.getTenantId().equals(tenantId);
          } else {
            return ProcessEngineConfiguration.NO_TENANT_ID.equals(eventSubscriptionEntity.getTenantId()) || eventSubscriptionEntity.getTenantId() == null;
          }
        }
        return false;
      }
      
    }, true);
    
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
    
    EventHandler eventHandler = getProcessEngineConfiguration().getEventHandler(eventSubscriptionEntity.getEventType());
    if (eventHandler == null) {
      throw new ActivitiException("Could not find eventhandler for event of type '" + eventSubscriptionEntity.getEventType() + "'.");
    }
    eventHandler.handleEvent(eventSubscriptionEntity, payload, getCommandContext());
  }

  protected void scheduleEventAsync(EventSubscriptionEntity eventSubscriptionEntity, Object payload) {

    MessageEntity message = new MessageEntity();
    message.setJobHandlerType(ProcessEventJobHandler.TYPE);
    message.setJobHandlerConfiguration(eventSubscriptionEntity.getId());
    message.setTenantId(eventSubscriptionEntity.getTenantId());

    GregorianCalendar expireCal = new GregorianCalendar();
    ProcessEngineConfiguration processEngineConfig = getProcessEngineConfiguration();
    expireCal.setTime(processEngineConfig.getClock().getCurrentTime());
    expireCal.add(Calendar.SECOND, processEngineConfig.getLockTimeAsyncJobWaitTime());
    message.setLockExpirationTime(expireCal.getTime());

    // TODO: support payload
    // if(payload != null) {
    // message.setEventPayload(payload);
    // }

    getJobEntityManager().send(message);
  }
  
  @Override
  public void deleteEventSubscriptionsForProcessDefinition(String processDefinitionId) {
    getDbSqlSession().delete("deleteEventSubscriptionsForProcessDefinition", processDefinitionId);
  }
  
  protected List<SignalEventSubscriptionEntity> toSignalEventSubscriptionEntityList(List<EventSubscriptionEntity> result) {
    List<SignalEventSubscriptionEntity> signalEventSubscriptionEntities = new ArrayList<SignalEventSubscriptionEntity>(result.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : result ) {
      signalEventSubscriptionEntities.add((SignalEventSubscriptionEntity) eventSubscriptionEntity);
    }
    return signalEventSubscriptionEntities;
  }
  
  protected List<MessageEventSubscriptionEntity> toMessageEventSubscriptionEntityList(List<EventSubscriptionEntity> result) {
    List<MessageEventSubscriptionEntity> messageEventSubscriptionEntities = new ArrayList<MessageEventSubscriptionEntity>(result.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : result ) {
      messageEventSubscriptionEntities.add((MessageEventSubscriptionEntity) eventSubscriptionEntity);
    }
    return messageEventSubscriptionEntities;
  }
  

}
