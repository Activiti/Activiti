/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;

/**
 * @author Christian Stettler
 */
public class HistoricActivityInstanceEntity extends HistoricScopeInstanceEntity implements HistoricActivityInstance {

  private static final long serialVersionUID = 1L;
  
  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String executionId;
  protected String assignee;
  protected String taskId;
  protected String calledProcessInstanceId;
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) new HashMap<String, Object>();
    persistentState.put("endTime", endTime);
    persistentState.put("durationInMillis", durationInMillis);
    persistentState.put("deleteReason", deleteReason);
    persistentState.put("executionId", executionId);
    persistentState.put("assignee", assignee);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getActivityId() {
    return activityId;
  }
  public String getActivityName() {
    return activityName;
  }

  public String getActivityType() {
    return activityType;
  }
  
  public String getExecutionId() {
    return executionId;
  }
  
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
  
  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }
  
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getTaskId() {
    return taskId;
  }
  
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getCalledProcessInstanceId() {
    return calledProcessInstanceId;
  }
  
  public void setCalledProcessInstanceId(String calledProcessInstanceId) {
    this.calledProcessInstanceId = calledProcessInstanceId;
  }

}
