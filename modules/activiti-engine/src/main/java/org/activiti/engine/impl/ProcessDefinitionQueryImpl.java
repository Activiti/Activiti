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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery<ProcessDefinitionQuery, ProcessDefinition> 
  implements ProcessDefinitionQuery {
  
  private static final long serialVersionUID = 1L;
  protected String id;
  protected String category;
  protected String categoryLike;
  protected String categoryNotEquals;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected Set<String> deploymentIds;
  protected String key;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected boolean latest = false;
  protected SuspensionState suspensionState;
  protected String authorizationUserId;
  protected String procDefId;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  
  protected String eventSubscriptionName;
  protected String eventSubscriptionType;

  public ProcessDefinitionQueryImpl() {
  }

  public ProcessDefinitionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public ProcessDefinitionQueryImpl processDefinitionId(String processDefinitionId) {
    this.id = processDefinitionId;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionCategory(String category) {
    if (category == null) {
      throw new ActivitiIllegalArgumentException("category is null");
    }
    this.category = category;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiIllegalArgumentException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionCategoryNotEquals(String categoryNotEquals) {
    if (categoryNotEquals == null) {
      throw new ActivitiIllegalArgumentException("categoryNotEquals is null");
    }
    this.categoryNotEquals = categoryNotEquals;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionName(String name) {
    if (name == null) {
      throw new ActivitiIllegalArgumentException("name is null");
    }
    this.name = name;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiIllegalArgumentException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiIllegalArgumentException("id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }
  
  public ProcessDefinitionQueryImpl deploymentIds(Set<String> deploymentIds) {
    if (deploymentIds == null) {
      throw new ActivitiIllegalArgumentException("ids are null");
    }
    this.deploymentIds = deploymentIds;
    return this;
  }

  public ProcessDefinitionQueryImpl processDefinitionKey(String key) {
    if (key == null) {
      throw new ActivitiIllegalArgumentException("key is null");
    }
    this.key = key;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiIllegalArgumentException("keyLike is null");
    }
    this.keyLike = keyLike;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionResourceName(String resourceName) {
    if (resourceName == null) {
      throw new ActivitiIllegalArgumentException("resourceName is null");
    }
    this.resourceName = resourceName;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionResourceNameLike(String resourceNameLike) {
    if (resourceNameLike == null) {
      throw new ActivitiIllegalArgumentException("resourceNameLike is null");
    }
    this.resourceNameLike = resourceNameLike;
    return this;
  }
  
  public ProcessDefinitionQueryImpl processDefinitionVersion(Integer version) {
    if (version == null) {
      throw new ActivitiIllegalArgumentException("version is null");
    } else if (version <= 0) {
      throw new ActivitiIllegalArgumentException("version must be positive");
    }
    this.version = version;
    return this;
  }
  
  public ProcessDefinitionQueryImpl latestVersion() {
    this.latest = true;
    return this;
  }
  
  public ProcessDefinitionQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;    
    return this;
  }
  
  public ProcessDefinitionQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }
  
  public ProcessDefinitionQuery processDefinitionTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("processDefinition tenantId is null");
  	}
  	this.tenantId = tenantId;
  	return this;
  }
  
  public ProcessDefinitionQuery processDefinitionTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("process definition tenantId is null");
  	}
  	this.tenantIdLike = tenantIdLike;
  	return this;
  }
  
  public ProcessDefinitionQuery processDefinitionWithoutTenantId() {
  	this.withoutTenantId = true;
  	return this;
  }
  
  public ProcessDefinitionQuery messageEventSubscription(String messageName) {
    return eventSubscription("message", messageName);
  }
  
  public ProcessDefinitionQuery messageEventSubscriptionName(String messageName) {
    return eventSubscription("message", messageName);
  }

  public ProcessDefinitionQuery processDefinitionStarter(String procDefId) {
    this.procDefId = procDefId;
    return this;
  }

  public ProcessDefinitionQuery eventSubscription(String eventType, String eventName) {
    if(eventName == null) {
      throw new ActivitiIllegalArgumentException("event name is null");
    }
    if(eventType == null) {
      throw new ActivitiException("event type is null");
    }
    this.eventSubscriptionType = eventType;
    this.eventSubscriptionName = eventName;
    return this;
  }
  
  public List<String> getAuthorizationGroups() {
    // Simmilar behaviour as the TaskQuery.taskCandidateUser() which includes the groups the candidate
    // user is part of
    if(authorizationUserId != null) {
      List<Group> groups = Context
              .getCommandContext()
              .getGroupIdentityManager()
              .findGroupsByUser(authorizationUserId);
            List<String> groupIds = new ArrayList<String>();
            for (Group group : groups) {
              groupIds.add(group.getId());
            }
            return groupIds;
    }
    return null;
  }
  
  //sorting ////////////////////////////////////////////
  
  public ProcessDefinitionQuery orderByDeploymentId() {
    return orderBy(ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }
  
  public ProcessDefinitionQuery orderByProcessDefinitionKey() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
  }
  
  public ProcessDefinitionQuery orderByProcessDefinitionCategory() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
  }
  
  public ProcessDefinitionQuery orderByProcessDefinitionId() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  public ProcessDefinitionQuery orderByProcessDefinitionVersion() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
  }
  
  public ProcessDefinitionQuery orderByProcessDefinitionName() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
  }
  
  public ProcessDefinitionQuery orderByTenantId() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_TENANT_ID);
  }
  
  //results ////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getProcessDefinitionEntityManager()
      .findProcessDefinitionCountByQueryCriteria(this);
  }

  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getProcessDefinitionEntityManager()
      .findProcessDefinitionsByQueryCriteria(this, page);
  }
  
  public void checkQueryOk() {
    super.checkQueryOk();
    
    // latest() makes only sense when used with key() or keyLike()
    if (latest && ( (id != null) || (name != null) || (nameLike != null) || (version != null) || (deploymentId != null) ) ){
      throw new ActivitiIllegalArgumentException("Calling latest() can only be used in combination with key(String) and keyLike(String)");
    }
  }
  
  //getters ////////////////////////////////////////////
  
  public String getDeploymentId() {
    return deploymentId;
  }
  public Set<String> getDeploymentIds() {
    return deploymentIds;
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
  public String getCategory() {
    return category;
  }
  public String getCategoryLike() {
    return categoryLike;
  }
  public String getResourceName() {
    return resourceName;
  }
  public String getResourceNameLike() {
    return resourceNameLike;
  }  
  public SuspensionState getSuspensionState() {
    return suspensionState;
  }  
  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }
  public String getCategoryNotEquals() {
    return categoryNotEquals;
  }
  public String getTenantId() {
    return tenantId;
  }
  public String getTenantIdLike() {
    return tenantIdLike;
  }
  public boolean isWithoutTenantId() {
    return withoutTenantId;
	}

	public ProcessDefinitionQueryImpl startableByUser(String userId) {
    if (userId == null) {
      throw new ActivitiIllegalArgumentException("userId is null");
    }
    this.authorizationUserId = userId;
    return this;
  }
}
