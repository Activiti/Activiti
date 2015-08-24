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
package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;

/**
 * @author Joram Barrez
 */
public interface EntityManager<Entity extends PersistentObject> {

  void insert(Entity entity);

  void insert(Entity entity, boolean fireCreateEvent);

  Entity get(String persistentObjectId);

  Entity getEntity(Class<? extends Entity> clazz, String entityId, CachedEntityMatcher<Entity> cachedEntityMatcher);

  Entity getEntity(Class<? extends Entity> clazz, String selectQuery, Object parameter, CachedEntityMatcher<Entity> cachedEntityMatcher);

  List<Entity> getList(String dbQueryName, Object parameter, CachedEntityMatcher<Entity> retainEntityCondition);
  
  void delete(String id);
  
  void delete(Entity entity);
  
  void delete(Entity entity, boolean fireDeleteEvent);

}