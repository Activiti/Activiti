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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceEntity implements PersistentObject {

  protected String id; // == task.getId()
  protected String name;
  protected String assignee;
  protected String processInstanceId;
  protected String activityInstanceId;
  protected Date startTime;
  protected Date endTime;
  protected Long durationInMillis;

  // persistence //////////////////////////////////////////////////////////////
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("assignee", assignee);
    persistentState.put("endTime", endTime);
    persistentState.put("durationInMillis", durationInMillis);
    return persistentState;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  public Date getStartTime() {
    return startTime;
  }
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }
  public Date getEndTime() {
    return endTime;
  }
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }
  public Long getDurationInMillis() {
    return durationInMillis;
  }
  public void setDurationInMillis(Long durationInMillis) {
    this.durationInMillis = durationInMillis;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
}
