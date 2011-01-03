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

package org.activiti.engine.impl.history;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceEntity extends HistoricScopeInstanceEntity implements HistoricTaskInstance, PersistentObject {

  protected String executionId;
  protected String name;
  protected String description;
  protected String assignee;

  public HistoricTaskInstanceEntity() {
  }

  public HistoricTaskInstanceEntity(TaskEntity task, ExecutionEntity execution) {
    this.id = task.getId();
    if (execution!=null) {
      this.processDefinitionId = execution.getProcessDefinitionId();
      this.processInstanceId = execution.getProcessInstanceId();
      this.executionId = execution.getId();
    }
    this.name = task.getName();
    this.description = task.getDescription();
    this.assignee = task.getAssignee();
    this.startTime = ClockUtil.getCurrentTime();
  }

  // persistence //////////////////////////////////////////////////////////////
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("assignee", assignee);
    persistentState.put("endTime", endTime);
    persistentState.put("durationInMillis", durationInMillis);
    persistentState.put("deleteReason", deleteReason);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  
}
