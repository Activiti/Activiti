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

package org.activiti.rest.api.task;

import java.util.Date;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;

/**
 * @author Frederik Heremans
 */
public class TaskResponse {

  private String id;
  private String url;
  private String owner;
  private String assignee;
  private String delegationState;
  private String name;
  private String description;
  private Date createTime;
  private Date dueDate;
  private int priority;
  private boolean suspended;
  private String taskDefinitionKey;
  
  // References to other resources
  private String parentTask;
  private String execution;
  private String processInstance;
  private String processDefinition;
  
  
  public TaskResponse(Task task) {
    setId(task.getId());
    setOwner(task.getOwner());
    setAssignee(task.getAssignee());
    setDelegationState(getDelegationStateString(task.getDelegationState()));
    setName(task.getName());
    setDescription(task.getDescription());
    setCreateTime(task.getCreateTime());
    setDueDate(task.getDueDate());
    setPriority(task.getPriority());
    setSuspended(task.isSuspended());
    setTaskDefinitionKey(task.getTaskDefinitionKey());
  }
  
  protected String getDelegationStateString(DelegationState state) {
    String result = null;
    if(state != null) {
      result = state.toString().toLowerCase();
    }
    return result;
  }

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
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
  public String getDelegationState() {
    return delegationState;
  }
  public void setDelegationState(String delegationState) {
    this.delegationState = delegationState;
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
  public Date getCreateTime() {
    return createTime;
  }
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  public boolean isSuspended() {
    return suspended;
  }
  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }
  public String getParentTask() {
    return parentTask;
  }
  public void setParentTask(String parentTask) {
    this.parentTask = parentTask;
  }
  public String getExecution() {
    return execution;
  }
  public void setExecution(String execution) {
    this.execution = execution;
  }
  public String getProcessInstance() {
    return processInstance;
  }
  public void setProcessInstance(String processInstance) {
    this.processInstance = processInstance;
  }
  public String getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(String processDefinition) {
    this.processDefinition = processDefinition;
  }
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }
}
