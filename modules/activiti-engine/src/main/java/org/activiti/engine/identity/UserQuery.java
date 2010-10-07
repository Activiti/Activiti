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

import java.util.List;

import org.activiti.engine.ActivitiException;


/**
 * Allows programmatic querying of {@link User}
 * 
 * @author Joram Barrez
 */
public interface UserQuery {
  
  /** Only select {@link User}s with the given id/ */
  UserQuery id(String id);
  
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
  UserQuery orderById();
  
  /** Order by user first name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByFirstName();
  
  /** Order by user last name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByLastName();
  
  /** Order by user email  (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderByEmail();
  
  /** Order by the given property (needs to be followed by {@link #asc()} or {@link #desc()}). */
  UserQuery orderBy(UserQueryProperty property);
  
  /** Order the results ascending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  UserQuery asc();

  /** Order the results descending on the given property as
   * defined in this class (needs to come after a call to one of the orderByXxxx methods). */
  UserQuery desc();

  //results ////////////////////////////////////////////////////////

  /** Executes the query and counts number of {@link User}s in the result. */
  long count();
  
  /**
   * Executes the query and returns the {@link User}. 
   * @throws ActivitiException when the query results in more 
   * than one {@link User}. 
   */
  User singleResult();
  
  /** Executes the query and get a list of {@link User}s as the result. */
  List<User> list();
  
  /** Executes the query and get a list of {@link User}s as the result. */
  List<User> listPage(int firstResult, int maxResults);

}
