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
import org.activiti.form.api.FormDeployment;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.FormDeploymentQueryImpl;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.persistence.entity.data.DataManager;
import org.activiti.form.engine.impl.persistence.entity.data.FormDeploymentDataManager;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class FormDeploymentEntityManagerImpl extends AbstractEntityManager<FormDeploymentEntity> implements FormDeploymentEntityManager {

  protected FormDeploymentDataManager deploymentDataManager;
  
  public FormDeploymentEntityManagerImpl(FormEngineConfiguration formEngineConfiguration, FormDeploymentDataManager deploymentDataManager) {
    super(formEngineConfiguration);
    this.deploymentDataManager = deploymentDataManager;
  }
  
  @Override
  protected DataManager<FormDeploymentEntity> getDataManager() {
    return deploymentDataManager;
  }
  
  @Override
  public void insert(FormDeploymentEntity deployment) {
    super.insert(deployment);

    for (ResourceEntity resource : deployment.getResources().values()) {
      resource.setDeploymentId(deployment.getId());
      getResourceEntityManager().insert(resource);
    }
  }

  @Override
  public void deleteDeployment(String deploymentId) {
    deleteDecisionTablesForDeployment(deploymentId);
    getResourceEntityManager().deleteResourcesByDeploymentId(deploymentId);
    delete(findById(deploymentId));
  }
  
  protected void deleteDecisionTablesForDeployment(String deploymentId) {
    getFormEntityManager().deleteFormsByDeploymentId(deploymentId);
  }
  
  protected FormEntity findLatestForm(Form form) {
    FormEntity latestForm = null;
    if (form.getTenantId() != null && !FormEngineConfiguration.NO_TENANT_ID.equals(form.getTenantId())) {
      latestForm = getFormEntityManager().findLatestFormByKeyAndTenantId(form.getKey(), form.getTenantId());
    } else {
      latestForm = getFormEntityManager().findLatestFormByKey(form.getKey());
    }
    return latestForm;
  }

  @Override
  public FormDeploymentEntity findLatestDeploymentByName(String deploymentName) {
    return deploymentDataManager.findLatestDeploymentByName(deploymentName);
  }

  @Override
  public long findDeploymentCountByQueryCriteria(FormDeploymentQueryImpl deploymentQuery) {
    return deploymentDataManager.findDeploymentCountByQueryCriteria(deploymentQuery);
  }

  @Override
  public List<FormDeployment> findDeploymentsByQueryCriteria(FormDeploymentQueryImpl deploymentQuery, Page page) {
    return deploymentDataManager.findDeploymentsByQueryCriteria(deploymentQuery, page);
  }

  @Override
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return deploymentDataManager.getDeploymentResourceNames(deploymentId);
  }

  @Override
  public List<FormDeployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return deploymentDataManager.findDeploymentsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
    return deploymentDataManager.findDeploymentCountByNativeQuery(parameterMap);
  }

  public FormDeploymentDataManager getDeploymentDataManager() {
    return deploymentDataManager;
  }

  public void setDeploymentDataManager(FormDeploymentDataManager deploymentDataManager) {
    this.deploymentDataManager = deploymentDataManager;
  }
  
}
