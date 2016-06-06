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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tijs Rademakers
 */
public class UserTask extends Task {

  protected String assignee;
  protected String owner;
  protected String priority;
  protected String formKey;
  protected String dueDate;
  protected String businessCalendarName;
  protected String category;
  protected String extensionId;
  protected List<String> candidateUsers = new ArrayList<String>();
  protected List<String> candidateGroups = new ArrayList<String>();
  protected List<FormProperty> formProperties = new ArrayList<FormProperty>();
  protected List<ActivitiListener> taskListeners = new ArrayList<ActivitiListener>();
  protected String skipExpression;

  protected Map<String, Set<String>> customUserIdentityLinks = new HashMap<String, Set<String>>(); 
  protected Map<String, Set<String>> customGroupIdentityLinks = new HashMap<String, Set<String>>();
  
  protected List<CustomProperty> customProperties = new ArrayList<CustomProperty>();

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

  public String getBusinessCalendarName() {
    return businessCalendarName;
  }

  public void setBusinessCalendarName(String businessCalendarName) {
    this.businessCalendarName = businessCalendarName;
  }

  public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getExtensionId() {
    return extensionId;
  }
  public void setExtensionId(String extensionId) {
    this.extensionId = extensionId;
  }
  public boolean isExtended() {
    return extensionId != null && !extensionId.isEmpty();
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
  
  public void addCustomUserIdentityLink(String userId, String type){
	  Set<String> userIdentitySet = customUserIdentityLinks.get(type);
	  
	  if(userIdentitySet == null){
		  userIdentitySet = new HashSet<String>();
		  customUserIdentityLinks.put(type, userIdentitySet);
	  }
	  
	  userIdentitySet.add(userId);
  }
  
  public void addCustomGroupIdentityLink(String groupId, String type){
	  Set<String> groupIdentitySet = customGroupIdentityLinks.get(type);
	  
	  if(groupIdentitySet == null){
		  groupIdentitySet = new HashSet<String>();
		  customGroupIdentityLinks.put(type, groupIdentitySet);
	  }
	  
	  groupIdentitySet.add(groupId);
  }
  
  public Map<String, Set<String>> getCustomUserIdentityLinks() {
	return customUserIdentityLinks;
  }
  
  public void setCustomUserIdentityLinks(
		Map<String, Set<String>> customUserIdentityLinks) {
	this.customUserIdentityLinks = customUserIdentityLinks;
  }
  
  public Map<String, Set<String>> getCustomGroupIdentityLinks() {
	return customGroupIdentityLinks;
  }
  
  public void setCustomGroupIdentityLinks(Map<String, Set<String>> customGroupIdentityLinks) {
    this.customGroupIdentityLinks = customGroupIdentityLinks;
  }
  
  public List<CustomProperty> getCustomProperties() {
    return customProperties;
  }
  public void setCustomProperties(List<CustomProperty> customProperties) {
    this.customProperties = customProperties;
  }
  
  public String getSkipExpression() {
    return skipExpression;
  }
  
  public void setSkipExpression(String skipExpression) {
    this.skipExpression = skipExpression;
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
    setExtensionId(otherElement.getExtensionId());
    setSkipExpression(otherElement.getSkipExpression());
    
    setCandidateGroups(new ArrayList<String>(otherElement.getCandidateGroups()));
    setCandidateUsers(new ArrayList<String>(otherElement.getCandidateUsers()));
    
    setCustomGroupIdentityLinks(otherElement.customGroupIdentityLinks);
    setCustomUserIdentityLinks(otherElement.customUserIdentityLinks);
    
    formProperties = new ArrayList<FormProperty>();
    if (otherElement.getFormProperties() != null && !otherElement.getFormProperties().isEmpty()) {
      for (FormProperty property : otherElement.getFormProperties()) {
        formProperties.add(property.clone());
      }
    }
    
    taskListeners = new ArrayList<ActivitiListener>();
    if (otherElement.getTaskListeners() != null && !otherElement.getTaskListeners().isEmpty()) {
      for (ActivitiListener listener : otherElement.getTaskListeners()) {
        taskListeners.add(listener.clone());
      }
    }
  }
}
