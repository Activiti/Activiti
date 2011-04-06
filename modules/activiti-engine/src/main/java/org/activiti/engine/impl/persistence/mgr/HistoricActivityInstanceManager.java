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

package org.activiti.engine.impl.persistence.mgr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;


/**
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricActivityInstancesByProcessInstanceId(String historicProcessInstanceId) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      getPersistenceSession().delete("deleteHistoricActivityInstancesByProcessInstanceId", historicProcessInstanceId);
    }
  }
  
  public void insertHistoricActivityInstance(HistoricActivityInstanceEntity historicActivityInstance) {
    getPersistenceSession().insert(historicActivityInstance);
  }

  public void deleteHistoricActivityInstance(String historicActivityInstanceId) {
    getPersistenceSession().delete(HistoricActivityInstanceEntity.class, historicActivityInstanceId);
  }

  public HistoricActivityInstanceEntity findHistoricActivityInstance(String activityId, String processInstanceId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityId);
    parameters.put("processInstanceId", processInstanceId);
  
    return (HistoricActivityInstanceEntity) getPersistenceSession().selectOne("selectHistoricActivityInstance", parameters);
  }

  public long findHistoricActivityInstanceCountByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery) {
    return (Long) getPersistenceSession().selectOne("selectHistoricActivityInstanceCountByQueryCriteria", historicActivityInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> findHistoricActivityInstancesByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery, Page page) {
    return getPersistenceSession().selectList("selectHistoricActivityInstancesByQueryCriteria", historicActivityInstanceQuery, page);
  }
}
