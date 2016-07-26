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
package org.activiti.dmn.engine.impl.persistence.entity.data.impl;

import java.util.List;
import java.util.Map;

import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.DmnDeploymentQueryImpl;
import org.activiti.dmn.engine.impl.Page;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntityImpl;
import org.activiti.dmn.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.DmnDeploymentDataManager;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class MybatisDmnDeploymentDataManager extends AbstractDataManager<DmnDeploymentEntity> implements DmnDeploymentDataManager {

  public MybatisDmnDeploymentDataManager(DmnEngineConfiguration dmnEngineConfiguration) {
    super(dmnEngineConfiguration);
  }

  @Override
  public Class<? extends DmnDeploymentEntity> getManagedEntityClass() {
    return DmnDeploymentEntityImpl.class;
  }
  
  @Override
  public DmnDeploymentEntity create() {
    return new DmnDeploymentEntityImpl();
  }
  
  @Override
  public DmnDeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List<?> list = getDbSqlSession().selectList("selectDeploymentsByName", deploymentName, 0, 1);
    if (list != null && !list.isEmpty()) {
      return (DmnDeploymentEntity) list.get(0);
    }
    return null;
  }

  @Override
  public long findDeploymentCountByQueryCriteria(DmnDeploymentQueryImpl deploymentQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<DmnDeployment> findDeploymentsByQueryCriteria(DmnDeploymentQueryImpl deploymentQuery, Page page) {
    final String query = "selectDeploymentsByQueryCriteria";
    return getDbSqlSession().selectList(query, deploymentQuery, page);
  }

  @Override
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbSqlSession().getSqlSession().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<DmnDeployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectDeploymentByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByNativeQuery", parameterMap);
  }
  
}
