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

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.runtime.EventSubscription;


/**
 * @author Daniel Meyer
 */
public class EventSubscriptionManager extends AbstractManager {
  
  /** keep track of subscriptions created in the current command */
  protected List<EventSubscriptionEntity> createdSignalSubscriptions = new ArrayList<EventSubscriptionEntity>();
  
  public void insert(EventSubscriptionEntity persistentObject) {
    super.insert(persistentObject);
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.add(persistentObject);
    }
  }
  
  public void deleteEventSubscription(EventSubscriptionEntity persistentObject) {
    getDbSqlSession().delete(persistentObject.getClass(), persistentObject.getId());
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.remove(persistentObject);
    }
  }
    
  public EventSubscriptionEntity findEventSubscriptionbyId(String id) {
    return (EventSubscriptionEntity) getDbSqlSession().selectOne("selectEventSubscription", id);
  }

  public long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl) {
    final String query = "selectEventSubscriptionCountByQueryCriteria"; 
    return (Long) getDbSqlSession().selectOne(query, eventSubscriptionQueryImpl);
  }

  @SuppressWarnings("unchecked")
  public List<EventSubscription> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page) {
    final String query = "selectEventSubscriptionByQueryCriteria"; 
    return getDbSqlSession().selectList(query, eventSubscriptionQueryImpl, page);
  }

  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName) {
    final String query = "selectSignalEventSubscriptionsByEventName";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, eventName));
    
    // add events created in this command (not visible yet in query)
    for (EventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(entity instanceof SignalEventSubscriptionEntity
         && eventName.equals(entity.getEventName())) {
        selectList.add((SignalEventSubscriptionEntity) entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByExecution(String executionId) {
    final String query = "selectSignalEventSubscriptionsByExecution";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, executionId));
    
    // add events created in this command (not visible yet in query)
    for (EventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(entity instanceof SignalEventSubscriptionEntity) {
        selectList.add((SignalEventSubscriptionEntity) entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type) {
    final String query = "selectEventSubscriptionsByExecutionAndType";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);    
    return getDbSqlSession().selectList(query, params);    
  }
  
  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type, String activityId) {
    final String query = "selectEventSubscriptionsByExecutionTypeAndActivity";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    return getDbSqlSession().selectList(query, params);            
  }
   
}
