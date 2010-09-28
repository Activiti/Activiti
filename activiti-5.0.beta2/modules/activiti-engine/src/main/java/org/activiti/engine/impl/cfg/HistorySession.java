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

package org.activiti.engine.impl.cfg;

import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.history.HistoricProcessInstanceEntity;


/**
 * @author Christian Stettler
 * @author Tom Baeyens
 */
public interface HistorySession {

  void deleteHistoricProcessInstance(String historicProcessInstanceId);
  HistoricProcessInstanceEntity findHistoricProcessInstance(String processInstanceId);

  void deleteHistoricActivityInstance(String historicActivityInstanceId);
  HistoricActivityInstanceEntity findHistoricActivityInstance(String activityId, String processInstanceId);
  
  long findHistoricProcessInstanceCountByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQueryImpl);
  List<HistoricProcessInstance> findHistoricProcessInstancesByQueryCriteria(HistoricProcessInstanceQueryImpl historicProcessInstanceQueryImpl, Page page);

  long findHistoricActivityInstanceCountByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl);
  List<HistoricActivityInstance> findHistoricActivityInstancesByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl, Page page);
}
