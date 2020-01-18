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
import java.util.List;

import org.activiti.bpmn.model.Signal;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.EventSubscriptionDataManager;

/**


 */
public class EventSubscriptionEntityManagerImpl extends AbstractEntityManager<EventSubscriptionEntity> implements EventSubscriptionEntityManager {
  
  protected EventSubscriptionDataManager eventSubscriptionDataManager;
  
  public EventSubscriptionEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, EventSubscriptionDataManager eventSubscriptionDataManager) {
    super(processEngineConfiguration);
    this.eventSubscriptionDataManager = eventSubscriptionDataManager;
  }
  
  @Override
  protected DataManager<EventSubscriptionEntity> getDataManager() {
    return eventSubscriptionDataManager;
  }
  
  @Override
  public CompensateEventSubscriptionEntity createCompensateEventSubscription() {
    return eventSubscriptionDataManager.createCompensateEventSubscription();
  }
  
  @Override
  public MessageEventSubscriptionEntity createMessageEventSubscription() {
    return eventSubscriptionDataManager.createMessageEventSubscription();
  }
  
  @Override
  public SignalEventSubscriptionEntity createSignalEventSubscription() {
    return eventSubscriptionDataManager.createSignalEventSubscription();
  }
  
  @Override
  public SignalEventSubscriptionEntity insertSignalEvent(String signalName, Signal signal, ExecutionEntity execution) {
    SignalEventSubscriptionEntity subscriptionEntity = createSignalEventSubscription();
    subscriptionEntity.setExecution(execution);
    if (signal != null) {
      subscriptionEntity.setEventName(signal.getName());
      if (signal.getScope() != null) {
        subscriptionEntity.setConfiguration(signal.getScope());
      }
    } else {
      subscriptionEntity.setEventName(signalName);
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
  public MessageEventSubscriptionEntity insertMessageEvent(String messageName, ExecutionEntity execution) {
    MessageEventSubscriptionEntity subscriptionEntity = createMessageEventSubscription();
    subscriptionEntity.setExecution(execution);
    subscriptionEntity.setEventName(messageName);

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
    CompensateEventSubscriptionEntity eventSubscription = createCompensateEventSubscription();
    eventSubscription.setExecution(execution);
    eventSubscription.setActivityId(activityId);
    if (execution.getTenantId() != null) {
      eventSubscription.setTenantId(execution.getTenantId());
    }
    insert(eventSubscription);
    return eventSubscription;
  }
  
  @Override
  public void insert(EventSubscriptionEntity entity, boolean fireCreateEvent) {
    super.insert(entity, fireCreateEvent);
    
    if (entity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
      CountingExecutionEntity executionEntity = (CountingExecutionEntity) entity.getExecution();
      if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
        executionEntity.setEventSubscriptionCount(executionEntity.getEventSubscriptionCount() + 1);
      }
    }
  }
  
  @Override
  public void delete(EventSubscriptionEntity entity, boolean fireDeleteEvent) {
    if (entity.getExecutionId() != null && isExecutionRelatedEntityCountEnabledGlobally()) {
      CountingExecutionEntity executionEntity = (CountingExecutionEntity) entity.getExecution();
      if (isExecutionRelatedEntityCountEnabled(executionEntity)) {
        executionEntity.setEventSubscriptionCount(executionEntity.getEventSubscriptionCount() - 1);
      }
    }
    super.delete(entity, fireDeleteEvent);
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
    return eventSubscriptionDataManager.findEventSubscriptionCountByQueryCriteria(eventSubscriptionQueryImpl);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page) {
    return eventSubscriptionDataManager.findEventSubscriptionsByQueryCriteria(eventSubscriptionQueryImpl, page);
  }
  
  @Override
  public List<MessageEventSubscriptionEntity> findMessageEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName) {
    return eventSubscriptionDataManager.findMessageEventSubscriptionsByProcessInstanceAndEventName(processInstanceId, eventName);
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName, String tenantId) {
    return eventSubscriptionDataManager.findSignalEventSubscriptionsByEventName(eventName, tenantId);
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName) {
    return eventSubscriptionDataManager.findSignalEventSubscriptionsByProcessInstanceAndEventName(processInstanceId, eventName);
  }

  @Override
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(String name, String executionId) {
    return eventSubscriptionDataManager.findSignalEventSubscriptionsByNameAndExecution(name, executionId);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(final String executionId, final String type) {
    return eventSubscriptionDataManager.findEventSubscriptionsByExecutionAndType(executionId, type);
  }
  
  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByProcessInstanceAndActivityId(String processInstanceId, String activityId, String type) {
    return eventSubscriptionDataManager.findEventSubscriptionsByProcessInstanceAndActivityId(processInstanceId, activityId, type);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecution(final String executionId) {
    return eventSubscriptionDataManager.findEventSubscriptionsByExecution(executionId);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByTypeAndProcessDefinitionId(String type, String processDefinitionId, String tenantId) {
    return eventSubscriptionDataManager.findEventSubscriptionsByTypeAndProcessDefinitionId(type, processDefinitionId, tenantId);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByName(String type, String eventName, String tenantId) {
    return eventSubscriptionDataManager.findEventSubscriptionsByName(type, eventName, tenantId);
  }

  @Override
  public List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId) {
    return eventSubscriptionDataManager.findEventSubscriptionsByNameAndExecution(type, eventName, executionId);
  }

  @Override
  public MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName, String tenantId) {
    return eventSubscriptionDataManager.findMessageStartEventSubscriptionByName(messageName, tenantId);
  }

  @Override
  public void updateEventSubscriptionTenantId(String oldTenantId, String newTenantId) {
    eventSubscriptionDataManager.updateEventSubscriptionTenantId(oldTenantId, newTenantId);
  }
  
  @Override
  public void deleteEventSubscriptionsForProcessDefinition(String processDefinitionId) {
    eventSubscriptionDataManager.deleteEventSubscriptionsForProcessDefinition(processDefinitionId);
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
    JobEntity message = getJobEntityManager().create();
    message.setJobType(JobEntity.JOB_TYPE_MESSAGE);
    message.setJobHandlerType(ProcessEventJobHandler.TYPE);
    message.setJobHandlerConfiguration(eventSubscriptionEntity.getId());
    message.setTenantId(eventSubscriptionEntity.getTenantId());

    // TODO: support payload
    // if(payload != null) {
    // message.setEventPayload(payload);
    // }

    getJobManager().scheduleAsyncJob(message);
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

  public EventSubscriptionDataManager getEventSubscriptionDataManager() {
    return eventSubscriptionDataManager;
  }

  public void setEventSubscriptionDataManager(EventSubscriptionDataManager eventSubscriptionDataManager) {
    this.eventSubscriptionDataManager = eventSubscriptionDataManager;
  }
  

}
