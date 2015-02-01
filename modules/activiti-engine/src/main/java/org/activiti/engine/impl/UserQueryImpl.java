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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Joram Barrez
 */
public class UserQueryImpl extends AbstractQuery<UserQuery, User> implements UserQuery {
  
  private static final long serialVersionUID = 1L;
  protected String id;
  protected String firstName;
  protected String firstNameLike;
  protected String lastName;
  protected String lastNameLike;
  protected String fullNameLike;
  protected String email;
  protected String emailLike;
  protected String groupId;
  protected String procDefId;
  
  public UserQueryImpl() {
  }

  public UserQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public UserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public UserQuery userId(String id) {
    if (id == null) {
      throw new ActivitiIllegalArgumentException("Provided id is null");
    }
    this.id = id;
    return this;
  }
  
  public UserQuery userFirstName(String firstName) {
    if (firstName == null) {
      throw new ActivitiIllegalArgumentException("Provided firstName is null");
    }
    this.firstName = firstName;
    return this;
  }
  
  public UserQuery userFirstNameLike(String firstNameLike) {
    if (firstNameLike == null) {
      throw new ActivitiIllegalArgumentException("Provided firstNameLike is null");
    }
    this.firstNameLike = firstNameLike;
    return this;
  }
  
  public UserQuery userLastName(String lastName) {
    if (lastName == null) {
      throw new ActivitiIllegalArgumentException("Provided lastName is null");
    }
    this.lastName = lastName;
    return this;
  }
  
  public UserQuery userLastNameLike(String lastNameLike) {
    if (lastNameLike == null) {
      throw new ActivitiIllegalArgumentException("Provided lastNameLike is null");
    }
    this.lastNameLike = lastNameLike;
    return this;
  }
  
  public UserQuery userFullNameLike(String fullNameLike) {
    if (fullNameLike == null) {
      throw new ActivitiIllegalArgumentException("Provided full name is null");
    }
    this.fullNameLike = fullNameLike;
    return this;
  }
  
  public UserQuery userEmail(String email) {
    if (email == null) {
      throw new ActivitiIllegalArgumentException("Provided email is null");
    }
    this.email = email;
    return this;
  }
  
  public UserQuery userEmailLike(String emailLike) {
    if (emailLike == null) {
      throw new ActivitiIllegalArgumentException("Provided emailLike is null");
    }
    this.emailLike = emailLike;
    return this;
  }
  
  public UserQuery memberOfGroup(String groupId) {
    if (groupId == null) {
      throw new ActivitiIllegalArgumentException("Provided groupIds is null or empty");
    }
    this.groupId = groupId;
    return this;
  }
  
  public UserQuery potentialStarter(String procDefId) {
    if (procDefId == null) {
      throw new ActivitiIllegalArgumentException("Provided processDefinitionId is null or empty");
    }
    this.procDefId = procDefId;
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
      .getUserIdentityManager()
      .findUserCountByQueryCriteria(this);
  }
  
  public List<User> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getUserIdentityManager()
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
  public String getFullNameLike() {
    return fullNameLike;
  }
  
}
