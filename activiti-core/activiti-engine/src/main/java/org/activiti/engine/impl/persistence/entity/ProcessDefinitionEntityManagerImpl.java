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

package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.ProcessDefinitionDataManager;
import org.activiti.engine.repository.ProcessDefinition;

/**




 */
public class ProcessDefinitionEntityManagerImpl extends AbstractEntityManager<ProcessDefinitionEntity> implements ProcessDefinitionEntityManager {

  protected ProcessDefinitionDataManager processDefinitionDataManager;
  
  public ProcessDefinitionEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, ProcessDefinitionDataManager processDefinitionDataManager) {
    super(processEngineConfiguration);
    this.processDefinitionDataManager = processDefinitionDataManager;
  }

  @Override
  protected DataManager<ProcessDefinitionEntity> getDataManager() {
    return processDefinitionDataManager;
  }
  
  @Override
  public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return processDefinitionDataManager.findLatestProcessDefinitionByKey(processDefinitionKey);
  }

  @Override
  public ProcessDefinitionEntity findLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
   return processDefinitionDataManager.findLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, tenantId);
  }

  @Override
  public void deleteProcessDefinitionsByDeploymentId(String deploymentId) {
    processDefinitionDataManager.deleteProcessDefinitionsByDeploymentId(deploymentId);
  }

  @Override
  public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery, Page page) {
   return processDefinitionDataManager.findProcessDefinitionsByQueryCriteria(processDefinitionQuery, page);
  }

  @Override
  public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
    return processDefinitionDataManager.findProcessDefinitionCountByQueryCriteria(processDefinitionQuery);
  }

  @Override
  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    return processDefinitionDataManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinitionKey);
  }

  @Override
  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKeyAndTenantId(String deploymentId, String processDefinitionKey, String tenantId) {
   return processDefinitionDataManager.findProcessDefinitionByDeploymentAndKeyAndTenantId(deploymentId, processDefinitionKey, tenantId);
  }

  @Override
  public ProcessDefinition findProcessDefinitionByKeyAndVersionAndTenantId(String processDefinitionKey, Integer processDefinitionVersion, String tenantId) {
    if (tenantId == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(tenantId)) {
      return processDefinitionDataManager.findProcessDefinitionByKeyAndVersion(processDefinitionKey, processDefinitionVersion);
    } else {
      return processDefinitionDataManager.findProcessDefinitionByKeyAndVersionAndTenantId(processDefinitionKey, processDefinitionVersion, tenantId);
    }
  }

  @Override
  public List<ProcessDefinition> findProcessDefinitionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return processDefinitionDataManager.findProcessDefinitionsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findProcessDefinitionCountByNativeQuery(Map<String, Object> parameterMap) {
    return processDefinitionDataManager.findProcessDefinitionCountByNativeQuery(parameterMap);
  }

  @Override
  public void updateProcessDefinitionTenantIdForDeployment(String deploymentId, String newTenantId) {
    processDefinitionDataManager.updateProcessDefinitionTenantIdForDeployment(deploymentId, newTenantId);
  }

  public ProcessDefinitionDataManager getProcessDefinitionDataManager() {
    return processDefinitionDataManager;
  }

  public void setProcessDefinitionDataManager(ProcessDefinitionDataManager processDefinitionDataManager) {
    this.processDefinitionDataManager = processDefinitionDataManager;
  }
  
}
