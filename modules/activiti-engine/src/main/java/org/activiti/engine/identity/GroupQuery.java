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

package org.activiti.engine.identity;

import org.activiti.engine.impl.Query;


/**
 * Allows to programmatically query for {@link Group}s.
 * 
 * @author Joram Barrez
 */
public interface GroupQuery extends Query<GroupQuery, Group> {
  
  /** Only select {@link Group}s with the given id. */
  GroupQuery id(String id);
  
  /** Only select {@link Group}s with the given name. */
  GroupQuery name(String name);
  
  /** Only select {@link Group}s where the name matches the given parameter.
   *  The syntax to use is that of SQL, eg. %activiti%. */
  GroupQuery nameLike(String nameLike);
  
  /** Only select {@link Group}s which have the given type. */
  GroupQuery type(String type);
  
  /** Only selects {@link Group}s where the given user is a member of. */
  GroupQuery member(String userId);
  
  //sorting ////////////////////////////////////////////////////////
  
  /** Order by group id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  GroupQuery orderById();
  
  /** Order by group name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  GroupQuery orderByName();
  
  /** Order by group type (needs to be followed by {@link #asc()} or {@link #desc()}). */
  GroupQuery orderByType();

}
