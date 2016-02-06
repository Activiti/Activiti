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

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class EventSubscriptionEntityManager extends AbstractManager {
  
  /** keep track of subscriptions created in the current command */
  protected List<SignalEventSubscriptionEntity> createdSignalSubscriptions = new ArrayList<SignalEventSubscriptionEntity>();
  
  public void insert(EventSubscriptionEntity persistentObject) {
    super.insert(persistentObject);
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.add((SignalEventSubscriptionEntity)persistentObject);
    }
  }
  
  public void deleteEventSubscription(EventSubscriptionEntity persistentObject) {
    getDbSqlSession().delete(persistentObject);
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
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
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName, String tenantId) {
    final String query = "selectSignalEventSubscriptionsByEventName";    
    
    Set<SignalEventSubscriptionEntity> selectList = null;
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventName", eventName);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
    	params.put("tenantId", tenantId);
      selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));
    } else {
    	selectList = new HashSet<SignalEventSubscriptionEntity>(getDbSqlSession().selectList(query, params));
    }
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(eventName.equals(entity.getEventName())) {
        selectList.add(entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByProcessInstanceAndEventName(String processInstanceId, String eventName) {
    final String query = "selectSignalEventSubscriptionsByProcessInstanceAndEventName"; 
    Map<String,String> params = new HashMap<String, String>();
    params.put("processInstanceId", processInstanceId);
    params.put("eventName", eventName);    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, params));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(processInstanceId.equals(entity.getProcessInstanceId()) && eventName.equals(entity.getEventName())) {
        selectList.add(entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }  
  
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByExecution(String executionId) {
    final String query = "selectSignalEventSubscriptionsByExecution";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, executionId));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(executionId.equals(entity.getExecutionId())) {
        selectList.add((SignalEventSubscriptionEntity) entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(String name, String executionId) {
    final String query = "selectSignalEventSubscriptionsByNameAndExecution";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventName", name);    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, params));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(executionId.equals(entity.getExecutionId())
         && name.equals(entity.getEventName())) {
        selectList.add((SignalEventSubscriptionEntity) entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  public List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(String executionId, String type) {
    final String query = "selectEventSubscriptionsByExecutionAndType";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);    
    return getDbSqlSession().selectList(query, params);    
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecution(String executionId) {
    final String query = "selectEventSubscriptionsByExecution";    
    return getDbSqlSession().selectList(query, executionId);    
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type, String activityId) {
    final String query = "selectEventSubscriptionsByExecutionTypeAndActivity";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    return getDbSqlSession().selectList(query, params);            
  }

  public List<EventSubscriptionEntity> findEventSubscriptionsByConfiguration(String type, String configuration, String tenantId) {
    final String query = "selectEventSubscriptionsByConfiguration";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("configuration", configuration);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
    	params.put("tenantId", tenantId);
    }
    return getDbSqlSession().selectList(query, params);            
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptionsByTypeAndProcessDefinitionId(String type, String processDefinitionId, String tenantId) {
    final String query = "selectEventSubscriptionsByTypeAndProcessDefinitionId";    
    Map<String,String> params = new HashMap<String, String>();
    if (type != null) {
      params.put("eventType", type);
    }
    params.put("processDefinitionId", processDefinitionId);
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      params.put("tenantId", tenantId);
    }
    return getDbSqlSession().selectList(query, params);            
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptionsByName(String type, String eventName, String tenantId) {
    final String query = "selectEventSubscriptionsByName";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);  
    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
    	params.put("tenantId", tenantId);
    }
    return getDbSqlSession().selectList(query, params);            
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId) {
    final String query = "selectEventSubscriptionsByNameAndExecution";    
    Map<String,String> params = new HashMap<String, String>();
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
