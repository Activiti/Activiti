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

package org.activiti.rest.service.api.history;

import org.activiti.rest.common.api.PaginateRequest;



/**
 * @author Tijs Rademakers
 */
public class HistoricDetailQueryRequest extends PaginateRequest {

  private String id;
  private String processInstanceId;
  private String executionId;
  private String activityInstanceId;
  private String taskId;
  private Boolean selectOnlyFormProperties;
  private Boolean selectOnlyVariableUpdates;
  
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

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Boolean getSelectOnlyFormProperties() {
    return selectOnlyFormProperties;
  }

  public void setSelectOnlyFormProperties(Boolean selectOnlyFormProperties) {
    this.selectOnlyFormProperties = selectOnlyFormProperties;
  }

  public Boolean getSelectOnlyVariableUpdates() {
    return selectOnlyVariableUpdates;
  }

  public void setSelectOnlyVariableUpdates(Boolean selectOnlyVariableUpdates) {
    this.selectOnlyVariableUpdates = selectOnlyVariableUpdates;
  }
}
