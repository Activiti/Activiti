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

package org.activiti.rest.api.history;

import java.util.Date;
import java.util.List;

import org.activiti.rest.api.engine.variable.QueryVariable;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;


/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceQueryRequest {

  private String taskId;
  private String processInstanceId;
  private String processBusinessKey;
  private String processDefinitionId;
  private String processDefinitionKey;
  private String processDefinitionName;
  private String executionId;
  private String taskName;
  private String taskNameLike;
  private String taskDescription;
  private String taskDescriptionLike;
  private String taskDefinitionKey;
  private String taskDeleteReason;
  private String taskDeleteReasonLike;
  private String taskAssignee;
  private String taskAssigneeLike;
  private String taskOwner;
  private String taskOwnerLike;
  private String taskInvolvedUser;
  private Integer taskPriority;
  private Boolean finished;
  private Boolean processFinished;
  private String parentTaskId;
  private Date dueDate;
  private Date dueDateAfter;
  private Date dueDateBefore;
  private Date taskCreatedOn;
  private Boolean includeTaskLocalVariables;
  private Boolean includeProcessVariables;
  private List<QueryVariable> taskVariables;
  private List<QueryVariable> processVariables;

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessBusinessKey() {
    return processBusinessKey;
  }

  public void setProcessBusinessKey(String processBusinessKey) {
    this.processBusinessKey = processBusinessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public String getTaskNameLike() {
    return taskNameLike;
  }

  public void setTaskNameLike(String taskNameLike) {
    this.taskNameLike = taskNameLike;
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }

  public String getTaskDescriptionLike() {
    return taskDescriptionLike;
  }

  public void setTaskDescriptionLike(String taskDescriptionLike) {
    this.taskDescriptionLike = taskDescriptionLike;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  public String getTaskDeleteReason() {
    return taskDeleteReason;
  }

  public void setTaskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
  }

  public String getTaskDeleteReasonLike() {
    return taskDeleteReasonLike;
  }

  public void setTaskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
  }

  public String getTaskAssignee() {
    return taskAssignee;
  }

  public void setTaskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
  }

  public String getTaskAssigneeLike() {
    return taskAssigneeLike;
  }

  public void setTaskAssigneeLike(String taskAssigneeLike) {
    this.taskAssigneeLike = taskAssigneeLike;
  }

  public String getTaskOwner() {
    return taskOwner;
  }

  public void setTaskOwner(String taskOwner) {
    this.taskOwner = taskOwner;
  }

  public String getTaskOwnerLike() {
    return taskOwnerLike;
  }

  public void setTaskOwnerLike(String taskOwnerLike) {
    this.taskOwnerLike = taskOwnerLike;
  }

  public String getTaskInvolvedUser() {
    return taskInvolvedUser;
  }

  public void setTaskInvolvedUser(String taskInvolvedUser) {
    this.taskInvolvedUser = taskInvolvedUser;
  }

  public Integer getTaskPriority() {
    return taskPriority;
  }

  public void setTaskPriority(Integer taskPriority) {
    this.taskPriority = taskPriority;
  }

  public Boolean getFinished() {
    return finished;
  }

  public void setFinished(Boolean finished) {
    this.finished = finished;
  }

  public Boolean getProcessFinished() {
    return processFinished;
  }

  public void setProcessFinished(Boolean processFinished) {
    this.processFinished = processFinished;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public Date getDueDateAfter() {
    return dueDateAfter;
  }

  public void setDueDateAfter(Date dueDateAfter) {
    this.dueDateAfter = dueDateAfter;
  }

  public Date getDueDateBefore() {
    return dueDateBefore;
  }

  public void setDueDateBefore(Date dueDateBefore) {
    this.dueDateBefore = dueDateBefore;
  }

  public Date getTaskCreatedOn() {
    return taskCreatedOn;
  }

  public void setTaskCreatedOn(Date taskCreatedOn) {
    this.taskCreatedOn = taskCreatedOn;
  }

  public Boolean getIncludeTaskLocalVariables() {
    return includeTaskLocalVariables;
  }

  public void setIncludeTaskLocalVariables(Boolean includeTaskLocalVariables) {
    this.includeTaskLocalVariables = includeTaskLocalVariables;
  }

  public Boolean getIncludeProcessVariables() {
    return includeProcessVariables;
  }

  public void setIncludeProcessVariables(Boolean includeProcessVariables) {
    this.includeProcessVariables = includeProcessVariables;
  }

  @JsonTypeInfo(use=Id.CLASS, defaultImpl=QueryVariable.class)  
  public List<QueryVariable> getTaskVariables() {
    return taskVariables;
  }
  
  public void setTaskVariables(List<QueryVariable> taskVariables) {
    this.taskVariables = taskVariables;
  }
  
  @JsonTypeInfo(use=Id.CLASS, defaultImpl=QueryVariable.class)  
  public List<QueryVariable> getProcessVariables() {
    return processVariables;
  }
  
  public void setProcessVariables(List<QueryVariable> processVariables) {
    this.processVariables = processVariables;
  }
}
