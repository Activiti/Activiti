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

import org.activiti.engine.query.Query;


/**
 * Allows programmatic querying of {@link User}
 * 
 * @author Joram Barrez
 */
public interface UserQuery extends Query<UserQuery, User> {
  
  /** Only select {@link User}s with the given id/ */
  UserQuery userId(String id);
  
  /** Only select {@link User}s with the given firstName. */
  UserQuery firstName(String firstName);
  
  /** Only select {@link User}s where the first name matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   */
  UserQuery firstNameLike(String firstNameLike);
  
  /** Only select {@link User}s with the given lastName. */
  UserQuery lastName(String lastName);
  
  /** Only select {@link User}s where the last name matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   */
  UserQuery lastNameLike(String lastNameLike);
  
  /** Only those {@link User}s with the given email addres. */
  UserQuery email(String email);
  
  /** Only select {@link User}s where the email matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   */
  UserQuery emailLike(String emailLike);
  
  /** Only select {@link User}s that belong to the given group. */ 
  UserQuery memberOfGroup(String groupId);
  
  //sorting ////////////////////////////////////////////////////////
  
  /** Order by user id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByUserId();
  
  /** Order by user first name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByFirstName();
  
  /** Order by user last name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByLastName();
  
  /** Order by user email  (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByEmail();
}
