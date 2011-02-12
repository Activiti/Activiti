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

package org.activiti.engine.impl.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.HistorySession;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.history.HistoricDetailEntity;
import org.activiti.engine.impl.history.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.interceptor.Session;


/**
 * @author Christian Stettler
 * @author Tom Baeyens
 */
public class DbHistorySession extends AbstractDbSession implements HistorySession, Session {

  public void insertHistoricProcessInstance(HistoricProcessInstanceEntity historicProcessInstance) {
    dbSqlSession.insert(historicProcessInstance);
  }

  @SuppressWarnings("unchecked")
  public void deleteHistoricProcessInstance(String historicProcessInstanceId) {
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      List<HistoricDetailEntity> historicDetails = (List) new HistoricDetailQueryImpl(Context.getCommandContext())
        .processInstanceId(historicProcessInstanceId)
        .list();
      for (HistoricDetailEntity historicDetail: historicDetails) {
        historicDetail.delete();
      }
    }
    
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      dbSqlSession.delete("deleteHistoricActivityInstancesByProcessInstanceId", historicProcessInstanceId);
    }
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      dbSqlSession.delete("deleteHistoricTaskInstancesByProcessInstanceId", historicProcessInstanceId);
    }
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      dbSqlSession.delete(HistoricProcessInstanceEntity.class, historicProcessInstanceId);
    }
  }

  public HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId) {
    return (HistoricProcessInstanceEntity) dbSqlSession.selectById(HistoricProcessInstanceEntity.class, processInstanceId);
  }

  public void insertHistoricActivityInstance(HistoricActivityInstanceEntity historicActivityInstance) {
    dbSqlSession.insert(historicActivityInstance);
  }

  public void deleteHistoricActivityInstance(String historicActivityInstanceId) {
    dbSqlSession.delete(HistoricActivityInstanceEntity.class, historicActivityInstanceId);
  }

  public HistoricActivityInstanceEntity findHistoricActivityInstance(String activityId, String processInstanceId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityId);
    parameters.put("processInstanceId", processInstanceId);
  
    return (HistoricActivityInstanceEntity) dbSqlSession.selectOne("selectHistoricActivityInstance", parameters);
  }

  public long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery) {
    return (Long) dbSqlSession.selectOne("selectHistoricProcessInstanceCountByQueryCriteria", historicProcessInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQuery, Page page) {
    return dbSqlSession.selectList("selectHistoricProcessInstancesByQueryCriteria", historicProcessInstanceQuery, page);
  }

  public long findHistoricActivityInstanceCountByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery) {
    return (Long) dbSqlSession.selectOne("selectHistoricActivityInstanceCountByQueryCriteria", historicActivityInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> findHistoricActivityInstancesByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery, Page page) {
    return dbSqlSession.selectList("selectHistoricActivityInstancesByQueryCriteria", historicActivityInstanceQuery, page);
  }

  public long findHistoricDetailCountByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery) {
    return (Long) dbSqlSession.selectOne("selectHistoricDetailCountByQueryCriteria", historicVariableUpdateQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricDetail> findHistoricDetailsByQueryCriteria(HistoricDetailQueryImpl historicVariableUpdateQuery, Page page) {
    return dbSqlSession.selectList("selectHistoricDetailsByQueryCriteria", historicVariableUpdateQuery, page);
  }

  public void close() {
  }

  public void flush() {
  }
}
