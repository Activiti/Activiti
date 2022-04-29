/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;

/**


 */
public class ModelQueryImpl extends AbstractQuery<ModelQuery, Model> implements ModelQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String category;
  protected String categoryLike;
  protected String categoryNotEquals;
  protected String name;
  protected String nameLike;
  protected String key;
  protected Integer version;
  protected boolean latest;
  protected String deploymentId;
  protected boolean notDeployed;
  protected boolean deployed;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;

  public ModelQueryImpl() {
  }

  public ModelQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public ModelQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ModelQueryImpl modelId(String modelId) {
    this.id = modelId;
    return this;
  }

  public ModelQueryImpl modelCategory(String category) {
    if (category == null) {
      throw new ActivitiIllegalArgumentException("category is null");
    }
    this.category = category;
    return this;
  }

  public ModelQueryImpl modelCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiIllegalArgumentException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public ModelQueryImpl modelCategoryNotEquals(String categoryNotEquals) {
    if (categoryNotEquals == null) {
      throw new ActivitiIllegalArgumentException("categoryNotEquals is null");
    }
    this.categoryNotEquals = categoryNotEquals;
    return this;
  }

  public ModelQueryImpl modelName(String name) {
    if (name == null) {
      throw new ActivitiIllegalArgumentException("name is null");
    }
    this.name = name;
    return this;
  }

  public ModelQueryImpl modelNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiIllegalArgumentException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public ModelQuery modelKey(String key) {
    if (key == null) {
      throw new ActivitiIllegalArgumentException("key is null");
    }
    this.key = key;
    return this;
  }

  public ModelQueryImpl modelVersion(Integer version) {
    if (version == null) {
      throw new ActivitiIllegalArgumentException("version is null");
    } else if (version <= 0) {
      throw new ActivitiIllegalArgumentException("version must be positive");
    }
    this.version = version;
    return this;
  }

  public ModelQuery latestVersion() {
    this.latest = true;
    return this;
  }

  public ModelQuery deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiIllegalArgumentException("DeploymentId is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public ModelQuery notDeployed() {
    if (deployed) {
      throw new ActivitiIllegalArgumentException("Invalid usage: cannot use deployed() and notDeployed() in the same query");
    }
    this.notDeployed = true;
    return this;
  }

  public ModelQuery deployed() {
    if (notDeployed) {
      throw new ActivitiIllegalArgumentException("Invalid usage: cannot use deployed() and notDeployed() in the same query");
    }
    this.deployed = true;
    return this;
  }

  public ModelQuery modelTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiIllegalArgumentException("Model tenant id is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public ModelQuery modelTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiIllegalArgumentException("Model tenant id is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public ModelQuery modelWithoutTenantId() {
    this.withoutTenantId = true;
    return this;
  }

  // sorting ////////////////////////////////////////////

  public ModelQuery orderByModelCategory() {
    return orderBy(ModelQueryProperty.MODEL_CATEGORY);
  }

  public ModelQuery orderByModelId() {
    return orderBy(ModelQueryProperty.MODEL_ID);
  }

  public ModelQuery orderByModelKey() {
    return orderBy(ModelQueryProperty.MODEL_KEY);
  }

  public ModelQuery orderByModelVersion() {
    return orderBy(ModelQueryProperty.MODEL_VERSION);
  }

  public ModelQuery orderByModelName() {
    return orderBy(ModelQueryProperty.MODEL_NAME);
  }

  public ModelQuery orderByCreateTime() {
    return orderBy(ModelQueryProperty.MODEL_CREATE_TIME);
  }

  public ModelQuery orderByLastUpdateTime() {
    return orderBy(ModelQueryProperty.MODEL_LAST_UPDATE_TIME);
  }

  public ModelQuery orderByTenantId() {
    return orderBy(ModelQueryProperty.MODEL_TENANT_ID);
  }

  // results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getModelEntityManager().findModelCountByQueryCriteria(this);
  }

  public List<Model> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getModelEntityManager().findModelsByQueryCriteria(this, page);
  }

  // getters ////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }

  public Integer getVersion() {
    return version;
  }

  public String getCategory() {
    return category;
  }

  public String getCategoryLike() {
    return categoryLike;
  }

  public String getCategoryNotEquals() {
    return categoryNotEquals;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getKey() {
    return key;
  }

  public boolean isLatest() {
    return latest;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public boolean isNotDeployed() {
    return notDeployed;
  }

  public boolean isDeployed() {
    return deployed;
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
