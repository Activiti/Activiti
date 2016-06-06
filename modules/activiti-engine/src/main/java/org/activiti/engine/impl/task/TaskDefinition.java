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
package org.activiti.engine.impl.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.form.TaskFormHandler;

/**
 * Container for task definition information gathered at parsing time.
 * 
 * @author Joram Barrez
 */
public class TaskDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String key;
  
  // assignment fields
  protected Expression nameExpression;
  protected Expression ownerExpression;
  protected Expression descriptionExpression;
  protected Expression assigneeExpression;
  protected Set<Expression> candidateUserIdExpressions = new HashSet<Expression>();
  protected Set<Expression> candidateGroupIdExpressions = new HashSet<Expression>();
  protected Expression dueDateExpression;
  protected Expression businessCalendarNameExpression;
  protected Expression priorityExpression;
  protected Expression categoryExpression;
  protected Map<String, Set<Expression>> customUserIdentityLinkExpressions = new HashMap<String, Set<Expression>>(); 
  protected Map<String, Set<Expression>> customGroupIdentityLinkExpressions = new HashMap<String, Set<Expression>>();
  protected Expression skipExpression;
  
  // form fields
  protected TaskFormHandler taskFormHandler;
  protected Expression formKeyExpression;
  
  // task listeners
  protected Map<String, List<TaskListener>> taskListeners = new HashMap<String, List<TaskListener>>();
  
  public TaskDefinition(TaskFormHandler taskFormHandler) {
    this.taskFormHandler = taskFormHandler;
  }

  // getters and setters //////////////////////////////////////////////////////

  public Expression getNameExpression() {
    return nameExpression;
  }

  public void setNameExpression(Expression nameExpression) {
    this.nameExpression = nameExpression;
  }
  
  public Expression getOwnerExpression() {
    return ownerExpression;
  }
  
  public void setOwnerExpression(Expression ownerExpression) {
    this.ownerExpression = ownerExpression;
  }

  public Expression getDescriptionExpression() {
    return descriptionExpression;
  }

  public void setDescriptionExpression(Expression descriptionExpression) {
    this.descriptionExpression = descriptionExpression;
  }

  public Expression getAssigneeExpression() {
    return assigneeExpression;
  }

  public void setAssigneeExpression(Expression assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }

  public Set<Expression> getCandidateUserIdExpressions() {
    return candidateUserIdExpressions;
  }

  public void addCandidateUserIdExpression(Expression userId) {
    candidateUserIdExpressions.add(userId);
  }

  public void setCandidateUserIdExpressions(Set<Expression> candidateUserIdExpressions) {
    this.candidateUserIdExpressions = candidateUserIdExpressions;
  }

  public Set<Expression> getCandidateGroupIdExpressions() {
    return candidateGroupIdExpressions;
  }

  public void addCandidateGroupIdExpression(Expression groupId) {
    candidateGroupIdExpressions.add(groupId);
  }
  
  public void setCandidateGroupIdExpressions(Set<Expression> candidateGroupIdExpressions) {
    this.candidateGroupIdExpressions = candidateGroupIdExpressions;
  }

  public Map<String, Set<Expression>> getCustomUserIdentityLinkExpressions() {
    return customUserIdentityLinkExpressions;
  }

  public void addCustomUserIdentityLinkExpression(String identityLinkType, Set<Expression> idList) {
  	customUserIdentityLinkExpressions.put(identityLinkType, idList);
  }

  public Map<String, Set<Expression>> getCustomGroupIdentityLinkExpressions() {
    return customGroupIdentityLinkExpressions;
  }

  public void addCustomGroupIdentityLinkExpression(String identityLinkType, Set<Expression> idList) {
  	customGroupIdentityLinkExpressions.put(identityLinkType, idList);
  }

  public Expression getPriorityExpression() {
    return priorityExpression;
  }

  public void setPriorityExpression(Expression priorityExpression) {
    this.priorityExpression = priorityExpression;
  }

  public TaskFormHandler getTaskFormHandler() {
    return taskFormHandler;
  }

  public void setTaskFormHandler(TaskFormHandler taskFormHandler) {
    this.taskFormHandler = taskFormHandler;
  }
  
  public Expression getFormKeyExpression() {
		return formKeyExpression;
	}

	public void setFormKeyExpression(Expression formKeyExpression) {
		this.formKeyExpression = formKeyExpression;
	}

	public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
  
  public Expression getDueDateExpression() {
    return dueDateExpression;
  }
  
  public void setDueDateExpression(Expression dueDateExpression) {
    this.dueDateExpression = dueDateExpression;
  }

  public Expression getBusinessCalendarNameExpression() {
    return businessCalendarNameExpression;
  }

  public void setBusinessCalendarNameExpression(Expression businessCalendarNameExpression) {
    this.businessCalendarNameExpression = businessCalendarNameExpression;
  }

  public Expression getCategoryExpression() {
		return categoryExpression;
	}

	public void setCategoryExpression(Expression categoryExpression) {
		this.categoryExpression = categoryExpression;
	}

	public Map<String, List<TaskListener>> getTaskListeners() {
    return taskListeners;
  }

  public void setTaskListeners(Map<String, List<TaskListener>> taskListeners) {
    this.taskListeners = taskListeners;
  }
  
  public List<TaskListener> getTaskListener(String eventName) {
    return taskListeners.get(eventName);
  }
  
  public void addTaskListener(String eventName, TaskListener taskListener) {
    if(TaskListener.EVENTNAME_ALL_EVENTS.equals(eventName)) {
      // In order to prevent having to merge the "all" tasklisteners with the ones for a specific eventName,
      // every time "getTaskListener()" is called, we add the listener explicitally to the individual lists
      this.addTaskListener(TaskListener.EVENTNAME_CREATE, taskListener);
      this.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, taskListener);
      this.addTaskListener(TaskListener.EVENTNAME_COMPLETE, taskListener);
      this.addTaskListener(TaskListener.EVENTNAME_DELETE, taskListener);
      
    } else {
      List<TaskListener> taskEventListeners = taskListeners.get(eventName);
      if (taskEventListeners == null) {
        taskEventListeners = new ArrayList<TaskListener>();
        taskListeners.put(eventName, taskEventListeners);
      }
      taskEventListeners.add(taskListener);
    }
  }
  
  public Expression getSkipExpression() {
    return skipExpression;
  }

  
  public void setSkipExpression(Expression skipExpression) {
    this.skipExpression = skipExpression;
  }
}
