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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class UserTask extends Task {

  protected String assignee;
  protected String owner;
  protected String priority;
  protected String formKey;
  protected String dueDate;
  protected String category;
  protected List<String> candidateUsers = new ArrayList<String>();
  protected List<String> candidateGroups = new ArrayList<String>();
  protected List<FormProperty> formProperties = new ArrayList<FormProperty>();
  protected List<ActivitiListener> taskListeners = new ArrayList<ActivitiListener>();

  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }
  public String getPriority() {
    return priority;
  }
  public void setPriority(String priority) {
    this.priority = priority;
  }
  public String getFormKey() {
    return formKey;
  }
  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }
  public String getDueDate() {
    return dueDate;
  }
  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }
  public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public List<String> getCandidateUsers() {
    return candidateUsers;
  }
  public void setCandidateUsers(List<String> candidateUsers) {
    this.candidateUsers = candidateUsers;
  }
  public List<String> getCandidateGroups() {
    return candidateGroups;
  }
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }
  public List<FormProperty> getFormProperties() {
    return formProperties;
  }
  public void setFormProperties(List<FormProperty> formProperties) {
    this.formProperties = formProperties;
  }
  public List<ActivitiListener> getTaskListeners() {
    return taskListeners;
  }
  public void setTaskListeners(List<ActivitiListener> taskListeners) {
    this.taskListeners = taskListeners;
  }
  
  public UserTask clone() {
    UserTask clone = new UserTask();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(UserTask otherElement) {
    super.setValues(otherElement);
    setAssignee(otherElement.getAssignee());
    setOwner(otherElement.getOwner());
    setFormKey(otherElement.getFormKey());
    setDueDate(otherElement.getDueDate());
    setPriority(otherElement.getPriority());
    setCategory(otherElement.getCategory());
    
    setCandidateGroups(new ArrayList<String>(otherElement.getCandidateGroups()));
    setCandidateUsers(new ArrayList<String>(otherElement.getCandidateUsers()));
    
    formProperties = new ArrayList<FormProperty>();
    if (otherElement.getFormProperties() != null && otherElement.getFormProperties().size() > 0) {
      for (FormProperty property : otherElement.getFormProperties()) {
        formProperties.add(property.clone());
      }
    }
    
    taskListeners = new ArrayList<ActivitiListener>();
    if (otherElement.getTaskListeners() != null && otherElement.getTaskListeners().size() > 0) {
      for (ActivitiListener listener : otherElement.getTaskListeners()) {
        taskListeners.add(listener.clone());
      }
    }
  }
}
