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

import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;

/**
 * @author Joram Barrez
 */
public interface EntityManager<EntityImpl extends Entity> {

  void insert(EntityImpl entity);

  void insert(EntityImpl entity, boolean fireCreateEvent);

  EntityImpl findById(String entityId);

  EntityImpl findByQuery(String selectQuery, Object parameter, CachedEntityMatcher<EntityImpl> cachedEntityMatcher);

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
  List<EntityImpl> getList(String dbQueryName, Object parameter, CachedEntityMatcher<EntityImpl> entityMatcher, boolean checkCache);
  
  void delete(String id);
  
  void delete(EntityImpl entity);
  
  void delete(EntityImpl entity, boolean fireDeleteEvent);
  
}