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

package org.activiti.form.engine.impl;

import java.io.Serializable;
import java.util.List;

import org.activiti.form.api.FormDeployment;
import org.activiti.form.api.FormDeploymentQuery;
import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.interceptor.CommandExecutor;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class FormDeploymentQueryImpl extends AbstractQuery<FormDeploymentQuery, FormDeployment> implements FormDeploymentQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String name;
  protected String nameLike;
  protected String category;
  protected String categoryNotEquals;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected String parentDeploymentId;
  protected String parentDeploymentIdLike;
  protected String formDefinitionKey;
  protected String formDefinitionKeyLike;

  public FormDeploymentQueryImpl() {
  }

  public FormDeploymentQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public FormDeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public FormDeploymentQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiFormIllegalArgumentException("Deployment id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public FormDeploymentQueryImpl deploymentName(String deploymentName) {
    if (deploymentName == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentName is null");
    }
    this.name = deploymentName;
    return this;
  }

  public FormDeploymentQueryImpl deploymentNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentNameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public FormDeploymentQueryImpl deploymentCategory(String deploymentCategory) {
    if (deploymentCategory == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentCategory is null");
    }
    this.category = deploymentCategory;
    return this;
  }

  public FormDeploymentQueryImpl deploymentCategoryNotEquals(String deploymentCategoryNotEquals) {
    if (deploymentCategoryNotEquals == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentCategoryExclude is null");
    }
    this.categoryNotEquals = deploymentCategoryNotEquals;
    return this;
  }

  public FormDeploymentQueryImpl parentDeploymentId(String parentDeploymentId) {
    if (parentDeploymentId == null) {
      throw new ActivitiFormIllegalArgumentException("parentDeploymentId is null");
    }
    this.parentDeploymentId = parentDeploymentId;
    return this;
  }

  public FormDeploymentQueryImpl parentDeploymentIdLike(String parentDeploymentIdLike) {
    if (parentDeploymentIdLike == null) {
      throw new ActivitiFormIllegalArgumentException("parentDeploymentIdLike is null");
    }
    this.parentDeploymentIdLike = parentDeploymentIdLike;
    return this;
  }

  public FormDeploymentQueryImpl deploymentWithoutTenantId() {
    this.withoutTenantId = true;
    return this;
  }
  
  public FormDeploymentQueryImpl deploymentTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentTenantId is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public FormDeploymentQueryImpl deploymentTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiFormIllegalArgumentException("deploymentTenantIdLike is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public FormDeploymentQueryImpl formDefinitionKey(String key) {
    if (key == null) {
      throw new ActivitiFormIllegalArgumentException("key is null");
    }
    this.formDefinitionKey = key;
    return this;
  }

  public FormDeploymentQueryImpl formDefinitionKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiFormIllegalArgumentException("keyLike is null");
    }
    this.formDefinitionKeyLike = keyLike;
    return this;
  }

  // sorting ////////////////////////////////////////////////////////

  public FormDeploymentQuery orderByDeploymentId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_ID);
  }

  public FormDeploymentQuery orderByDeploymentTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }

  public FormDeploymentQuery orderByDeploymentName() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_NAME);
  }

  public FormDeploymentQuery orderByTenantId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
  }

  // results ////////////////////////////////////////////////////////

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getDeploymentEntityManager().findDeploymentCountByQueryCriteria(this);
  }

  @Override
  public List<FormDeployment> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getDeploymentEntityManager().findDeploymentsByQueryCriteria(this, page);
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

  public String getFormDefinitionKey() {
    return formDefinitionKey;
  }

  public String getFormDefinitionKeyLike() {
    return formDefinitionKeyLike;
  }
}
