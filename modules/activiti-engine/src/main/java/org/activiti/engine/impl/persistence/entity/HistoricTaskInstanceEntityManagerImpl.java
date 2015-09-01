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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.util.Activiti5Util;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class HistoricTaskInstanceEntityManagerImpl extends AbstractEntityManager<HistoricTaskInstanceEntity> implements HistoricTaskInstanceEntityManager {

  @Override
  public Class<HistoricTaskInstanceEntity> getManagedEntity() {
    return HistoricTaskInstanceEntity.class;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void deleteHistoricTaskInstancesByProcessInstanceId(String processInstanceId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      List<String> taskInstanceIds = (List<String>) getDbSqlSession().selectList("selectHistoricTaskInstanceIdsByProcessInstanceId", processInstanceId);
      for (String taskInstanceId : taskInstanceIds) {
        delete(taskInstanceId);
      }
    }
  }

  @Override
  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
    }
    return 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return getDbSqlSession().selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery);
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesAndVariablesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      // paging doesn't work for combining task instances and variables
      // due to an outer join, so doing it in-memory
      if (historicTaskInstanceQuery.getFirstResult() < 0 || historicTaskInstanceQuery.getMaxResults() <= 0) {
        return Collections.EMPTY_LIST;
      }

      int firstResult = historicTaskInstanceQuery.getFirstResult();
      int maxResults = historicTaskInstanceQuery.getMaxResults();

      // setting max results, limit to 20000 results for performance
      // reasons
      historicTaskInstanceQuery.setMaxResults(20000);
      historicTaskInstanceQuery.setFirstResult(0);

      List<HistoricTaskInstance> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter("selectHistoricTaskInstancesWithVariablesByQueryCriteria", historicTaskInstanceQuery,
          historicTaskInstanceQuery.getFirstResult(), historicTaskInstanceQuery.getMaxResults());

      if (instanceList != null && !instanceList.isEmpty()) {
        if (firstResult > 0) {
          if (firstResult <= instanceList.size()) {
            int toIndex = firstResult + Math.min(maxResults, instanceList.size() - firstResult);
            return instanceList.subList(firstResult, toIndex);
          } else {
            return Collections.EMPTY_LIST;
          }
        } else {
          int toIndex = Math.min(maxResults, instanceList.size());
          return instanceList.subList(0, toIndex);
        }
      }
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public void delete(String id) {
    if (getHistoryManager().isHistoryEnabled()) {
      HistoricTaskInstanceEntity historicTaskInstance = findById(id);
      if (historicTaskInstance != null) {
        
        if (historicTaskInstance.getProcessDefinitionId() != null 
            && Activiti5Util.isActiviti5ProcessDefinitionId(getCommandContext(), historicTaskInstance.getProcessDefinitionId())) {
          Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
          activiti5CompatibilityHandler.deleteHistoricTask(id);
          return;
        }

        getHistoricDetailEntityManager().deleteHistoricDetailsByTaskId(id);
        getHistoricVariableInstanceEntityManager().deleteHistoricVariableInstancesByTaskId(id);
        getCommentEntityManager().deleteCommentsByTaskId(id);
        getAttachmentEntityManager().deleteAttachmentsByTaskId(id);
        getHistoricIdentityLinkEntityManager().deleteHistoricIdentityLinksByTaskId(id);
        
        getDbSqlSession().delete(historicTaskInstance);
      }
    }
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
