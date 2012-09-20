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

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceManager extends AbstractHistoricManager {

  @SuppressWarnings("unchecked")
  public void deleteHistoricVariableInstanceByProcessInstanceId(String historicProcessInstanceId) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      HistoricVariableInstanceManager historicProcessVariableManager = Context
              .getCommandContext()
              .getHistoricVariableInstanceManager();

      // delete entries in DB
      List<HistoricVariableInstanceEntity> historicProcessVariables = (List) getDbSqlSession()
        .createHistoricVariableInstanceQuery()
        .processInstanceId(historicProcessInstanceId)
        .list();
      for (HistoricVariableInstanceEntity historicProcessVariable : historicProcessVariables) {
        historicProcessVariable.delete();
      }
      
      //delete enrties in Cache
      List<HistoricVariableInstanceEntity> cachedHistoricVariableInstances = getDbSqlSession().findInCache(HistoricVariableInstanceEntity.class);
      for (HistoricVariableInstanceEntity historicProcessVariable : cachedHistoricVariableInstances) {
        // make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if (historicProcessVariable.getProcessInstanceId().equals(historicProcessInstanceId )) {
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
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      HistoricVariableInstanceQueryImpl historicProcessVariableQuery = 
        (HistoricVariableInstanceQueryImpl) new HistoricVariableInstanceQueryImpl().taskId(taskId);
      List<HistoricVariableInstance> historicProcessVariables = historicProcessVariableQuery.list();
      for(HistoricVariableInstance historicProcessVariable : historicProcessVariables) {
        ((HistoricVariableInstanceEntity) historicProcessVariable).delete();
      }
    }
  }
}
