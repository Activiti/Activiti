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

package org.activiti.dmn.engine.impl;

import java.util.List;
import java.util.Set;

import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.activiti.dmn.engine.impl.interceptor.CommandContext;
import org.activiti.dmn.engine.impl.interceptor.CommandExecutor;
import org.activiti.dmn.engine.repository.DecisionTable;
import org.activiti.dmn.engine.repository.DecisionTableQuery;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DecisionTableQueryImpl extends AbstractQuery<DecisionTableQuery, DecisionTable> implements DecisionTableQuery {

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
  protected String decisionTableId;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;

  public DecisionTableQueryImpl() {
  }

  public DecisionTableQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public DecisionTableQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DecisionTableQueryImpl decisionTableId(String decisionTableId) {
    this.id = decisionTableId;
    return this;
  }
  
  @Override
  public DecisionTableQuery decisionTableIds(Set<String> decisionTableIds) {
  	this.ids = decisionTableIds;
  	return this;
  }
  
  public DecisionTableQueryImpl decisionTableCategory(String category) {
    if (category == null) {
      throw new ActivitiDmnIllegalArgumentException("category is null");
    }
    this.category = category;
    return this;
  }

  public DecisionTableQueryImpl decisionTableCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiDmnIllegalArgumentException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  public DecisionTableQueryImpl decisionTableCategoryNotEquals(String categoryNotEquals) {
    if (categoryNotEquals == null) {
      throw new ActivitiDmnIllegalArgumentException("categoryNotEquals is null");
    }
    this.categoryNotEquals = categoryNotEquals;
    return this;
  }

  public DecisionTableQueryImpl decisionTableName(String name) {
    if (name == null) {
      throw new ActivitiDmnIllegalArgumentException("name is null");
    }
    this.name = name;
    return this;
  }

  public DecisionTableQueryImpl decisionTableNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiDmnIllegalArgumentException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  public DecisionTableQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiDmnIllegalArgumentException("id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  public DecisionTableQueryImpl deploymentIds(Set<String> deploymentIds) {
    if (deploymentIds == null) {
      throw new ActivitiDmnIllegalArgumentException("ids are null");
    }
    this.deploymentIds = deploymentIds;
    return this;
  }

  public DecisionTableQueryImpl decisionTableKey(String key) {
    if (key == null) {
      throw new ActivitiDmnIllegalArgumentException("key is null");
    }
    this.key = key;
    return this;
  }

  public DecisionTableQueryImpl decisionTableKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiDmnIllegalArgumentException("keyLike is null");
    }
    this.keyLike = keyLike;
    return this;
  }

  public DecisionTableQueryImpl decisionTableResourceName(String resourceName) {
    if (resourceName == null) {
      throw new ActivitiDmnIllegalArgumentException("resourceName is null");
    }
    this.resourceName = resourceName;
    return this;
  }

  public DecisionTableQueryImpl decisionTableResourceNameLike(String resourceNameLike) {
    if (resourceNameLike == null) {
      throw new ActivitiDmnIllegalArgumentException("resourceNameLike is null");
    }
    this.resourceNameLike = resourceNameLike;
    return this;
  }

  public DecisionTableQueryImpl decisionTableVersion(Integer version) {
    checkVersion(version);
    this.version = version;
    return this;
  }

  public DecisionTableQuery decisionTableVersionGreaterThan(Integer decisionTableVersion) {
    checkVersion(decisionTableVersion);
    this.versionGt = decisionTableVersion;
    return this;
  }

  public DecisionTableQuery decisionTableVersionGreaterThanOrEquals(Integer decisionTableVersion) {
    checkVersion(decisionTableVersion);
    this.versionGte = decisionTableVersion;
    return this;
  }

  public DecisionTableQuery decisionTableVersionLowerThan(Integer decisionTableVersion) {
    checkVersion(decisionTableVersion);
    this.versionLt = decisionTableVersion;
    return this;
  }

  public DecisionTableQuery decisionTableVersionLowerThanOrEquals(Integer decisionTableVersion) {
    checkVersion(decisionTableVersion);
    this.versionLte = decisionTableVersion;
    return this;
  }
  
  protected void checkVersion(Integer version) {
    if (version == null) {
      throw new ActivitiDmnIllegalArgumentException("version is null");
    } else if (version <= 0) {
      throw new ActivitiDmnIllegalArgumentException("version must be positive");
    }
  }

  public DecisionTableQueryImpl latestVersion() {
    this.latest = true;
    return this;
  }

  public DecisionTableQuery decisionTableTenantId(String tenantId) {
    if (tenantId == null) {
      throw new ActivitiDmnIllegalArgumentException("decision table tenantId is null");
    }
    this.tenantId = tenantId;
    return this;
  }

  public DecisionTableQuery decisionTableTenantIdLike(String tenantIdLike) {
    if (tenantIdLike == null) {
      throw new ActivitiDmnIllegalArgumentException("decision table tenantId is null");
    }
    this.tenantIdLike = tenantIdLike;
    return this;
  }

  public DecisionTableQuery decisionTableWithoutTenantId() {
    this.withoutTenantId = true;
    return this;
  }

  // sorting ////////////////////////////////////////////

  public DecisionTableQuery orderByDeploymentId() {
    return orderBy(DecisionTableQueryProperty.DEPLOYMENT_ID);
  }

  public DecisionTableQuery orderByDecisionTableKey() {
    return orderBy(DecisionTableQueryProperty.DECISION_TABLE_KEY);
  }

  public DecisionTableQuery orderByDecisionTableCategory() {
    return orderBy(DecisionTableQueryProperty.DECISION_TABLE_CATEGORY);
  }

  public DecisionTableQuery orderByDecisionTableId() {
    return orderBy(DecisionTableQueryProperty.DECISION_TABLE_ID);
  }

  public DecisionTableQuery orderByDecisionTableVersion() {
    return orderBy(DecisionTableQueryProperty.DECISION_TABLE_VERSION);
  }

  public DecisionTableQuery orderByDecisionTableName() {
    return orderBy(DecisionTableQueryProperty.DECISION_TABLE_NAME);
  }

  public DecisionTableQuery orderByTenantId() {
    return orderBy(DecisionTableQueryProperty.DECISION_TABLE_TENANT_ID);
  }

  // results ////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext.getDecisionTableEntityManager().findDecisionTableCountByQueryCriteria(this);
  }

  public List<DecisionTable> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext.getDecisionTableEntityManager().findDecisionTablesByQueryCriteria(this, page);
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
