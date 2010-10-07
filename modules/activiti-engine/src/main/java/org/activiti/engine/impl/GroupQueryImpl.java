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
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.GroupQueryProperty;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Joram Barrez
 */
public class GroupQueryImpl extends AbstractQuery<Group> implements GroupQuery {
  
  protected String id;
  protected String name;
  protected String nameLike;
  protected String type;
  protected String userId;
  protected GroupQueryProperty orderProperty;
  
  public GroupQueryImpl() {
    
  }
  
  public GroupQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public GroupQuery id(String id) {
    if (id == null) {
      throw new ActivitiException("Provided id is null");
    }
    this.id = id;
    return this;
  }
  
  public GroupQuery name(String name) {
    if (name == null) {
      throw new ActivitiException("Provided name is null");
    }
    this.name = name;
    return this;
  }
  
  public GroupQuery nameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("Provided nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  public GroupQuery type(String type) {
    if (type == null) {
      throw new ActivitiException("Provided type is null");
    }
    this.type = type;
    return this;
  }
  
  public GroupQuery member(String userId) {
    if (userId == null) {
      throw new ActivitiException("Provided userId is null");
    }
    this.userId = userId;
    return this;
  }

  //sorting ////////////////////////////////////////////////////////
  
  public GroupQuery orderById() {
    return orderBy(GroupQueryProperty.ID);
  }
  
  public GroupQuery orderByName() {
    return orderBy(GroupQueryProperty.NAME);
  }
  
  public GroupQuery orderByType() {
    return orderBy(GroupQueryProperty.TYPE);
  }
  
  public GroupQuery orderBy(GroupQueryProperty property) {
    this.orderProperty = property;
    return this;
  }
  
  public GroupQuery asc() {
    return direction(Direction.ASCENDING);
  }
  
  public GroupQuery desc() {
    return direction(Direction.DESCENDING);
  }
  
  public GroupQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }

  //results ////////////////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getIdentitySession()
      .findGroupCountByQueryCriteria(this);
  }
  
  public List<Group> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getIdentitySession()
      .findGroupByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }
  
  //getters ////////////////////////////////////////////////////////
  
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getType() {
    return type;
  }
  public String getUserId() {
    return userId;
  }
  
}
