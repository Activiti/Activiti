package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;

/**
 * @author Joram Barrez
 */
public class AbstractEntityManager<Entity extends PersistentObject> extends AbstractManager implements EntityManager<Entity> {

  public Class<Entity> getManagedPersistentObject() {
    // Cannot make abstract cause some managers don't use db persistence (eg ldap)
    throw new UnsupportedOperationException();
  }
  
  public List<Class<? extends Entity>> getManagedPersistentObjectSubClasses() {
    return null;
  }
  
  /*
   * CRUD operations
   */

  @Override
  public void insert(Entity entity) {
    insert(entity, true);
  }

  @Override
  public void insert(Entity entity, boolean fireCreateEvent) {
    getDbSqlSession().insert(entity);

    if (fireCreateEvent && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, entity));
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, entity));
    }
  }
  
  @Override
  public void delete(String id) {
    Entity entity = get(id);
    delete(entity);
  }
  
  @Override
  public void delete(Entity entity) {
    delete(entity, true);
  }

  @Override
  public void delete(Entity entity, boolean fireDeleteEvent) {
    getDbSqlSession().delete(entity);

    if (fireDeleteEvent && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher()
        .dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, entity));
    }
  }

  @Override
  public Entity get(String persistentObjectId) {
    return getDbSqlSession().selectById(getManagedPersistentObject(), persistentObjectId);
  }

  /*
   * Advanced operations
   */

  @Override
  public Entity getEntity(Class<? extends Entity> clazz, String entityId, CachedEntityMatcher<Entity> cachedEntityMatcher) {

    // Cache
    for (Entity cachedEntity : getDbSqlSession().findInCache(getManagedPersistentObject())) {
      if (cachedEntityMatcher.isRetained(cachedEntity)) {
        return cachedEntity;
      }
    }

    // Database
    return getDbSqlSession().selectById(clazz, entityId);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Entity getEntity(Class<? extends Entity> clazz, String selectQuery, Object parameter, CachedEntityMatcher<Entity> cachedEntityMatcher) {
    // Cache
    for (Entity cachedEntity : getDbSqlSession().findInCache(getManagedPersistentObject())) {
      if (cachedEntityMatcher.isRetained(cachedEntity)) {
        return cachedEntity;
      }
    }

    // Database
    return (Entity) getDbSqlSession().selectOne(selectQuery, parameter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Entity> getList(String dbQueryName, Object parameter, CachedEntityMatcher<Entity> retainEntityCondition) {
    HashMap<String, Entity> entityMap = new HashMap<String, Entity>();

    // Database
    List<Entity> entitiesFromDb = getDbSqlSession().selectList(dbQueryName, parameter);
    for (Entity entity : entitiesFromDb) {
      entityMap.put(entity.getId(), entity);
    }

    // Cache
    for (Entity cachedEntity : getDbSqlSession().findInCache(getManagedPersistentObject())) {
      if (retainEntityCondition.isRetained(cachedEntity)) {
        entityMap.put(cachedEntity.getId(), cachedEntity);
      }
    }
    
    if (getManagedPersistentObjectSubClasses() != null) {
      for (Class<? extends Entity> entitySubClass : getManagedPersistentObjectSubClasses()) {
        for (Entity cachedEntity : getDbSqlSession().findInCache(entitySubClass)) {
          if (retainEntityCondition.isRetained(cachedEntity)) {
            entityMap.put(cachedEntity.getId(), cachedEntity);
          }
        }
      }
    }

    // Remove any entries which are already deleted
    Collection<Entity> result = entityMap.values();
    if (result.size() > 0) {
      Iterator<Entity> resultIterator = result.iterator();
      while (resultIterator.hasNext()) {
        if (getDbSqlSession().isPersistentObjectDeleted(resultIterator.next())) {
          resultIterator.remove();
        }
      }
    }

    return new ArrayList<Entity>(result);
  }

}
