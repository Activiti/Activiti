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
package org.activiti.engine.impl.persistence.deploy;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.cfg.multitenant.TenantInfoHolder;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * @author jbarrez
 */
public class MultiSchemaMultiTenantProcessDefinitionCache implements DeploymentCache<ProcessDefinitionEntity> {

  protected TenantInfoHolder tenantInfoHolder;
  protected int cacheLimit;
  protected Map<String, DeploymentCache<ProcessDefinitionEntity>> caches = new HashMap<String, DeploymentCache<ProcessDefinitionEntity>>();

  public MultiSchemaMultiTenantProcessDefinitionCache(TenantInfoHolder tenantInfoHolder, int cacheLimit) {
    this.tenantInfoHolder = tenantInfoHolder;
    this.cacheLimit = cacheLimit;
  }

  protected DeploymentCache<ProcessDefinitionEntity> getDeploymentCacheForCurrentTenant() {
    String currentTenantId = tenantInfoHolder.getCurrentTenantId();
    DeploymentCache<ProcessDefinitionEntity> tenantDeploymentCache = caches.get(currentTenantId);
    if (tenantDeploymentCache == null) {
      tenantDeploymentCache = createTenantDeploymentCache(currentTenantId);
    }
    return tenantDeploymentCache;
  }

  protected synchronized DeploymentCache<ProcessDefinitionEntity> createTenantDeploymentCache(String currentTenantId) {
    DeploymentCache<ProcessDefinitionEntity> tenantDeploymentCache = caches.get(currentTenantId);
    if (tenantDeploymentCache == null) {
      tenantDeploymentCache = new DefaultDeploymentCache<ProcessDefinitionEntity>(cacheLimit);
      caches.put(currentTenantId, tenantDeploymentCache);
    }
    return tenantDeploymentCache;
  }

  @Override
  public ProcessDefinitionEntity get(String id) {
    return getDeploymentCacheForCurrentTenant().get(id);
  }

  @Override
  public void add(String id, ProcessDefinitionEntity object) {
    DeploymentCache<ProcessDefinitionEntity> tenantDeploymentCache = getDeploymentCacheForCurrentTenant();
    tenantDeploymentCache.add(id, object);
    System.out.println("AAP");
  }

  @Override
  public void remove(String id) {
    getDeploymentCacheForCurrentTenant().remove(id);
  }

  @Override
  public void clear() {
    getDeploymentCacheForCurrentTenant().clear();
  }

}