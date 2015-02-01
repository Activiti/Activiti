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
  UserQuery userFirstName(String firstName);
  
  /** Only select {@link User}s where the first name matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   */
  UserQuery userFirstNameLike(String firstNameLike);
  
  /** Only select {@link User}s with the given lastName. */
  UserQuery userLastName(String lastName);
  
  /** Only select {@link User}s where the last name matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   */
  UserQuery userLastNameLike(String lastNameLike);
  
  /**
   * Only select {@link User}s where the full name matches the given parameters.
   * Both the first name and last name will be tried, ie in semi-sql:
   * where firstName like xxx or lastname like xxx
   */
  UserQuery userFullNameLike(String fullNameLike);
  
  /** Only those {@link User}s with the given email addres. */
  UserQuery userEmail(String email);
  
  /** Only select {@link User}s where the email matches the given parameter.
   * The syntax is that of SQL, eg. %activivi%.
   */
  UserQuery userEmailLike(String emailLike);
  
  /** Only select {@link User}s that belong to the given group. */ 
  UserQuery memberOfGroup(String groupId);

  /** Only select {@link User}S that are potential starter for the given process definition. */  
  public UserQuery potentialStarter(String procDefId);
  
  //sorting ////////////////////////////////////////////////////////
  
  /** Order by user id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByUserId();
  
  /** Order by user first name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByUserFirstName();
  
  /** Order by user last name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByUserLastName();
  
  /** Order by user email  (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByUserEmail();
}
