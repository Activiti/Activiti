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
package org.activiti.engine.impl.persistence.entity.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.cache.CachedEntity;
import org.activiti.engine.impl.persistence.cache.EntityCache;

/**
 * @author Joram Barrez
 */
public abstract class AbstractDataManager<EntityImpl extends Entity> extends AbstractManager implements DataManager<EntityImpl> {
  
  public abstract Class<? extends EntityImpl> getManagedEntityClass();
  
  public List<Class<? extends EntityImpl>> getManagedEntitySubClasses() {
    return null;
  }
  
  protected DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }
  
  protected EntityCache getEntityCache() {
    return getSession(EntityCache.class);
  }
  
  @Override
  public EntityImpl findById(String entityId) {
    
    if (entityId == null) {
      return null;
    }

    // Cache
    EntityImpl cachedEntity = getEntityCache().findInCache(getManagedEntityClass(), entityId);
    if (cachedEntity != null) {
      return cachedEntity;
    }
    
    // Database
    return getDbSqlSession().selectById(getManagedEntityClass(), entityId);
  }
  
  @Override
  public void insert(EntityImpl entity) {
    getDbSqlSession().insert(entity);
  }
  
  public EntityImpl update(EntityImpl entity) {
    getDbSqlSession().update(entity);
    return entity;
  }
  
  @Override
  public void delete(String id) {
    EntityImpl entity = findById(id);
    delete(entity);
  }
  
  @Override
  public void delete(EntityImpl entity) {
    getDbSqlSession().delete(entity);
  }
  
  @SuppressWarnings("unchecked")
  protected EntityImpl findByQuery(String selectQuery, Object parameter, CachedEntityMatcher<EntityImpl> cachedEntityMatcher) {
    // Cache
    for (EntityImpl cachedEntity : getEntityCache().findInCache(getManagedEntityClass())) {
      if (cachedEntityMatcher.isRetained(cachedEntity)) {
        return cachedEntity;
      }
    }

    // Database
    return (EntityImpl) getDbSqlSession().selectOne(selectQuery, parameter);
  }
  
  /**
   * Gets a list by querying the database and the cache using {@link CachedEntityMatcher}.
   * First, the entities are fetched from the database using the provided query. 
   * The cache is then queried for the entities of the same type. If an entity matches
   * the {@link CachedEntityMatcher} condition, it replaces the entity from the database (as it is newer).
   * 
   * @param dbQueryName The query name that needs to be executed.
   * @param parameter The parameters for the query.
   * @param entityMatcher The matcher used to determine which entities from the cache needs to be retained
   * @param checkCache If false, no cache check will be done, and the returned list will simply be the list from the database.
   */
  @SuppressWarnings("unchecked")
  protected List<EntityImpl> getList(String dbQueryName, Object parameter, CachedEntityMatcher<EntityImpl> retainEntityCondition, boolean checkCache) {

    Collection<EntityImpl> result = getDbSqlSession().selectList(dbQueryName, parameter);
    
    if (checkCache) {
      
      Collection<CachedEntity> cachedObjects = getEntityCache().findInCacheAsCachedObjects(getManagedEntityClass());
      
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
