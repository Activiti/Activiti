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
package org.activiti.form.engine.impl.persistence.entity.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.form.api.Form;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.FormQueryImpl;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.persistence.entity.FormEntity;
import org.activiti.form.engine.impl.persistence.entity.FormEntityImpl;
import org.activiti.form.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.FormDataManager;

/**
 * @author Joram Barrez
 */
public class MybatisFormDataManager extends AbstractDataManager<FormEntity> implements FormDataManager {
  
  public MybatisFormDataManager(FormEngineConfiguration formEngineConfiguration) {
    super(formEngineConfiguration);
  }

  @Override
  public Class<? extends FormEntity> getManagedEntityClass() {
    return FormEntityImpl.class;
  }
  
  @Override
  public FormEntity create() {
    return new FormEntityImpl();
  }
  
  @Override
  public FormEntity findLatestFormByKey(String formDefinitionKey) {
    return (FormEntity) getDbSqlSession().selectOne("selectLatestFormByKey", formDefinitionKey);
  }

  @Override
  public FormEntity findLatestFormByKeyAndTenantId(String formDefinitionKey, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("formDefinitionKey", formDefinitionKey);
    params.put("tenantId", tenantId);
    return (FormEntity) getDbSqlSession().selectOne("selectLatestFormByKeyAndTenantId", params);
  }
  
  @Override
  public FormEntity findLatestFormByKeyAndParentDeploymentId(String formDefinitionKey, String parentDeploymentId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("formDefinitionKey", formDefinitionKey);
    params.put("parentDeploymentId", parentDeploymentId);
    return (FormEntity) getDbSqlSession().selectOne("selectLatestFormByKeyAndParentDeploymentId", params);
  }
  
  @Override
  public FormEntity findLatestFormByKeyParentDeploymentIdAndTenantId(String formDefinitionKey, String parentDeploymentId, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("formDefinitionKey", formDefinitionKey);
    params.put("parentDeploymentId", parentDeploymentId);
    params.put("tenantId", tenantId);
    return (FormEntity) getDbSqlSession().selectOne("selectLatestFormByKeyParentDeploymentIdAndTenantId", params);
  }

  @Override
  public void deleteFormsByDeploymentId(String deploymentId) {
    getDbSqlSession().delete("deleteFormsByDeploymentId", deploymentId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Form> findFormsByQueryCriteria(FormQueryImpl formQuery, Page page) {
    return getDbSqlSession().selectList("selectFormsByQueryCriteria", formQuery, page);
  }

  @Override
  public long findFormCountByQueryCriteria(FormQueryImpl formQuery) {
    return (Long) getDbSqlSession().selectOne("selectFormCountByQueryCriteria", formQuery);
  }

  @Override
  public FormEntity findFormByDeploymentAndKey(String deploymentId, String formDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("formDefinitionKey", formDefinitionKey);
    return (FormEntity) getDbSqlSession().selectOne("selectFormByDeploymentAndKey", parameters);
  }

  @Override
  public FormEntity findFormByDeploymentAndKeyAndTenantId(String deploymentId, String formDefinitionKey, String tenantId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("formDefinitionKey", formDefinitionKey);
    parameters.put("tenantId", tenantId);
    return (FormEntity) getDbSqlSession().selectOne("selectFormByDeploymentAndKeyAndTenantId", parameters);
  }
  
  @Override
  public FormEntity findFormByKeyAndVersion(String formDefinitionKey, Integer formVersion) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("formDefinitionKey", formDefinitionKey);
    params.put("formVersion", formVersion);
    List<FormEntity> results = getDbSqlSession().selectList("selectFormsByKeyAndVersion", params);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiFormException("There are " + results.size() + " forms with key = '" + formDefinitionKey + "' and version = '" + formVersion + "'.");
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public FormEntity findFormByKeyAndVersionAndTenantId(String formDefinitionKey, Integer formVersion, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("formDefinitionKey", formDefinitionKey);
    params.put("formVersion", formVersion);
    params.put("tenantId", tenantId);
    List<FormEntity> results = getDbSqlSession().selectList("selectFormsByKeyAndVersionAndTenantId", params);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiFormException("There are " + results.size() + " forms with key = '" + formDefinitionKey + "' and version = '" + formVersion + "'.");
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Form> findFormsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectFormByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findFormCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectFormCountByNativeQuery", parameterMap);
  }

  @Override
  public void updateFormTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateFormTenantIdForDeploymentId", params);
  }
  
}
