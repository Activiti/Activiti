package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.cache.CachedEntity;

/**
 * @author Joram Barrez
 */
public class AbstractEntityManager<EntityImpl extends Entity> extends AbstractManager implements EntityManager<EntityImpl> {

  public Class<EntityImpl> getManagedEntity() {
    // Cannot make abstract cause some managers don't use db persistence (eg ldap)
    throw new UnsupportedOperationException();
  }
  
  public List<Class<? extends EntityImpl>> getManagedEntitySubClasses() {
    return null;
  }
  
  /*
   * CRUD operations
   */

  @Override
  public void insert(EntityImpl entity) {
    insert(entity, true);
  }

  @Override
  public void insert(EntityImpl entity, boolean fireCreateEvent) {
    getDbSqlSession().insert(entity);

    ActivitiEventDispatcher eventDispatcher = getEventDispatcher();
    if (fireCreateEvent && eventDispatcher.isEnabled()) {
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, entity));
      eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, entity));
    }
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
    getDbSqlSession().delete(entity);

    if (fireDeleteEvent && getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, entity));
    }
  }

  /*
   * Advanced operations
   */

  @Override
  public EntityImpl findById(String entityId) {

    // Cache
    EntityImpl cachedEntity = getEntityCache().findInCache(getManagedEntity(), entityId);
    if (cachedEntity != null) {
      return cachedEntity;
    }
    
    // Database
    return getDbSqlSession().selectById(getManagedEntity(), entityId);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public EntityImpl findByQuery(String selectQuery, Object parameter, CachedEntityMatcher<EntityImpl> cachedEntityMatcher) {
    // Cache
    for (EntityImpl cachedEntity : getEntityCache().findInCache(getManagedEntity())) {
      if (cachedEntityMatcher.isRetained(cachedEntity)) {
        return cachedEntity;
      }
    }

    // Database
    return (EntityImpl) getDbSqlSession().selectOne(selectQuery, parameter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EntityImpl> getList(String dbQueryName, Object parameter, CachedEntityMatcher<EntityImpl> retainEntityCondition, boolean checkCache) {

    Collection<EntityImpl> result = getDbSqlSession().selectList(dbQueryName, parameter);
    
    if (checkCache) {
      
      Collection<CachedEntity> cachedObjects = getEntityCache().findInCacheAsCachedObjects(getManagedEntity());
      
      if ( (cachedObjects != null && cachedObjects.size() > 0) || getManagedEntitySubClasses() != null) {
        
        HashMap<String, EntityImpl> entityMap = new HashMap<String, EntityImpl>(result.size());
        
        // Database entities
        for (EntityImpl entity : result) {
          entityMap.put(entity.getId(), entity);
        }

        // Cache entities
        if (cachedObjects != null) {
          for (CachedEntity cachedObject : cachedObjects) {
            EntityImpl cachedEntity = (EntityImpl) cachedObject.getEntity();
            if (retainEntityCondition.isRetained(cachedEntity)) {
              entityMap.put(cachedEntity.getId(), cachedEntity); // will overwite db version with newer version
            }
          }
        }
        
        if (getManagedEntitySubClasses() != null) {
          for (Class<? extends EntityImpl> entitySubClass : getManagedEntitySubClasses()) {
            Collection<CachedEntity> subclassCachedObjects = getEntityCache().findInCacheAsCachedObjects(entitySubClass);
            if (subclassCachedObjects != null) {
              for (CachedEntity subclassCachedObject : subclassCachedObjects) {
                EntityImpl cachedSubclassEntity = (EntityImpl) subclassCachedObject.getEntity();
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
      Iterator<EntityImpl> resultIterator = result.iterator();
      while (resultIterator.hasNext()) {
        if (getDbSqlSession().isEntityToBeDeleted(resultIterator.next())) {
          resultIterator.remove();
        }
      }
    }

    return new ArrayList<EntityImpl>(result);
  }
  
}
