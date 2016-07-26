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

import java.util.List;
import java.util.Map;

import org.activiti.form.api.FormDeployment;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.FormDeploymentQueryImpl;
import org.activiti.form.engine.impl.Page;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntityImpl;
import org.activiti.form.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.FormDeploymentDataManager;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class MybatisFormDeploymentDataManager extends AbstractDataManager<FormDeploymentEntity> implements FormDeploymentDataManager {

  public MybatisFormDeploymentDataManager(FormEngineConfiguration formEngineConfiguration) {
    super(formEngineConfiguration);
  }

  @Override
  public Class<? extends FormDeploymentEntity> getManagedEntityClass() {
    return FormDeploymentEntityImpl.class;
  }
  
  @Override
  public FormDeploymentEntity create() {
    return new FormDeploymentEntityImpl();
  }
  
  @Override
  public FormDeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List<?> list = getDbSqlSession().selectList("selectDeploymentsByName", deploymentName, 0, 1);
    if (list != null && !list.isEmpty()) {
      return (FormDeploymentEntity) list.get(0);
    }
    return null;
  }

  @Override
  public long findDeploymentCountByQueryCriteria(FormDeploymentQueryImpl deploymentQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<FormDeployment> findDeploymentsByQueryCriteria(FormDeploymentQueryImpl deploymentQuery, Page page) {
    final String query = "selectDeploymentsByQueryCriteria";
    return getDbSqlSession().selectList(query, deploymentQuery, page);
  }

  @Override
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbSqlSession().getSqlSession().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<FormDeployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectDeploymentByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByNativeQuery", parameterMap);
  }
  
}
