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

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailEntity implements HistoricDetail, PersistentObject {

  protected String id;
  protected String processInstanceId;
  protected String activityInstanceId;
  protected String taskId;
  protected String executionId;
  protected Date time;

  public Object getPersistentState() {
    // details are not updatable so we always provide the same object as the state
    return HistoricDetailEntity.class;
  }
  
  public void delete() {
    DbSqlSession dbSqlSession = CommandContext
      .getCurrent()
      .getDbSqlSession();

    dbSqlSession.delete(HistoricDetailEntity.class, id);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  
  public String getExecutionId() {
    return executionId;
  }

  
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public Date getTime() {
    return time;
  }
  
  public void setTime(Date time) {
    this.time = time;
  }

  
  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
}
