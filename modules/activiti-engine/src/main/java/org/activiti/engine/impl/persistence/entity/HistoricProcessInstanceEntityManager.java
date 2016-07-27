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

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceEntityManager extends AbstractManager {

  public HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId) {
    if (getHistoryManager().isHistoryEnabled()) {
      return (HistoricProcessInstanceEntity) getDbSqlSession().selectById(HistoricProcessInstanceEntity.class, processInstanceId);
    }
    return null;
  }


  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (getHistoryManager().isHistoryEnabled()) {
      List<String> historicProcessInstanceIds = getDbSqlSession()
        .selectList("selectHistoricProcessInstanceIdsByProcessDefinitionId", processDefinitionId);
    
      for (String historicProcessInstanceId: historicProcessInstanceIds) {
        deleteHistoricProcessInstanceById(historicProcessInstanceId);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstanceById(String historicProcessInstanceId) {
    if (getHistoryManager().isHistoryEnabled()) {
      CommandContext commandContext = Context.getCommandContext();
      HistoricProcessInstanceEntity historicProcessInstance = findHistoricProcessInstance(historicProcessInstanceId);
      
      commandContext
        .getHistoricDetailEntityManager()
        .deleteHistoricDetailsByProcessInstanceId(historicProcessInstanceId);

      commandContext
        .getHistoricVariableInstanceEntityManager()
        .deleteHistoricVariableInstanceByProcessInstanceId(historicProcessInstanceId);
      
      commandContext
        .getHistoricActivityInstanceEntityManager()
        .deleteHistoricActivityInstancesByProcessInstanceId(historicProcessInstanceId);
      
      commandContext
        .getHistoricTaskInstanceEntityManager()
        .deleteHistoricTaskInstancesByProcessInstanceId(historicProcessInstanceId);
      
      commandContext
      	.getHistoricIdentityLinkEntityManager()
        .deleteHistoricIdentityLinksByProcInstance(historicProcessInstanceId);
      
      commandContext
        .getCommentEntityManager()
        .deleteCommentsByProcessInstanceId(historicProcessInstanceId);
      
      getDbSqlSession().delete(historicProcessInstance);
      
      // Also delete any sub-processes that may be active (ACT-821)
      HistoricProcessInstanceQueryImpl subProcessesQueryImpl = new HistoricProcessInstanceQueryImpl();
      subProcessesQueryImpl.superProcessInstanceId(historicProcessInstanceId);
      
      List<HistoricProcessInstance> selectList = getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteria", subProcessesQueryImpl);
      for(HistoricProcessInstance child : selectList) {
      	deleteHistoricProcessInstanceById(child.getId());
      }
    }
  }
  
  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return (Long) getDbSqlSession().selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery);
    }
    return Collections.EMPTY_LIST;
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesAndVariablesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      // paging doesn't work for combining process instances and variables due to an outer join, so doing it in-memory
      if (historicProcessInstanceQuery.getFirstResult() < 0 || historicProcessInstanceQuery.getMaxResults() <= 0) {
        return Collections.EMPTY_LIST;
      }
      
      int firstResult = historicProcessInstanceQuery.getFirstResult();
      int maxResults = historicProcessInstanceQuery.getMaxResults();
      
      // setting max results, limit to 20000 results for performance reasons
      if (historicProcessInstanceQuery.getProcessInstanceVariablesLimit() != null) {
        historicProcessInstanceQuery.setMaxResults(historicProcessInstanceQuery.getProcessInstanceVariablesLimit());
      } else {
        historicProcessInstanceQuery.setMaxResults(Context.getProcessEngineConfiguration().getHistoricProcessInstancesQueryLimit());
      }
      historicProcessInstanceQuery.setFirstResult(0);
      
      List<HistoricProcessInstance> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter("selectHistoricProcessInstancesWithVariablesByQueryCriteria", 
          historicProcessInstanceQuery, historicProcessInstanceQuery.getFirstResult(), historicProcessInstanceQuery.getMaxResults());
      
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

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricProcessInstanceByNativeQuery", parameterMap, firstResult, maxResults);    
  }

  public long findHistoricProcessInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricProcessInstanceCountByNativeQuery", parameterMap);
  }
}
