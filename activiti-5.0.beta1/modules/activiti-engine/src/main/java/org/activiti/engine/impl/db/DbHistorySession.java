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

import org.activiti.engine.Page;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.HistorySession;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
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

  public void deleteHistoricProcessInstance(String historicProcessInstanceId) {
    dbSqlSession.delete(HistoricProcessInstanceEntity.class, historicProcessInstanceId);
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

  public void close() {
  }

  public void flush() {
  }
}
