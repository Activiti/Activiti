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
package org.activiti.engine.impl.persistence.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;

/**
 * @author Joram Barrez
 */
public class PersistentObjectCacheImpl implements PersistentObjectCache {
  
  protected Map<Class<?>, Map<String, CachedObject>> cachedObjects = new HashMap<Class<?>, Map<String,CachedObject>>();
  
  @Override
  public CachedObject cachePut(PersistentObject persistentObject, boolean storeState) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObject.getClass());
    if (classCache == null) {
      classCache = new HashMap<String, CachedObject>();
      cachedObjects.put(persistentObject.getClass(), classCache);
    }
    CachedObject cachedObject = new CachedObject(persistentObject, storeState);
    classCache.put(persistentObject.getId(), cachedObject);
    return cachedObject;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T> T cacheGet(Class<T> entityClass, String id) {
    CachedObject cachedObject = null;
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache != null) {
      cachedObject = classCache.get(id);
    }
    if (cachedObject != null) {
      return (T) cachedObject.getPersistentObject();
    }
    return null;
  }

  @Override
  public void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObjectClass);
    if (classCache == null) {
      return;
    }
    classCache.remove(persistentObjectId);
  }
  
  @Override
  public <T> Collection<CachedObject> findInCacheAsCachedObjects(Class<T> entityClass) {
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache != null) {
      return classCache.values();
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> findInCache(Class<T> entityClass) {
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache != null) {
      List<T> entities = new ArrayList<T>(classCache.size());
      for (CachedObject cachedObject : classCache.values()) {
        entities.add((T) cachedObject.getPersistentObject());
      }
      return entities;
    }
    return Collections.emptyList();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> findInCache(List<Class<T>> entityClasses) {
    List<T> entities = null;
    
    for (Class<T> entityClass : entityClasses) {
      Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
      if (classCache != null) {
        if (entities == null) {
          entities = new ArrayList<T>(classCache.size());
        }
        for (CachedObject cachedObject : classCache.values()) {
          entities.add((T) cachedObject.getPersistentObject());
        }
      }
    }
    
    if (entities != null) {
      return entities;
    }
    
    return Collections.emptyList();
  }

  public Map<Class<?>, Map<String, CachedObject>> getAllCachedObjects() {
    return cachedObjects;
  }
  
  @Override
  public void close() {
    
  }
  
  @Override
  public void flush() {
    
  }
  
}
