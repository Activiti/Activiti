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

package org.activiti.engine.impl.persistence.db;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.cfg.HistorySession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.history.HistoricActivityInstanceImpl;
import org.activiti.engine.impl.persistence.history.HistoricProcessInstanceImpl;


/**
 * @author Christian Stettler
 * @author Tom Baeyens
 */
public class DbHistorySession extends AbstractDbSession implements HistorySession, Session {

  public HistoricProcessInstanceImpl findHistoricProcessInstance(String processInstanceId) {
    return (HistoricProcessInstanceImpl) dbSqlSession.selectOne("selectHistoricProcessInstance", processInstanceId);
  }

  public HistoricActivityInstanceImpl findHistoricActivityInstance(String activityId, String processInstanceId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityId);
    parameters.put("processInstanceId", processInstanceId);

    return (HistoricActivityInstanceImpl) dbSqlSession.selectOne("selectHistoricActivityInstance", parameters);
  }

  public void insertHistoricProcessInstance(HistoricProcessInstanceImpl historicProcessInstance) {
    dbSqlSession.insert(historicProcessInstance);
  }

  public void deleteHistoricProcessInstance(HistoricProcessInstanceImpl historicProcessInstance) {
    dbSqlSession.delete(historicProcessInstance);
  }

  public void insertHistoricActivityInstance(HistoricActivityInstanceImpl historicActivityInstance) {
    dbSqlSession.insert(historicActivityInstance);
  }

  public void deleteHistoricActivityInstance(HistoricActivityInstanceImpl historicActivityInstance) {
    dbSqlSession.delete(historicActivityInstance);
  }

  public void close() {
  }

  public void flush() {
  }
}
