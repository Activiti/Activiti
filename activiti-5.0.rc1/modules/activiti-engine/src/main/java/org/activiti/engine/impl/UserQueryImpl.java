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

package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.QueryProperty;


/**
 * @author Joram Barrez
 */
public class UserQueryImpl extends AbstractQuery<UserQuery, User> implements UserQuery {
  
  protected String id;
  protected String firstName;
  protected String firstNameLike;
  protected String lastName;
  protected String lastNameLike;
  protected String email;
  protected String emailLike;
  protected String groupId;
  
  public UserQueryImpl() {
    
  }
  
  public UserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public UserQuery userId(String id) {
    if (id == null) {
      throw new ActivitiException("Provided id is null");
    }
    this.id = id;
    return this;
  }
  
  public UserQuery userFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  
  public UserQuery userFirstNameLike(String firstNameLike) {
    if (firstNameLike == null) {
      throw new ActivitiException("Provided firstNameLike is null");
    }
    this.firstNameLike = firstNameLike;
    return this;
  }
  
  public UserQuery userLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  
  public UserQuery userLastNameLike(String lastNameLike) {
    if (lastNameLike == null) {
      throw new ActivitiException("Provided lastNameLike is null");
    }
    this.lastNameLike = lastNameLike;
    return this;
  }
  
  public UserQuery userEmail(String email) {
    this.email = email;
    return this;
  }
  
  public UserQuery userEmailLike(String emailLike) {
    if (emailLike == null) {
      throw new ActivitiException("Provided emailLike is null");
    }
    this.emailLike = emailLike;
    return this;
  }
  
  public UserQuery memberOfGroup(String groupId) {
    if (groupId == null) {
      throw new ActivitiException("Provided groupIds is null or empty");
    }
    this.groupId = groupId;
    return this;
  }

  //sorting //////////////////////////////////////////////////////////
  
  public UserQuery orderByUserId() {
    return orderBy(UserQueryProperty.USER_ID);
  }
  
  public UserQuery orderByUserEmail() {
    return orderBy(UserQueryProperty.EMAIL);
  }
  
  public UserQuery orderByUserFirstName() {
    return orderBy(UserQueryProperty.FIRST_NAME);
  }
  
  public UserQuery orderByUserLastName() {
    return orderBy(UserQueryProperty.LAST_NAME);
  }
  
  //results //////////////////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getIdentitySession()
      .findUserCountByQueryCriteria(this);
  }
  
  public List<User> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getIdentitySession()
      .findUserByQueryCriteria(this, page);
  }
  
  //getters //////////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public String getFirstName() {
    return firstName;
  }
  public String getFirstNameLike() {
    return firstNameLike;
  }
  public String getLastName() {
    return lastName;
  }
  public String getLastNameLike() {
    return lastNameLike;
  }
  public String getEmail() {
    return email;
  }
  public String getEmailLike() {
    return emailLike;
  }
  public String getGroupId() {
    return groupId;
  }
}
