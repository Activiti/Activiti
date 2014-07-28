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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.rest.common.util.DateToStringSerializer;
import org.activiti.rest.service.api.engine.variable.RestVariable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceResponse {
  
  protected String id;
  protected String processDefinitionId;
  protected String processDefinitionUrl;
  protected String processInstanceId;
  protected String processInstanceUrl;
  protected String executionId;
  protected String name;
  protected String description;
  protected String deleteReason;
  protected String owner;
  protected String assignee;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date startTime;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date endTime;
  protected Long durationInMillis;
  protected Long workTimeInMillis;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date claimTime;
  protected String taskDefinitionKey;
  protected String formKey;
  protected Integer priority;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date dueDate;
  protected String parentTaskId;
  protected String url;
  protected List<RestVariable> variables = new ArrayList<RestVariable>();
  protected String tenantId; 
  protected String category; 
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  public String getProcessDefinitionUrl() {
    return processDefinitionUrl;
  }
  public void setProcessDefinitionUrl(String processDefinitionUrl) {
    this.processDefinitionUrl = processDefinitionUrl;
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
  public String getDeleteReason() {
    return deleteReason;
  }
  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
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
  public Long getWorkTimeInMillis() {
    return workTimeInMillis;
  }
  public void setWorkTimeInMillis(Long workTimeInMillis) {
    this.workTimeInMillis = workTimeInMillis;
  }
  public Date getClaimTime() {
    return claimTime;
  }
  public void setClaimTime(Date claimTime) {
    this.claimTime = claimTime;
  }
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }
  public String getFormKey() {
    return formKey;
  }
  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }
  public Integer getPriority() {
    return priority;
  }
  public void setPriority(Integer priority) {
    this.priority = priority;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public String getParentTaskId() {
    return parentTaskId;
  }
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public List<RestVariable> getVariables() {
    return variables;
  }
  public void setVariables(List<RestVariable> variables) {
    this.variables = variables;
  }
  public void addVariable(RestVariable variable) {
    variables.add(variable);
  }
  public void setTenantId(String tenantId) {
	  this.tenantId = tenantId;
  }
  public String getTenantId() {
	  return tenantId;
  }
  public void setCategory(String category) {
	  this.category = category;
  }
  public String getCategory() {
	  return category;
  }
}
