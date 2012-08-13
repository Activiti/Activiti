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

import org.activiti.engine.history.HistoricProcessVariable;
import org.activiti.engine.impl.HistoricProcessVariableQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricProcessVariableManager extends AbstractHistoricManager {

  public void deleteHistoricProcessVariable(HistoricProcessVariableEntity historicProcessVariable) {
    String byteArrayValueId = historicProcessVariable.getByteArrayValueId();
    if (byteArrayValueId != null) {
        // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
        // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
        // @see also HistoricVariableInstanceEntity
      historicProcessVariable.getByteArrayValue();
      Context
        .getCommandContext()
        .getSession(DbSqlSession.class)
        .delete(ByteArrayEntity.class, byteArrayValueId);
    }
    getDbSqlSession().delete(HistoricProcessVariableEntity.class, historicProcessVariable.getId());
  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessVariableByProcessInstanceId(String historicProcessInstanceId) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_VARIABLE) {
      HistoricProcessVariableManager historicProcessVariableManager = Context
              .getCommandContext()
              .getHistoricProcessVariableManager();

      // delete entries in DB
      List<HistoricProcessVariableEntity> historicProcessVariables = (List) getDbSqlSession()
        .createHistoricProcessVariableQuery()
        .processInstanceId(historicProcessInstanceId)
        .list();
      for (HistoricProcessVariableEntity historicProcessVariable : historicProcessVariables) {
        historicProcessVariableManager.deleteHistoricProcessVariable(historicProcessVariable);
      }
      
      //delete enrties in Cache
      List<HistoricProcessVariableEntity> cachedHistoricProcessVariables = getDbSqlSession().findInCache(HistoricProcessVariableEntity.class);
      for (HistoricProcessVariableEntity historicProcessVariable : cachedHistoricProcessVariables) {
        // make sure we only delete the right ones (as we cannot make a proper query in the cache)
        if (historicProcessVariable.getProcessInstanceId().equals(historicProcessInstanceId )) {
          historicProcessVariableManager.deleteHistoricProcessVariable(historicProcessVariable);
        }
      }
    }
  }
  
  public long findHistoricProcessVariableCountByQueryCriteria(HistoricProcessVariableQueryImpl historicProcessVariableQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricProcessVariableCountByQueryCriteria", historicProcessVariableQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessVariable> findHistoricProcessVariableByQueryCriteria(HistoricProcessVariableQueryImpl historicProcessVariableQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricProcessVariableByQueryCriteria", historicProcessVariableQuery, page);
  }

  public void deleteHistoricProcessVariableByTaskId(String taskId) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_VARIABLE) {
      HistoricProcessVariableQueryImpl historicProcessVariableQuery = 
        (HistoricProcessVariableQueryImpl) new HistoricProcessVariableQueryImpl().taskId(taskId);
      List<HistoricProcessVariable> historicProcessVariables = historicProcessVariableQuery.list();
      for(HistoricProcessVariable historicProcessVariable : historicProcessVariables) {
        ((HistoricProcessVariableEntity) historicProcessVariable).delete();
      }
    }
  }
}
