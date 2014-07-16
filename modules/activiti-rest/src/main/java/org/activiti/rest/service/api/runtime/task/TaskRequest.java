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

package org.activiti.rest.service.api.runtime.task;

import java.util.Date;


/**
 * Request body containing a task and general properties.
 * 
 * @author Frederik Heremans
 */
public class TaskRequest {

  private String owner;
  private String assignee;
  private String delegationState;
  private String name;
  private String description;
  private Date dueDate;
  private int priority;
  private String parentTaskId;
  private String category;
  private String tenantId;
  private String formKey;
  
  private boolean ownerSet = false;
  private boolean assigneeSet = false;
  private boolean delegationStateSet = false;
  private boolean nameSet = false;
  private boolean descriptionSet = false;
  private boolean duedateSet = false;
  private boolean prioritySet = false;
  private boolean parentTaskIdSet = false;
  private boolean categorySet = false;
  private boolean tenantIdSet = false;
  private boolean formKeySet = false;
  
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
    ownerSet = true;
  }
  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
    assigneeSet = true;
  }
  public String getDelegationState() {
    return delegationState;
  }
  public void setDelegationState(String delegationState) {
    this.delegationState = delegationState;
    delegationStateSet = true;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
    nameSet = true;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
    descriptionSet = true;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
    duedateSet = true;
  }
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
    prioritySet = true;
  }
  public String getParentTaskId() {
    return parentTaskId;
  }
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
    parentTaskIdSet = true;
  }
  public void setCategory(String category) {
	  this.category = category;
	  categorySet = true;
  }
  public String getCategory() {
	  return category;
  }
  public String getTenantId() {
    return tenantId;
  }
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
    tenantIdSet = true;
  }
  public String getFormKey() {
    return formKey;
  }
  public void setFormKey(String formKey) {
    this.formKey = formKey;
    formKeySet = true;
  }
  
  public boolean isOwnerSet() {
    return ownerSet;
  }
  public boolean isAssigneeSet() {
    return assigneeSet;
  }
  public boolean isDelegationStateSet() {
    return delegationStateSet;
  }
  public boolean isNameSet() {
    return nameSet;
  }
  public boolean isDescriptionSet() {
    return descriptionSet;
  }
  public boolean isDuedateSet() {
    return duedateSet;
  }
  public boolean isPrioritySet() {
    return prioritySet;
  }
  public boolean isParentTaskIdSet() {
    return parentTaskIdSet;
  }
  public boolean isCategorySet() {
	  return categorySet;
  }
  public boolean isTenantIdSet() {
    return tenantIdSet;
  }
  public boolean isFormKeySet() {
    return formKeySet;
  }
}
