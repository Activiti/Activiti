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
package org.activiti.engine.impl.persistence.entity.data.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailAssignmentEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailAssignmentEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailTransitionInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailTransitionInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricDetailDataManager;


public class MybatisHistoricDetailDataManager extends AbstractDataManager<HistoricDetailEntity> implements HistoricDetailDataManager {

  public MybatisHistoricDetailDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends HistoricDetailEntity> getManagedEntityClass() {
    return HistoricDetailEntityImpl.class;
  }
  
  @Override
  public HistoricDetailEntity create() {
    // Superclass is abstract
    throw new UnsupportedOperationException();
  }
  
  @Override
  public HistoricDetailAssignmentEntity createHistoricDetailAssignment() {
    return new HistoricDetailAssignmentEntityImpl();
  }
  
  @Override
  public HistoricDetailTransitionInstanceEntity createHistoricDetailTransitionInstance() {
    return new HistoricDetailTransitionInstanceEntityImpl();
  }
  
  @Override
  public HistoricDetailVariableInstanceUpdateEntity createHistoricDetailVariableInstanceUpdate() {
    return new HistoricDetailVariableInstanceUpdateEntityImpl();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricDetailEntity> findHistoricDetailsByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectHistoricDetailByProcessInstanceId", processInstanceId);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricDetailEntity> findHistoricDetailsByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectHistoricDetailByTaskId", taskId);
  }
  
  @Override
  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricDetailCountByQueryCriteria", historicVariableUpdateQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricDetailsByQueryCriteria", historicVariableUpdateQuery, page);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricDetailByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricDetailCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricDetailCountByNativeQuery", parameterMap);
  }
  
}
