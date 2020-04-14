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

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricTaskInstanceDataManager;

/**

 */
public class MybatisHistoricTaskInstanceDataManager extends AbstractDataManager<HistoricTaskInstanceEntity> implements HistoricTaskInstanceDataManager {

  public MybatisHistoricTaskInstanceDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends HistoricTaskInstanceEntity> getManagedEntityClass() {
    return HistoricTaskInstanceEntityImpl.class;
  }

  @Override
  public HistoricTaskInstanceEntity create() {
    return new HistoricTaskInstanceEntityImpl();
  }

  @Override
  public HistoricTaskInstanceEntity create(TaskEntity task, ExecutionEntity execution) {
    return new HistoricTaskInstanceEntityImpl(task, execution);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<HistoricTaskInstanceEntity> findHistoricTasksByParentTaskId(String parentTaskId) {
    return getDbSqlSession().selectList("selectHistoricTasksByParentTaskId", parentTaskId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstanceEntity> findHistoricTaskInstanceByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectHistoricTaskInstancesByProcessInstanceId", processInstanceId);
  }

  @Override
  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    return getDbSqlSession().selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery);
  }

  @Override
  public List<HistoricTaskInstance> findHistoricTaskInstancesAndVariablesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    // paging doesn't work for combining task instances and variables
    // due to an outer join, so doing it in-memory
    if (historicTaskInstanceQuery.getFirstResult() < 0 || historicTaskInstanceQuery.getMaxResults() <= 0) {
      return emptyList();
    }

    int firstResult = historicTaskInstanceQuery.getFirstResult();
    int maxResults = historicTaskInstanceQuery.getMaxResults();

    // setting max results, limit to 20000 results for performance reasons
    if (historicTaskInstanceQuery.getTaskVariablesLimit() != null) {
      historicTaskInstanceQuery.setMaxResults(historicTaskInstanceQuery.getTaskVariablesLimit());
    } else {
      historicTaskInstanceQuery.setMaxResults(getProcessEngineConfiguration().getHistoricTaskQueryLimit());
    }
    historicTaskInstanceQuery.setFirstResult(0);

    List<HistoricTaskInstance> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter("selectHistoricTaskInstancesWithVariablesByQueryCriteria", historicTaskInstanceQuery,
        historicTaskInstanceQuery.getFirstResult(), historicTaskInstanceQuery.getMaxResults());

    if (instanceList != null && !instanceList.isEmpty()) {
      if (firstResult > 0) {
        if (firstResult <= instanceList.size()) {
          int toIndex = firstResult + Math.min(maxResults, instanceList.size() - firstResult);
          return instanceList.subList(firstResult, toIndex);
        } else {
          return emptyList();
        }
      } else {
        int toIndex = Math.min(maxResults, instanceList.size());
        return instanceList.subList(0, toIndex);
      }
    }

    return instanceList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricTaskInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricTaskInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByNativeQuery", parameterMap);
  }

}
