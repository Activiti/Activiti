package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.data.DataManager;

/**
 * @author Joram Barrez
 */
public abstract class AbstractEntityManager<EntityImpl extends Entity> extends AbstractManager implements EntityManager<EntityImpl> {

  /*
   * CRUD operations
   */
  
  @Override
  public EntityImpl findById(String entityId) {
    return getDataManager().findById(entityId);
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

}
