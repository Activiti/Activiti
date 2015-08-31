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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.Session;

/**
 * This is a cache for {@link PersistentObject} instances during the execution of one {@link Command}.
 * 
 * @author Joram Barrez
 */
public interface PersistentObjectCache extends Session {
  
  /**
   * Returns all cached {@link PersistentObject} instances as a map 
   * with following structure: { entityClassName, {entityId, PersistentObject} }
   */
  Map<Class<?>, Map<String, CachedPersistentObject>> getAllCachedPersistentObjects();
  
  /**
   * Adds the gives {@link PersistentObject} to the cache.
   * 
   * @param persistentObject The {@link PersistentObject} instance
   * @param storeState If true, the current state {@link PersistentObject#getPersistentState()} will be stored for future diffing.
   */
  CachedPersistentObject put(PersistentObject persistentObject, boolean storeState);
  
  /**
   * Returns the cached {@link PersistentObject} instance of the given class with the provided id.
   * Returns null if such a {@link PersistentObject} cannot be found. 
   */
  <T> T findInCache(Class<T> entityClass, String id);
  
  /**
   * Returns all cached {@link PersistentObject} instances of a given type.
   * Returns an empty list if no instances of the given type exist.
   */
  <T> List<T> findInCache(Class<T> entityClass);

  /**
   * Returns all {@link CachedPersistentObject} instances for the given type.
   * The difference with {@link #findInCache(Class)} is that here the whole {@link CachedPersistentObject}
   * is returned, which gives access to the persistent state at the moment of putting it in the cache.  
   */
  <T> Collection<CachedPersistentObject> findInCacheAsCachedObjects(Class<T> entityClass);
  
  /**
   * Removes the {@link PersistentObject} of the given type with the given id from the cache. 
   */
  void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId);

}
