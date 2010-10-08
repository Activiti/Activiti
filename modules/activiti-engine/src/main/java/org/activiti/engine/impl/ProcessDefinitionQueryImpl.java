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
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.repository.ProcessDefinitionQueryProperty;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery<ProcessDefinitionQuery, ProcessDefinition> 
  implements ProcessDefinitionQuery {
  
  protected String id;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected String key;
  protected String keyLike;
  protected Integer version;
  protected boolean latest = false;
  protected ProcessDefinitionQueryProperty orderProperty;

  public ProcessDefinitionQueryImpl() {
  }

  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public ProcessDefinitionQuery id(String processDefinitionId) {
    this.id = processDefinitionId;
    return this;
  }
  
  public ProcessDefinitionQuery name(String name) {
    if (name == null) {
      throw new ActivitiException("name is null");
    }
    this.name = name;
    return this;
  }
  
  public ProcessDefinitionQuery nameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiException("id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public ProcessDefinitionQuery key(String key) {
    if (key == null) {
      throw new ActivitiException("key is null");
    }
    this.key = key;
    return this;
  }
  
  public ProcessDefinitionQuery keyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiException("keyLike is null");
    }
    this.keyLike = keyLike;
    return this;
  }
  
  public ProcessDefinitionQuery version(Integer version) {
    if (version == null) {
      throw new ActivitiException("version is null");
    } else if (version <= 0) {
      throw new ActivitiException("version must be positive");
    }
    this.version = version;
    return this;
  }
  
  public ProcessDefinitionQuery latest() {
    this.latest = true;
    return this;
  }
  
  //sorting ////////////////////////////////////////////
  
  public ProcessDefinitionQuery orderByDeploymentId() {
    return orderBy(ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }
  
  public ProcessDefinitionQuery orderById() {
    return orderBy(ProcessDefinitionQueryProperty.ID);
  }
  
  public ProcessDefinitionQuery orderByKey() {
    return orderBy(ProcessDefinitionQueryProperty.KEY);
  }
  
  public ProcessDefinitionQuery orderByVersion() {
    return orderBy(ProcessDefinitionQueryProperty.VERSION);
  }
  
  public ProcessDefinitionQuery orderBy(QueryProperty property) {
    if(!(property instanceof ProcessDefinitionQueryProperty)) {
      throw new ActivitiException("Only ProcessDefinitionQueryProperty can be used with orderBy");
    }
    this.orderProperty = (ProcessDefinitionQueryProperty) property;
    return this;
  }
  
  public ProcessDefinitionQuery asc() {
    return direction(Direction.ASCENDING);
  }
  
  public ProcessDefinitionQuery desc() {
    return direction(Direction.DESCENDING);
  }
  
  public ProcessDefinitionQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }
  
  
  //results ////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getRepositorySession()
      .findProcessDefinitionCountByQueryCriteria(this);
  }

  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getRepositorySession()
      .findProcessDefinitionsByQueryCriteria(this, page);
  }
  
  public void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
    
    // latest() makes only sense when used with key() or keyLike()
    if (latest && ( (id != null) || (name != null) || (nameLike != null) || (version != null) || (deploymentId != null) ) ){
      throw new ActivitiException("Calling latest() can only be used in combination with key(String) and keyLike(String)");
    }
  }
  
  //getters ////////////////////////////////////////////
  
  public String getDeploymentId() {
    return deploymentId;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getKey() {
    return key;
  }
  public String getKeyLike() {
    return keyLike;
  }
  public Integer getVersion() {
    return version;
  }
  public boolean isLatest() {
    return latest;
  }
}
