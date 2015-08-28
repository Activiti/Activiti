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
  
  Map<Class<?>, Map<String, CachedObject>> getAllCachedObjects();
  
  <T> T cacheGet(Class<T> entityClass, String id);
  
  CachedObject cachePut(PersistentObject persistentObject, boolean storeState);

  <T> List<T> findInCache(List<Class<T>> entityClasses);

  <T> List<T> findInCache(Class<T> entityClass);

  <T> Collection<CachedObject> findInCacheAsCachedObjects(Class<T> entityClass);
  
  void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId);

}
