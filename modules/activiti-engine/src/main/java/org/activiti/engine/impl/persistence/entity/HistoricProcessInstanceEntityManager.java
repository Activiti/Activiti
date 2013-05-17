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
import org.activiti.engine.impl.Page;
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
      
      commandContext.getHistoricIdentityLinkEntityManager()
        .deleteHistoricIdentityLinksByProcInstance(historicProcessInstanceId);

      getDbSqlSession().delete(historicProcessInstance);
    }
  }
  
  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return (Long) getDbSqlSession().selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery, Page page) {
    if (getHistoryManager().isHistoryEnabled()) {
      return getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery, page);
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
