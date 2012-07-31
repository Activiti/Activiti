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

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricProcessInstanceManager extends AbstractHistoricManager {

  public HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return (HistoricProcessInstanceEntity) getDbSqlSession().selectById(HistoricProcessInstanceEntity.class, processInstanceId);
    }
    return null;
  }


  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstanceByProcessDefinitionId(String processDefinitionId) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<String> historicProcessInstanceIds = getDbSqlSession()
        .selectList("selectHistoricProcessInstanceIdsByProcessDefinitionId", processDefinitionId);
    
      for (String historicProcessInstanceId: historicProcessInstanceIds) {
        deleteHistoricProcessInstanceById(historicProcessInstanceId);
      }
    }
  }
  
  public void deleteHistoricProcessInstanceById(String historicProcessInstanceId) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      CommandContext commandContext = Context.getCommandContext();
      
      commandContext
        .getHistoricDetailManager()
        .deleteHistoricDetailsByProcessInstanceId(historicProcessInstanceId);

      commandContext
        .getHistoricProcessVariableManager()
        .deleteHistoricProcessVariableByProcessInstanceId(historicProcessInstanceId);
      
      commandContext
        .getHistoricActivityInstanceManager()
        .deleteHistoricActivityInstancesByProcessInstanceId(historicProcessInstanceId);
      
      commandContext
        .getHistoricTaskInstanceManager()
        .deleteHistoricTaskInstancesByProcessInstanceId(historicProcessInstanceId);

      getDbSqlSession().delete(HistoricProcessInstanceEntity.class, historicProcessInstanceId);
    }
  }
  
  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return (Long) getDbSqlSession().selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery, Page page) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return getDbSqlSession().selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }
}
