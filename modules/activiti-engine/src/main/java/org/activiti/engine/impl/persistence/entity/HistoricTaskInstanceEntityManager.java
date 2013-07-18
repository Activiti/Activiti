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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceEntityManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public void deleteHistoricTaskInstancesByProcessInstanceId(String processInstanceId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      List<String> taskInstanceIds = (List<String>) getDbSqlSession().selectList("selectHistoricTaskInstanceIdsByProcessInstanceId", processInstanceId);
      for (String taskInstanceId: taskInstanceIds) {
        deleteHistoricTaskInstanceById(taskInstanceId);
      }
    }
  }

  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    if (getHistoryManager().isHistoryEnabled()) {
      return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery, Page page) {
    if (getHistoryManager().isHistoryEnabled()) {
      return getDbSqlSession().selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }
  
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesAndVariablesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery, Page page) {
    if (getHistoryManager().isHistoryEnabled()) {
      return getDbSqlSession().selectList("selectHistoricTaskInstancesWithVariablesByQueryCriteria", historicTaskInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }
  
  public HistoricTaskInstanceEntity findHistoricTaskInstanceById(String taskId) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("Invalid historic task id : null");
    }
    if (getHistoryManager().isHistoryEnabled()) {
      return (HistoricTaskInstanceEntity) getDbSqlSession().selectOne("selectHistoricTaskInstance", taskId);
    }
    return null;
  }
  
  public void deleteHistoricTaskInstanceById(String taskId) {
    if (getHistoryManager().isHistoryEnabled()) {
      HistoricTaskInstanceEntity historicTaskInstance = findHistoricTaskInstanceById(taskId);
      if(historicTaskInstance!=null) {
        CommandContext commandContext = Context.getCommandContext();
        
        commandContext
          .getHistoricDetailEntityManager()
          .deleteHistoricDetailsByTaskId(taskId);

        commandContext
          .getHistoricVariableInstanceEntityManager()
          .deleteHistoricVariableInstancesByTaskId(taskId);

        commandContext
          .getCommentEntityManager()
          .deleteCommentsByTaskId(taskId);
        
        commandContext
          .getAttachmentEntityManager()
          .deleteAttachmentsByTaskId(taskId);
        
        commandContext.getHistoricIdentityLinkEntityManager()
          .deleteHistoricIdentityLinksByTaskId(taskId);
      
        getDbSqlSession().delete(historicTaskInstance);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricTaskInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricTaskInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByNativeQuery", parameterMap);
  }
}
