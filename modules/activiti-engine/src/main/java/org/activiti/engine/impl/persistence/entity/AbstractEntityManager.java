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
import org.activiti.engine.impl.persistence.CachedPersistentObjectMatcher;
import org.activiti.engine.impl.persistence.cache.CachedPersistentObject;

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
    Entity entity = getEntity(id);
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

  /*
   * Advanced operations
   */

  @Override
  public Entity getEntity(String entityId) {

    // Cache
    for (Entity cachedEntity : getPersistentObjectCache().findInCache(getManagedPersistentObject())) {
      if (entityId.equals(cachedEntity.getId())) {
        return cachedEntity;
      }
    }

    // Database
    return getDbSqlSession().selectById(getManagedPersistentObject(), entityId);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Entity getEntity(String selectQuery, Object parameter, CachedPersistentObjectMatcher<Entity> cachedEntityMatcher) {
    // Cache
    for (Entity cachedEntity : getPersistentObjectCache().findInCache(getManagedPersistentObject())) {
      if (cachedEntityMatcher.isRetained(cachedEntity)) {
        return cachedEntity;
      }
    }

    // Database
    return (Entity) getDbSqlSession().selectOne(selectQuery, parameter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Entity> getList(String dbQueryName, Object parameter, CachedPersistentObjectMatcher<Entity> retainEntityCondition, boolean checkCache) {

    Collection<Entity> result = getDbSqlSession().selectList(dbQueryName, parameter);
    
    if (checkCache) {
      
      Collection<CachedPersistentObject> cachedObjects = getPersistentObjectCache().findInCacheAsCachedObjects(getManagedPersistentObject());
      
      if ( (cachedObjects != null && cachedObjects.size() > 0) || getManagedPersistentObjectSubClasses() != null) {
        
        HashMap<String, Entity> entityMap = new HashMap<String, Entity>(result.size());
        
        // Database entities
        for (Entity entity : result) {
          entityMap.put(entity.getId(), entity);
        }

        // Cache entities
        if (cachedObjects != null) {
          for (CachedPersistentObject cachedObject : cachedObjects) {
            Entity cachedEntity = (Entity) cachedObject.getPersistentObject();
            if (retainEntityCondition.isRetained(cachedEntity)) {
              entityMap.put(cachedEntity.getId(), cachedEntity); // will overwite db version with newer version
            }
          }
        }
        
        if (getManagedPersistentObjectSubClasses() != null) {
          for (Class<? extends Entity> entitySubClass : getManagedPersistentObjectSubClasses()) {
            Collection<CachedPersistentObject> subclassCachedObjects = getPersistentObjectCache().findInCacheAsCachedObjects(entitySubClass);
            if (subclassCachedObjects != null) {
              for (CachedPersistentObject subclassCachedObject : subclassCachedObjects) {
                Entity cachedSubclassEntity = (Entity) subclassCachedObject.getPersistentObject();
                if (retainEntityCondition.isRetained(cachedSubclassEntity)) {
                  entityMap.put(cachedSubclassEntity.getId(), cachedSubclassEntity); // will overwite db version with newer version
                }
              }
            }
          }
        }
        
        result = entityMap.values();
        
      }
      
    }
    
    // Remove entries which are already deleted
    if (result.size() > 0) {
      Iterator<Entity> resultIterator = result.iterator();
      while (resultIterator.hasNext()) {
        if (getDbSqlSession().isPersistentObjectToBeDeleted(resultIterator.next())) {
          resultIterator.remove();
        }
      }
    }

    return new ArrayList<Entity>(result);
  }
  
}
