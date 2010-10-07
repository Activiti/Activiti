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
import org.activiti.engine.identity.UserQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Joram Barrez
 */
public class UserQueryImpl extends AbstractQuery<User> implements UserQuery {
  
  protected String id;
  protected String firstName;
  protected String firstNameLike;
  protected String lastName;
  protected String lastNameLike;
  protected String email;
  protected String emailLike;
  protected String groupId;
  protected UserQueryProperty orderProperty;
  
  public UserQueryImpl() {
    
  }
  
  public UserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public UserQuery id(String id) {
    if (id == null) {
      throw new ActivitiException("Provided id is null");
    }
    this.id = id;
    return this;
  }
  
  public UserQuery firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  
  public UserQuery firstNameLike(String firstNameLike) {
    if (firstNameLike == null) {
      throw new ActivitiException("Provided firstNameLike is null");
    }
    this.firstNameLike = firstNameLike;
    return this;
  }
  
  public UserQuery lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  
  public UserQuery lastNameLike(String lastNameLike) {
    if (lastNameLike == null) {
      throw new ActivitiException("Provided lastNameLike is null");
    }
    this.lastNameLike = lastNameLike;
    return this;
  }
  
  public UserQuery email(String email) {
    this.email = email;
    return this;
  }
  
  public UserQuery emailLike(String emailLike) {
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
  
  public UserQuery orderById() {
    return orderBy(UserQueryProperty.ID);
  }
  
  public UserQuery orderByEmail() {
    return orderBy(UserQueryProperty.EMAIL);
  }
  
  public UserQuery orderByFirstName() {
    return orderBy(UserQueryProperty.FIRST_NAME);
  }
  
  public UserQuery orderByLastName() {
    return orderBy(UserQueryProperty.LAST_NAME);
  }
  
  public UserQuery orderBy(UserQueryProperty property) {
    this.orderProperty = property;
    return this;
  }
  
  public UserQuery asc() {
    return direction(Direction.ASCENDING);
  }
  
  public UserQuery desc() {
    return direction(Direction.DESCENDING);
  }
  
  public UserQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
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
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
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
  public UserQueryProperty getOrderProperty() {
    return orderProperty;
  }
  
}
