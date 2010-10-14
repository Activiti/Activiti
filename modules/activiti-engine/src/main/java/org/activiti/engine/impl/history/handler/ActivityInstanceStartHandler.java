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

package org.activiti.engine.impl.history.handler;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.event.EventListenerExecution;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceStartHandler implements EventListener {

  protected String activityType;
  
  public ActivityInstanceStartHandler(String activityType) {
    this.activityType = activityType;
  }

  public void notify(EventListenerExecution execution) {
    CommandContext commandContext = CommandContext.getCurrent();
    IdGenerator idGenerator = commandContext.getProcessEngineConfiguration().getIdGenerator();
    
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    String processDefinitionId = executionEntity.getProcessDefinitionId();
    String processInstanceId = executionEntity.getProcessInstanceId();
    String executionId = execution.getId();

    HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
    historicActivityInstance.setId(Long.toString(idGenerator.getNextId()));
    historicActivityInstance.setProcessDefinitionId(processDefinitionId);
    historicActivityInstance.setProcessInstanceId(processInstanceId);
    historicActivityInstance.setExecutionId(executionId);
    historicActivityInstance.setActivityId(executionEntity.getActivityId());
    historicActivityInstance.setActivityName((String) executionEntity.getActivity().getProperty("name"));
    historicActivityInstance.setActivityType((String) executionEntity.getActivity().getProperty("type"));
    historicActivityInstance.setStartTime(ClockUtil.getCurrentTime());
    
    commandContext
      .getDbSqlSession()
      .insert(historicActivityInstance);
  }
}
