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

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;

/**


 */
public class DeploymentQueryImpl extends AbstractQuery<DeploymentQuery, Deployment> implements DeploymentQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String name;
  protected String nameLike;
  protected String category;
  protected String categoryLike;
  protected String categoryNotEquals;
  protected String key;
  protected String keyLike;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected String processDefinitionKey;
  protected String processDefinitionKeyLike;
  protected boolean latest;

  public DeploymentQueryImpl() {
  }

  public DeploymentQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public DeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DeploymentQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiIllegalArgumentException("Deployment id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public DeploymentQueryImpl deploymentName(String deploymentName) {
    if (deploymentName == null) {
      throw new ActivitiIllegalArgumentException("deploymentName is null");
    }
    this.name = deploymentName;
    return this;
  }

  public DeploymentQueryImpl deploymentNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiIllegalArgumentException("deploymentNameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public DeploymentQueryImpl deploymentCategory(String deploymentCategory) {
    if (deploymentCategory == null) {
      throw new ActivitiIllegalArgumentException("deploymentCategory is null");
    }
    this.category = deploymentCategory;
    return this;
  }
  
  public DeploymentQueryImpl deploymentCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiIllegalArgumentException("deploymentCategoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public DeploymentQueryImpl deploymentCategoryNotEquals(String deploymentCategoryNotEquals) {
    if (deploymentCategoryNotEquals == null) {
      throw new ActivitiIllegalArgumentException("deploymentCategoryExclude is null");
    }
    this.categoryNotEquals = deploymentCategoryNotEquals;
    return this;
  }
  
  public DeploymentQueryImpl deploymentKey(String deploymentKey) {
    if (deploymentKey == null) {
      throw new ActivitiIllegalArgumentException("deploymentKey is null");
    }
    this.key = deploymentKey;
    return this;
  }
  
  public DeploymentQueryImpl deploymentKeyLike(String deploymentKeyLike) {
    if (deploymentKeyLike == null) {
      throw new ActivitiIllegalArgumentException("deploymentKeyLike is null");
    }
    this.keyLike = deploymentKeyLike;
    return this;
  }

  public DeploymentQueryImpl deploymentTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiIllegalArgumentException("deploymentTenantId is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public DeploymentQueryImpl deploymentTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiIllegalArgumentException("deploymentTenantIdLike is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public DeploymentQueryImpl deploymentWithoutTenantId() {
    this.withoutTenantId = true;
    return this;
  }

  public DeploymentQueryImpl processDefinitionKey(String key) {
    if (key == null) {
      throw new ActivitiIllegalArgumentException("key is null");
    }
    this.processDefinitionKey = key;
    return this;
  }

  public DeploymentQueryImpl processDefinitionKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiIllegalArgumentException("keyLike is null");
    }
    this.processDefinitionKeyLike = keyLike;
    return this;
  }
  
  public DeploymentQueryImpl latest() {
    if (key == null) {
      throw new ActivitiIllegalArgumentException("latest can only be used together with a deployment key");
    }
    
    this.latest = true;
    return this;
  }

  // sorting ////////////////////////////////////////////////////////

  public DeploymentQuery orderByDeploymentId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_ID);
  }

  public DeploymentQuery orderByDeploymenTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }

  public DeploymentQuery orderByDeploymentName() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_NAME);
  }

  public DeploymentQuery orderByTenantId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
  }

  // results ////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getDeploymentEntityManager().findDeploymentCountByQueryCriteria(this);
  }

  @Override
  public List<Deployment> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getDeploymentEntityManager().findDeploymentsByQueryCriteria(this, page);
  }

  @Override
  public Deployment executeSingleResult(CommandContext commandContext){

    Deployment deployment = commandContext.getDeploymentEntityManager().selectLatestDeployment("SpringAutoDeployment");

    if (deployment != null){
      return deployment;
    } else {
      List<Deployment> results = executeList(commandContext, null);
      if (results.size() == 1) {
        return results.get(0);
      } else if (results.size() > 1) {
        throw new ActivitiException("Query return " + results.size() + " results instead of max 1");
      }
      return null;
    }

  }

  // getters ////////////////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }

  public String getCategory() {
    return category;
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

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionKeyLike() {
    return processDefinitionKeyLike;
  }
}
