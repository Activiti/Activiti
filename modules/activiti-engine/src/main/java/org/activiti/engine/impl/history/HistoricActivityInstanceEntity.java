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

package org.activiti.engine.impl.history;

import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;

/**
 * @author Christian Stettler
 */
public class HistoricActivityInstanceEntity extends HistoricScopeInstanceEntity implements HistoricActivityInstance {

  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String executionId;
  
  @SuppressWarnings("unchecked")
  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) super.getPersistentState();
    persistentState.put("executionId", executionId);
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
}
