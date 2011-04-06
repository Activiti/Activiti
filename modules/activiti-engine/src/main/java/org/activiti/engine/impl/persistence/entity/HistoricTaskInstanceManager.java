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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceManager extends AbstractHistoricManager {

  public void deleteHistoricTaskInstancesByProcessInstanceId(String historicProcessInstanceId) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      getPersistenceSession().delete("deleteHistoricTaskInstancesByProcessInstanceId", historicProcessInstanceId);
    }
  }

  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    return (Long) getPersistenceSession().selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery, Page page) {
    return getPersistenceSession().selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery, page);
  }
  
  public HistoricTaskInstanceEntity findHistoricTaskInstanceById(String id) {
    if (id == null) {
      throw new ActivitiException("Invalid historic task id : null");
    }
    return (HistoricTaskInstanceEntity) getPersistenceSession().selectOne("selectHistoricTaskInstance", id);
  }
  
  public void deleteHistoricTaskInstance(String taskId) {
    HistoricTaskInstanceEntity historicTaskInstance = findHistoricTaskInstanceById(taskId);
    if(historicTaskInstance == null) {
      throw new ActivitiException("No historic task instance found for id '" + taskId + "'");
    }
    
    historicTaskInstance.delete();
  }

}
