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

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricVariableInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.HistoricVariableInstanceByProcInstMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.HistoricVariableInstanceByTaskIdMatcher;

/**

 */
public class MybatisHistoricVariableInstanceDataManager extends AbstractDataManager<HistoricVariableInstanceEntity> implements HistoricVariableInstanceDataManager {
  
  protected CachedEntityMatcher<HistoricVariableInstanceEntity> historicVariableInstanceByTaskIdMatcher = new HistoricVariableInstanceByTaskIdMatcher();
  
  protected CachedEntityMatcher<HistoricVariableInstanceEntity> historicVariableInstanceByProcInstMatcher = new HistoricVariableInstanceByProcInstMatcher();

  public MybatisHistoricVariableInstanceDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends HistoricVariableInstanceEntity> getManagedEntityClass() {
    return HistoricVariableInstanceEntityImpl.class;
  }
  
  @Override
  public HistoricVariableInstanceEntity create() {
    return new HistoricVariableInstanceEntityImpl();
  }
  
  @Override
  public void insert(HistoricVariableInstanceEntity entity) {
    super.insert(entity);
  }
  
  @Override
  public List<HistoricVariableInstanceEntity> findHistoricVariableInstancesByProcessInstanceId(final String processInstanceId) {
    return getList("selectHistoricVariableInstanceByProcessInstanceId", processInstanceId, historicVariableInstanceByProcInstMatcher, true);
  }
  
  @Override
  public List<HistoricVariableInstanceEntity> findHistoricVariableInstancesByTaskId(final String taskId) {
    return getList("selectHistoricVariableInstanceByTaskId", taskId, historicVariableInstanceByTaskIdMatcher, true);
  }
  
  @Override
  public long findHistoricVariableInstanceCountByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricVariableInstanceCountByQueryCriteria", historicProcessVariableQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricVariableInstanceByQueryCriteria", historicProcessVariableQuery, page);
  }

  @Override
  public HistoricVariableInstanceEntity findHistoricVariableInstanceByVariableInstanceId(String variableInstanceId) {
    return (HistoricVariableInstanceEntity) getDbSqlSession().selectOne("selectHistoricVariableInstanceByVariableInstanceId", variableInstanceId);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricVariableInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricVariableInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricVariableInstanceCountByNativeQuery", parameterMap);
  }
  
}
