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
package org.activiti.form.engine.impl.persistence.deploy;

import java.util.List;

import org.activiti.form.api.Form;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.FormQueryImpl;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntityManager;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntity;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DeploymentManager {

  protected FormEngineConfiguration engineConfig;
  protected DeploymentCache<FormCacheEntry> formCache;
  
  protected List<Deployer> deployers;
  protected FormEntityManager formEntityManager;
  protected FormDeploymentEntityManager deploymentEntityManager;
  
  public DeploymentManager(DeploymentCache<FormCacheEntry> formCache, FormEngineConfiguration engineConfig) {
    this.formCache = formCache;
    this.engineConfig = engineConfig;
  }
  
  public void deploy(FormDeploymentEntity deployment) {
    for (Deployer deployer : deployers) {
      deployer.deploy(deployment);
    }
  }

  public FormEntity findDeployedFormById(String formId) {
    if (formId == null) {
      throw new ActivitiFormException("Invalid form id : null");
    }

    // first try the cache
    FormCacheEntry cacheEntry = formCache.get(formId);
    FormEntity form = cacheEntry != null ? cacheEntry.getFormEntity() : null;

    if (form == null) {
      form = engineConfig.getFormEntityManager().findById(formId);
      if (form == null) {
        throw new ActivitiFormObjectNotFoundException("no deployed form found with id '" + formId + "'");
      }
      form = resolveForm(form).getFormEntity();
    }
    return form;
  }

  public FormEntity findDeployedLatestFormByKey(String formDefinitionKey) {
    FormEntity form = formEntityManager.findLatestFormByKey(formDefinitionKey);

    if (form == null) {
      throw new ActivitiFormObjectNotFoundException("no forms deployed with key '" + formDefinitionKey + "'");
    }
    form = resolveForm(form).getFormEntity();
    return form;
  }

  public FormEntity findDeployedLatestFormByKeyAndTenantId(String formDefinitionKey, String tenantId) {
    FormEntity form = formEntityManager.findLatestFormByKeyAndTenantId(formDefinitionKey, tenantId);
    
    if (form == null) {
      throw new ActivitiFormObjectNotFoundException("no forms deployed with key '" + formDefinitionKey + "' for tenant identifier '" + tenantId + "'");
    }
    form = resolveForm(form).getFormEntity();
    return form;
  }
  
  public FormEntity findDeployedLatestFormByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId) {
    FormEntity form = formEntityManager.findLatestFormByKeyAndParentDeploymentId(formDefinitionKey, parentDeploymentId);
    
    if (form == null) {
      throw new ActivitiFormObjectNotFoundException("no forms deployed with key '" + formDefinitionKey + 
          "' for parent deployment id '" + parentDeploymentId + "'");
    }
    form = resolveForm(form).getFormEntity();
    return form;
  }
  
  public FormEntity findDeployedLatestFormByKeyParentDeploymentIdAndTenantId(String formDefinitionKey, String parentDeploymentId, String tenantId) {
    FormEntity form = formEntityManager.findLatestFormByKeyParentDeploymentIdAndTenantId(formDefinitionKey, parentDeploymentId, tenantId);
    
    if (form == null) {
      throw new ActivitiFormObjectNotFoundException("no forms deployed with key '" + formDefinitionKey + 
          "' for parent deployment id '" + parentDeploymentId + "' and tenant identifier '" + tenantId + "'");
    }
    form = resolveForm(form).getFormEntity();
    return form;
  }

  public FormEntity findDeployedFormByKeyAndVersionAndTenantId(String formDefinitionKey, int formVersion, String tenantId) {
    FormEntity form = formEntityManager.findFormByKeyAndVersionAndTenantId(formDefinitionKey, formVersion, tenantId);
    
    if (form == null) {
      throw new ActivitiFormObjectNotFoundException("no decisions deployed with key = '" + formDefinitionKey + "' and version = '" + formVersion + "'");
    }
    
    form = resolveForm(form).getFormEntity();
    return form;
  }

  /**
   * Resolving the decision will fetch the DMN, parse it and store the
   * {@link DmnDefinition} in memory.
   */
  public FormCacheEntry resolveForm(Form form) {
    String formId = form.getId();
    String deploymentId = form.getDeploymentId();

    FormCacheEntry cachedForm = formCache.get(formId);

    if (cachedForm == null) {
      FormDeploymentEntity deployment = engineConfig.getDeploymentEntityManager().findById(deploymentId);
      List<ResourceEntity> resources = engineConfig.getResourceEntityManager().findResourcesByDeploymentId(deploymentId);
      for (ResourceEntity resource : resources) {
        deployment.addResource(resource);
      }
      
      deployment.setNew(false);
      deploy(deployment);
      cachedForm = formCache.get(formId);

      if (cachedForm == null) {
        throw new ActivitiFormException("deployment '" + deploymentId + "' didn't put form '" + formId + "' in the cache");
      }
    }
    return cachedForm;
  }

  public void removeDeployment(String deploymentId) {

    FormDeploymentEntity deployment = deploymentEntityManager.findById(deploymentId);
    if (deployment == null) {
      throw new ActivitiFormObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.");
    }

    // Remove any process definition from the cache
    List<Form> forms = new FormQueryImpl().deploymentId(deploymentId).list();
    
    // Delete data
    deploymentEntityManager.deleteDeployment(deploymentId);

    for (Form form : forms) {
      formCache.remove(form.getId());
    }
  }
  
  public List<Deployer> getDeployers() {
    return deployers;
  }

  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public DeploymentCache<FormCacheEntry> getFormCache() {
      return formCache;
  }

  public void setFormCache(DeploymentCache<FormCacheEntry> formCache) {
      this.formCache = formCache;
  }

  public FormEntityManager getFormEntityManager() {
    return formEntityManager;
  }

  public void setFormEntityManager(FormEntityManager formEntityManager) {
    this.formEntityManager = formEntityManager;
  }

  public FormDeploymentEntityManager getDeploymentEntityManager() {
    return deploymentEntityManager;
  }

  public void setDeploymentEntityManager(FormDeploymentEntityManager deploymentEntityManager) {
    this.deploymentEntityManager = deploymentEntityManager;
  }
}
