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
import org.activiti.form.engine.impl.FormQueryImpl;
import org.activiti.form.engine.impl.Page;

/**
 * @author Joram Barrez
 */
public interface FormEntityManager extends EntityManager<FormEntity> {

  FormEntity findLatestFormByKey(String formDefinitionKey);

  FormEntity findLatestFormByKeyAndTenantId(String formDefinitionKey, String tenantId);
  
  FormEntity findLatestFormByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId);
  
  FormEntity findLatestFormByKeyParentDeploymentIdAndTenantId(String formDefinitionKey, String parentDeploymentId, String tenantId);

  List<Form> findFormsByQueryCriteria(FormQueryImpl formQuery, Page page);

  long findFormCountByQueryCriteria(FormQueryImpl formQuery);

  FormEntity findFormByDeploymentAndKey(String deploymentId, String formDefinitionKey);

  FormEntity findFormByDeploymentAndKeyAndTenantId(String deploymentId, String formDefinitionKey, String tenantId);

  FormEntity findFormByKeyAndVersionAndTenantId(String formDefinitionKey, Integer formVersion, String tenantId);

  List<Form> findFormsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults);

  long findFormCountByNativeQuery(Map<String, Object> parameterMap);

  void updateFormTenantIdForDeployment(String deploymentId, String newTenantId);
  
  void deleteFormsByDeploymentId(String deploymentId);

}