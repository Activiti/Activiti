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

import java.util.Date;

import org.activiti.rest.common.util.DateToStringSerializer;
import org.activiti.rest.service.api.engine.variable.RestVariable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Tijs Rademakers
 */
public class HistoricDetailResponse {
  
  public static String FORM_PROPERTY = "formProperty";
  public static String VARIABLE_UPDATE = "variableUpdate";
  
  protected String id;
  protected String processInstanceId;
  protected String processInstanceUrl;
  protected String executionId;
  protected String activityInstanceId;
  protected String taskId;
  protected String taskUrl;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date time;
  protected String detailType;
  
  // Historic variable update properties
  protected Integer revision;
  protected RestVariable variable;
  
  // Form properties
  protected String propertyId;
  protected String propertyValue;
  
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
  public String getProcessInstanceUrl() {
    return processInstanceUrl;
  }
  public void setProcessInstanceUrl(String processInstanceUrl) {
    this.processInstanceUrl = processInstanceUrl;
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
  public String getTaskUrl() {
    return taskUrl;
  }
  public void setTaskUrl(String taskUrl) {
    this.taskUrl = taskUrl;
  }
  public Date getTime() {
    return time;
  }
  public void setTime(Date time) {
    this.time = time;
  }
  public String getDetailType() {
    return detailType;
  }
  public void setDetailType(String detailType) {
    this.detailType = detailType;
  }
  public Integer getRevision() {
    return revision;
  }
  public void setRevision(Integer revision) {
    this.revision = revision;
  }
  public RestVariable getVariable() {
    return variable;
  }
  public void setVariable(RestVariable variable) {
    this.variable = variable;
  }
  public String getPropertyId() {
    return propertyId;
  }
  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }
  public String getPropertyValue() {
    return propertyValue;
  }
  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }
}
