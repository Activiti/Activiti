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

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.RequestUtil;

/**
 * @author Tijs Rademakers
 */
public class SubTaskResponse {
  
  String assignee;
  String createTime;
  DelegationState delegationState;
  String description;
  String dueDate;
  String executionId;
  String id;
  String name;
  String owner;
  String parentTaskId;
  int priority;
  
  public SubTaskResponse(Task task) {
    setAssignee(task.getAssignee());
    setCreateTime(RequestUtil.dateToString(task.getCreateTime()));
    setDelegationState(task.getDelegationState());
    setDescription(task.getDescription());
    setDueDate(RequestUtil.dateToString(task.getDueDate()));
    setExecutionId(task.getExecutionId());
    setId(task.getId());
    setName(task.getName());
    setOwner(task.getOwner());
    setParentTaskId(task.getParentTaskId());
    setPriority(task.getPriority());
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public DelegationState getDelegationState() {
    return delegationState;
  }

  public void setDelegationState(DelegationState delegationState) {
    this.delegationState = delegationState;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDueDate() {
    return dueDate;
  }

  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

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

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }
}
