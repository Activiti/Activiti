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
public class AbstractEntityManager<Entity extends PersistentObject> extends AbstractManager {

  /*
   * CRUD operations
   */

  public Class<Entity> getManagedPersistentObject() {
    // Cannot make abstract cause some managers don't use db persistence (eg ldap)
    throw new UnsupportedOperationException();
  }

  public void insert(Entity entity) {
    insert(entity, true);
  }

  public void insert(Entity entity, boolean fireCreateEvent) {
    getDbSqlSession().insert(entity);

    if (fireCreateEvent && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, entity));
    }
  }

  public void delete(Entity entity, boolean fireDeleteEvent) {
    getDbSqlSession().delete(entity);

    if (fireDeleteEvent && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, entity));
    }
  }

  public void delete(Entity entity) {
    delete(entity, true);
  }

  /**
   * Will check the cache first before doing the db query.
   */
  public Entity get(String persistentObjectId) {
    return getDbSqlSession().selectById(getManagedPersistentObject(), persistentObjectId);
  }

  /*
   * Advanced operations
   */

  public Entity getEntity(Class<? extends Entity> clazz, String entityId, CachedEntityMatcher<Entity> retainEntityCondition) {

    // Cache
    for (Entity cachedEntity : getDbSqlSession().findInCache(getManagedPersistentObject())) {
      if (retainEntityCondition.isRetained(cachedEntity)) {
        return cachedEntity;
      }
    }

    // Database
    return getDbSqlSession().selectById(clazz, entityId);
  }

  /**
   * Main way to query a list of objects: 1. Fetches all data from the database using the provided query and parameters 2. Checks the internal entity cache and replaces any found entity with this
   * newer (potentially changed) entity.
   */
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

    // Remove any entries which are already deleted
    Collection<Entity> result = entityMap.values();
    Iterator<Entity> resultIterator = result.iterator();
    while (resultIterator.hasNext()) {
      if (getDbSqlSession().isPersistentObjectDeleted(resultIterator.next())) {
        resultIterator.remove();
      }
    }

    return new ArrayList<Entity>(result);
  }

}
