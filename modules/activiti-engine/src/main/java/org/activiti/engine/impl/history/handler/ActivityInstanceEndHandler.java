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

import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.event.EventListenerExecution;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceEndHandler implements EventListener {

  public void notify(EventListenerExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
//    HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
//
//    Date endTime = ClockUtil.getCurrentTime();
//    long durationInMillis = endTime.getTime() - historicActivityInstance.getStartTime().getTime();
//    historicActivityInstance.setEndTime(endTime);
//    historicActivityInstance.setDurationInMillis(durationInMillis);
  }

  public HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution) {
//    CommandContext commandContext = CommandContext.getCurrent();
//    HistoricProcessInstanceEntity historicProcessInstance = new HistoricActivityInstanceQueryImpl(commandContext)
//      .executionId(execution.getId())
//      .activityId(execution.getActivityId())
//      .listPage(0, 1);
//    
    return null;
  }
}
