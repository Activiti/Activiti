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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.RequestUtil;

/**
 * @author Tijs Rademakers
 */
public class TaskResponse {
  
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
  String processDefinitionId;
  String processInstanceId;
  String taskDefinitionKey;
  String formResourceKey;
  List<SubTaskResponse> subTaskList = new ArrayList<SubTaskResponse>();
  List<IdentityLinkResponse> identityLinkList = new ArrayList<IdentityLinkResponse>();
  List<AttachmentResponse> attachmentList = new ArrayList<AttachmentResponse>();
  
  public TaskResponse(Task task) {
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
    setProcessDefinitionId(task.getProcessDefinitionId());
    setProcessInstanceId(task.getProcessInstanceId());
    setTaskDefinitionKey(task.getTaskDefinitionKey());
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

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  public String getFormResourceKey() {
    return formResourceKey;
  }

  public void setFormResourceKey(String formResourceKey) {
    this.formResourceKey = formResourceKey;
  }

  public List<SubTaskResponse> getSubTaskList() {
    return subTaskList;
  }

  public void setSubTaskList(List<SubTaskResponse> subTaskList) {
    this.subTaskList = subTaskList;
  }
  
  public void addSubTask(SubTaskResponse subTask) {
    this.subTaskList.add(subTask);
  }

  public List<IdentityLinkResponse> getIdentityLinkList() {
    return identityLinkList;
  }

  public void setIdentityLinkList(List<IdentityLinkResponse> identityLinkList) {
    this.identityLinkList = identityLinkList;
  }
  
  public void addIdentityLink(IdentityLinkResponse link) {
    this.identityLinkList.add(link);
  }

  public List<AttachmentResponse> getAttachmentList() {
    return attachmentList;
  }

  public void setAttachmentList(List<AttachmentResponse> attachmentList) {
    this.attachmentList = attachmentList;
  }
  
  public void addAttachment(AttachmentResponse attachment) {
    this.attachmentList.add(attachment);
  }
}
