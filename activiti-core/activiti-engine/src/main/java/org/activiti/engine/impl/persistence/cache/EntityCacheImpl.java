/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.cache;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.Entity;


public class EntityCacheImpl implements EntityCache {

  protected Map<Class<?>, Map<String, CachedEntity>> cachedObjects = new HashMap<Class<?>, Map<String,CachedEntity>>();

  @Override
  public CachedEntity put(Entity entity, boolean storeState) {
    Map<String, CachedEntity> classCache = cachedObjects.get(entity.getClass());
    if (classCache == null) {
      classCache = new HashMap<String, CachedEntity>();
      cachedObjects.put(entity.getClass(), classCache);
    }
    CachedEntity cachedObject = new CachedEntity(entity, storeState);
    classCache.put(entity.getId(), cachedObject);
    return cachedObject;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T findInCache(Class<T> entityClass, String id) {
    CachedEntity cachedObject = null;
    Map<String, CachedEntity> classCache = cachedObjects.get(entityClass);

    if (classCache == null) {
      classCache = findClassCacheByCheckingSubclasses(entityClass);
    }

    if (classCache != null) {
      cachedObject = classCache.get(id);
    }

    if (cachedObject != null) {
      return (T) cachedObject.getEntity();
    }

    return null;
  }

  protected Map<String, CachedEntity> findClassCacheByCheckingSubclasses(Class<?> entityClass) {
    for (Class<?> clazz : cachedObjects.keySet()) {
      if (entityClass.isAssignableFrom(clazz)) {
        return cachedObjects.get(clazz);
      }
    }
    return null;
  }

  @Override
  public void cacheRemove(Class<?> entityClass, String entityId) {
    Map<String, CachedEntity> classCache = cachedObjects.get(entityClass);
    if (classCache == null) {
      return;
    }
    classCache.remove(entityId);
  }

  @Override
  public <T> Collection<CachedEntity> findInCacheAsCachedObjects(Class<T> entityClass) {
    Map<String, CachedEntity> classCache = cachedObjects.get(entityClass);
    if (classCache != null) {
      return classCache.values();
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> findInCache(Class<T> entityClass) {
    Map<String, CachedEntity> classCache = cachedObjects.get(entityClass);

    if (classCache == null) {
      classCache = findClassCacheByCheckingSubclasses(entityClass);
    }

    if (classCache != null) {
      List<T> entities = new ArrayList<T>(classCache.size());
      for (CachedEntity cachedObject : classCache.values()) {
        entities.add((T) cachedObject.getEntity());
      }
      return entities;
    }

    return emptyList();
  }

  public Map<Class<?>, Map<String, CachedEntity>> getAllCachedEntities() {
    return cachedObjects;
  }

  @Override
  public void close() {

  }

  @Override
  public void flush() {

  }

}
