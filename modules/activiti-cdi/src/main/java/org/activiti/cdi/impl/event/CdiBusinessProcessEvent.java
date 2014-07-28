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
package org.activiti.cdi.impl.event;

import java.util.Date;

import org.activiti.cdi.BusinessProcessEvent;
import org.activiti.cdi.BusinessProcessEventType;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * 
 * @author Daniel Meyer
 */
public class CdiBusinessProcessEvent implements BusinessProcessEvent {

  protected final String activityId;
  protected final ProcessDefinition processDefinition;
  protected final String transitionName;
  protected final String processInstanceId;
  protected final String executionId;
  protected final BusinessProcessEventType type;
  protected final Date timeStamp;
  protected final VariableScope variableScope;
  
  public CdiBusinessProcessEvent(String activityId, 
                                     String transitionName,
                                     ProcessDefinition processDefinition, 
                                     VariableScope execution,
                                     BusinessProcessEventType type,
                                     String processInstanceId,
                                     String executionId,
                                     Date timeStamp) {
      this.activityId = activityId;
      this.transitionName = transitionName;
      this.processInstanceId = processInstanceId;
      this.executionId = executionId;
      this.type = type;
      this.timeStamp = timeStamp;
      this.variableScope = execution;
      this.processDefinition = processDefinition;
  }
  
  @Override
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  @Override
  public String getActivityId() {
    return activityId;
  }

  @Override
  public String getTransitionName() {
    return transitionName;
  }

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
  public String getExecutionId() {
    return executionId;
  }

  @Override
  public BusinessProcessEventType getType() {
    return type;
  }

  @Override
  public Date getTimeStamp() {
    return timeStamp;
  }

  @Override
  public String toString() {
    return "Event '" + processDefinition.getKey() + "' ['" + type + "', " + (type == BusinessProcessEventType.TAKE ? transitionName : activityId) + "]";
  }
  
  @Override
  public VariableScope getVariableScope() {
    return variableScope;
  }

}
