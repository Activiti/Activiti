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

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.DataManager;

/**

 */
public abstract class AbstractEntityManager<EntityImpl extends Entity> extends AbstractManager implements EntityManager<EntityImpl> {

  public AbstractEntityManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }
  
  /*
   * CRUD operations
   */
  

  @Override
  public EntityImpl findById(String entityId) {
    return getDataManager().findById(entityId);
  }
  
  @Override
  public EntityImpl create() {
    return getDataManager().create();
  }

  @Override
  public void insert(EntityImpl entity) {
    insert(entity, true);
  }

  @Override
  public void insert(EntityImpl entity, boolean fireCreateEvent) {
    getDataManager().insert(entity);

    ActivitiEventDispatcher eventDispatcher = getEventDispatcher();
    if (fireCreateEvent && eventDispatcher.isEnabled()) {
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, entity));
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, entity));
    }
  }
  
  @Override
  public EntityImpl update(EntityImpl entity) {
    return update(entity, true);
  }
  
  @Override
  public EntityImpl update(EntityImpl entity, boolean fireUpdateEvent) {
    EntityImpl updatedEntity = getDataManager().update(entity);
    
    if (fireUpdateEvent && getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, entity));
    }
    
    return updatedEntity;
  }
  
  @Override
  public void delete(String id) {
    EntityImpl entity = findById(id);
    delete(entity);
  }
  
  @Override
  public void delete(EntityImpl entity) {
    delete(entity, true);
  }

  @Override
  public void delete(EntityImpl entity, boolean fireDeleteEvent) {
    getDataManager().delete(entity);

    if (fireDeleteEvent && getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, entity));
    }
  }
  
  protected abstract DataManager<EntityImpl> getDataManager();
  
  /* Execution related entity count methods */
  
  protected boolean isExecutionRelatedEntityCountEnabledGlobally() {
    return processEngineConfiguration.getPerformanceSettings().isEnableExecutionRelationshipCounts();
  }
  
  protected boolean isExecutionRelatedEntityCountEnabled(ExecutionEntity executionEntity) {
    if (executionEntity instanceof CountingExecutionEntity) {
      return isExecutionRelatedEntityCountEnabled((CountingExecutionEntity) executionEntity);
    }
    return false;
  }
  
  protected boolean isExecutionRelatedEntityCountEnabled(CountingExecutionEntity executionEntity) {
    
    /*
     * There are two flags here: a global flag and a flag on the execution entity.
     * The global flag can be switched on and off between different reboots,
     * however the flag on the executionEntity refers to the state at that particular moment.
     * 
     * Global flag / ExecutionEntity flag : result
     * 
     * T / T : T (all true, regular mode with flags enabled)
     * T / F : F (global is true, but execution was of a time when it was disabled, thus treating it as disabled)
     * F / T : F (execution was of time when counting was done. But this is overruled by the global flag and thus the queries will be done)
     * F / F : F (all disabled)
     * 
     * From this table it is clear that only when both are true, the result should be true,
     * which is the regular AND rule for booleans.
     */
    
    return isExecutionRelatedEntityCountEnabledGlobally() && executionEntity.isCountEnabled();
  }
  

}
