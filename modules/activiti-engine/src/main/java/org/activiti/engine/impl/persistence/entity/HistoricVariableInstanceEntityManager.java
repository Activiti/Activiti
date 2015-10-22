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

import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceEntityManager extends AbstractManager {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void deleteHistoricVariableInstanceByProcessInstanceId(String historicProcessInstanceId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {

      // Delete entries in DB
      List<HistoricVariableInstanceEntity> historicProcessVariables = (List) getDbSqlSession()
        .createHistoricVariableInstanceQuery()
        .processInstanceId(historicProcessInstanceId)
        .excludeVariableInitialization()
        .list();
      for (HistoricVariableInstanceEntity historicProcessVariable : historicProcessVariables) {
        historicProcessVariable.delete();
      }
      
      // Delete entries in Cache
      List<HistoricVariableInstanceEntity> cachedHistoricVariableInstances = getDbSqlSession().findInCache(HistoricVariableInstanceEntity.class);
      for (HistoricVariableInstanceEntity historicProcessVariable : cachedHistoricVariableInstances) {
        // Make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if (historicProcessInstanceId.equals(historicProcessVariable.getProcessInstanceId())) {
          historicProcessVariable.delete();
        }
      }
    }
  }
  
  public long findHistoricVariableInstanceCountByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricVariableInstanceCountByQueryCriteria", historicProcessVariableQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricVariableInstanceByQueryCriteria", historicProcessVariableQuery, page);
  }

  public HistoricVariableInstanceEntity findHistoricVariableInstanceByVariableInstanceId(String variableInstanceId) {
    return (HistoricVariableInstanceEntity) getDbSqlSession().selectOne("selectHistoricVariableInstanceByVariableInstanceId", variableInstanceId);
  }

  public void deleteHistoricVariableInstancesByTaskId(String taskId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricVariableInstance> historicProcessVariables = 
          new HistoricVariableInstanceQueryImpl().taskId(taskId).list();
      
      for (HistoricVariableInstance historicProcessVariable : historicProcessVariables) {
        ((HistoricVariableInstanceEntity) historicProcessVariable).delete();
      }
    }
  }
  
  @Override
  public void delete(PersistentObject persistentObject) {
    HistoricVariableInstanceEntity variableInstanceEntity = (HistoricVariableInstanceEntity) persistentObject;
    variableInstanceEntity.delete();
  }

  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricVariableInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findHistoricVariableInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricVariableInstanceCountByNativeQuery", parameterMap);
  }
}
