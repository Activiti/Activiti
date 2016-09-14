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

import java.util.List;
import java.util.Set;

import org.activiti.form.api.Form;
import org.activiti.form.api.FormQuery;
import org.activiti.form.engine.ActivitiFormIllegalArgumentException;
import org.activiti.form.engine.impl.interceptor.CommandContext;
import org.activiti.form.engine.impl.interceptor.CommandExecutor;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class FormQueryImpl extends AbstractQuery<FormQuery, Form> implements FormQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected Set<String> ids;
  protected String category;
  protected String categoryLike;
  protected String categoryNotEquals;
  protected String name;
  protected String nameLike;
  protected String deploymentId;
  protected Set<String> deploymentIds;
  protected String parentDeploymentId;
  protected String parentDeploymentIdLike;
  protected String key;
  protected String keyLike;
  protected String resourceName;
  protected String resourceNameLike;
  protected Integer version;
  protected Integer versionGt;
  protected Integer versionGte;
  protected Integer versionLt;
  protected Integer versionLte;
  protected boolean latest;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;

  public FormQueryImpl() {
  }

  public FormQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public FormQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public FormQueryImpl formId(String formId) {
    this.id = formId;
    return this;
  }
  
  @Override
  public FormQuery formIds(Set<String> formIds) {
  	this.ids = formIds;
  	return this;
  }
  
  public FormQueryImpl formCategory(String category) {
    if (category == null) {
      throw new ActivitiFormIllegalArgumentException("category is null");
    }
    this.category = category;
    return this;
  }

  public FormQueryImpl formCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiFormIllegalArgumentException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public FormQueryImpl formCategoryNotEquals(String categoryNotEquals) {
    if (categoryNotEquals == null) {
      throw new ActivitiFormIllegalArgumentException("categoryNotEquals is null");
    }
    this.categoryNotEquals = categoryNotEquals;
    return this;
  }

  public FormQueryImpl formName(String name) {
    if (name == null) {
      throw new ActivitiFormIllegalArgumentException("name is null");
    }
    this.name = name;
    return this;
  }

  public FormQueryImpl formNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiFormIllegalArgumentException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public FormQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiFormIllegalArgumentException("id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public FormQueryImpl deploymentIds(Set<String> deploymentIds) {
    if (deploymentIds == null) {
      throw new ActivitiFormIllegalArgumentException("ids are null");
    }
    this.deploymentIds = deploymentIds;
    return this;
  }
  
  public FormQueryImpl parentDeploymentId(String parentDeploymentId) {
    if (parentDeploymentId == null) {
      throw new ActivitiFormIllegalArgumentException("parentDeploymentId is null");
    }
    this.parentDeploymentId = parentDeploymentId;
    return this;
  }
  
  public FormQueryImpl parentDeploymentIdLike(String parentDeploymentIdLike) {
    if (parentDeploymentIdLike == null) {
      throw new ActivitiFormIllegalArgumentException("parentDeploymentIdLike is null");
    }
    this.parentDeploymentIdLike = parentDeploymentIdLike;
    return this;
  }

  public FormQueryImpl formDefinitionKey(String key) {
    if (key == null) {
      throw new ActivitiFormIllegalArgumentException("key is null");
    }
    this.key = key;
    return this;
  }

  public FormQueryImpl formDefinitionKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiFormIllegalArgumentException("keyLike is null");
    }
    this.keyLike = keyLike;
    return this;
  }

  public FormQueryImpl formResourceName(String resourceName) {
    if (resourceName == null) {
      throw new ActivitiFormIllegalArgumentException("resourceName is null");
    }
    this.resourceName = resourceName;
    return this;
  }

  public FormQueryImpl formResourceNameLike(String resourceNameLike) {
    if (resourceNameLike == null) {
      throw new ActivitiFormIllegalArgumentException("resourceNameLike is null");
    }
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public FormQueryImpl formVersion(Integer version) {
    checkVersion(version);
    this.version = version;
    return this;
  }

  public FormQuery formVersionGreaterThan(Integer formVersion) {
    checkVersion(formVersion);
    this.versionGt = formVersion;
    return this;
  }

  public FormQuery formVersionGreaterThanOrEquals(Integer formVersion) {
    checkVersion(formVersion);
    this.versionGte = formVersion;
    return this;
  }

  public FormQuery formVersionLowerThan(Integer formVersion) {
    checkVersion(formVersion);
    this.versionLt = formVersion;
    return this;
  }

  public FormQuery formVersionLowerThanOrEquals(Integer formVersion) {
    checkVersion(formVersion);
    this.versionLte = formVersion;
    return this;
  }
  
  protected void checkVersion(Integer version) {
    if (version == null) {
      throw new ActivitiFormIllegalArgumentException("version is null");
    } else if (version <= 0) {
      throw new ActivitiFormIllegalArgumentException("version must be positive");
    }
  }

  public FormQueryImpl latestVersion() {
    this.latest = true;
    return this;
  }

  public FormQuery formTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiFormIllegalArgumentException("form tenantId is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public FormQuery formTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiFormIllegalArgumentException("form tenantId is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public FormQuery formWithoutTenantId() {
    this.withoutTenantId = true;
    return this;
  }

  // sorting ////////////////////////////////////////////

  public FormQuery orderByDeploymentId() {
    return orderBy(FormQueryProperty.DEPLOYMENT_ID);
  }

  public FormQuery orderByFormDefinitionKey() {
    return orderBy(FormQueryProperty.FORM_DEFINITION_KEY);
  }

  public FormQuery orderByFormCategory() {
    return orderBy(FormQueryProperty.FORM_CATEGORY);
  }

  public FormQuery orderByFormId() {
    return orderBy(FormQueryProperty.FORM_ID);
  }

  public FormQuery orderByFormVersion() {
    return orderBy(FormQueryProperty.FORM_VERSION);
  }

  public FormQuery orderByFormName() {
    return orderBy(FormQueryProperty.FORM_NAME);
  }

  public FormQuery orderByTenantId() {
    return orderBy(FormQueryProperty.FORM_TENANT_ID);
  }

  // results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getFormEntityManager().findFormCountByQueryCriteria(this);
  }

  public List<Form> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getFormEntityManager().findFormsByQueryCriteria(this, page);
  }

  public void checkQueryOk() {
    super.checkQueryOk();
  }

  // getters ////////////////////////////////////////////

  public String getDeploymentId() {
    return deploymentId;
  }

  public Set<String> getDeploymentIds() {
    return deploymentIds;
  }

  public String getId() {
    return id;
  }

  public Set<String> getIds() {
    return ids;
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
  
  public Integer getVersionGt() {
    return versionGt;
  }
  
  public Integer getVersionGte() {
    return versionGte;
  }
  
  public Integer getVersionLt() {
    return versionLt;
  }
  
  public Integer getVersionLte() {
    return versionLte;
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
}
