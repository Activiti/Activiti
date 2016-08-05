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

package org.activiti.form.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.form.api.Form;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.FormQueryImpl;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.persistence.entity.data.DataManager;
import org.activiti.form.engine.impl.persistence.entity.data.FormDataManager;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class FormEntityManagerImpl extends AbstractEntityManager<FormEntity> implements FormEntityManager {

  protected FormDataManager formDataManager;
  
  public FormEntityManagerImpl(FormEngineConfiguration formEngineConfiguration, FormDataManager formDataManager) {
    super(formEngineConfiguration);
    this.formDataManager = formDataManager;
  }

  @Override
  protected DataManager<FormEntity> getDataManager() {
    return formDataManager;
  }
  
  @Override
  public FormEntity findLatestFormByKey(String formDefinitionKey) {
    return formDataManager.findLatestFormByKey(formDefinitionKey);
  }

  @Override
  public FormEntity findLatestFormByKeyAndTenantId(String formDefinitionKey, String tenantId) {
   return formDataManager.findLatestFormByKeyAndTenantId(formDefinitionKey, tenantId);
  }
  
  @Override
  public FormEntity findLatestFormByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId) {
   return formDataManager.findLatestFormByKeyAndParentDeploymentId(formDefinitionKey, parentDeploymentId);
  }
  
  @Override
  public FormEntity findLatestFormByKeyParentDeploymentIdAndTenantId(String formDefinitionKey, String parentDeploymentId, String tenantId) {
   return formDataManager.findLatestFormByKeyParentDeploymentIdAndTenantId(formDefinitionKey, parentDeploymentId, tenantId);
  }

  @Override
  public void deleteFormsByDeploymentId(String deploymentId) {
    formDataManager.deleteFormsByDeploymentId(deploymentId);
  }

  @Override
  public List<Form> findFormsByQueryCriteria(FormQueryImpl formQuery, Page page) {
   return formDataManager.findFormsByQueryCriteria(formQuery, page);
  }

  @Override
  public long findFormCountByQueryCriteria(FormQueryImpl formQuery) {
    return formDataManager.findFormCountByQueryCriteria(formQuery);
  }

  @Override
  public FormEntity findFormByDeploymentAndKey(String deploymentId, String formDefinitionKey) {
    return formDataManager.findFormByDeploymentAndKey(deploymentId, formDefinitionKey);
  }

  @Override
  public FormEntity findFormByDeploymentAndKeyAndTenantId(String deploymentId, String formDefinitionKey, String tenantId) {
   return formDataManager.findFormByDeploymentAndKeyAndTenantId(deploymentId, formDefinitionKey, tenantId);
  }

  @Override
  public FormEntity findFormByKeyAndVersionAndTenantId(String formDefinitionKey, Integer formVersion, String tenantId) {
    if (tenantId == null || FormEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
      return formDataManager.findFormByKeyAndVersion(formDefinitionKey, formVersion);
    } else {
      return formDataManager.findFormByKeyAndVersionAndTenantId(formDefinitionKey, formVersion, tenantId);
    }
  }

  @Override
  public List<Form> findFormsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return formDataManager.findFormsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findFormCountByNativeQuery(Map<String, Object> parameterMap) {
    return formDataManager.findFormCountByNativeQuery(parameterMap);
  }

  @Override
  public void updateFormTenantIdForDeployment(String deploymentId, String newTenantId) {
    formDataManager.updateFormTenantIdForDeployment(deploymentId, newTenantId);
  }

  public FormDataManager getFormDataManager() {
    return formDataManager;
  }

  public void setFormDataManager(FormDataManager formDataManager) {
    this.formDataManager = formDataManager;
  }
  
}
