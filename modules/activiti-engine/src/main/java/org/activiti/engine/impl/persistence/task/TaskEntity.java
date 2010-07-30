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
package org.activiti.engine.impl.persistence.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.Task;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.persistence.runtime.ActivityInstanceEntity;
import org.activiti.engine.impl.persistence.runtime.ProcessInstanceEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.impl.execution.VariableMap;
import org.activiti.pvm.runtime.PvmActivityInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */ 
public class TaskEntity implements Task, Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;
  protected boolean isNew = false;

  protected String assignee;

  protected String name;
  
  protected String description;

  protected int priority = Priority.NORMAL;

  protected Date createTime; // The time when the task has been created

  protected Date startDeadline; // The time when the task should have been started

  protected Date completionDeadline; // The time when the task should have been completed
  
  protected boolean skippable;
  
  protected VariableMap variableMap = null;

  protected boolean isTaskInvolvementsInitialized = false;
  protected List<TaskInvolvement> taskInvolvements = new ArrayList<TaskInvolvement>(); 
  
  protected String activityInstanceId;
  protected ActivityInstanceEntity activityInstance;
  
  protected String processInstanceId;
  protected ProcessInstanceEntity processInstance;
  
  protected String processDefinitionId;
  
  public TaskEntity() {
  }

  public TaskEntity(String taskId) {
    this.id = taskId;
    this.isNew = true;
  }
  
  /** creates and initializes a new persistent task. */
  public static TaskEntity createAndInsert() {
    TaskEntity task = create();
    CommandContext
        .getCurrent()
        .getPersistenceSession()
        .insert(task);
    return task;
  }
  
  /** new task.  Embedded state and create time will be initialized.
   * But this task still will have to be persisted with 
   * TransactionContext
   *     .getCurrent()
   *     .getPersistenceSession()
   *     .insert(task);
   */
  public static TaskEntity create() {
    TaskEntity task = new TaskEntity();
    task.isTaskInvolvementsInitialized = true;
    task.createTime = ClockUtil.getCurrentTime();
    return task;
  }

  public void delete() {
    // cascade deletion to task assignments
    for (TaskInvolvement taskInvolvements: getTaskInvolvements()) {
      taskInvolvements.delete();
    }
    
    CommandContext
        .getCurrent()
        .getPersistenceSession()
        .delete(this);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("assignee", this.assignee);
    persistentState.put("name", this.name);
    persistentState.put("priority", this.priority);
    if (createTime!=null) {
      persistentState.put("createTime", this.createTime);
    }
    if (startDeadline!=null) {
      persistentState.put("startDeadline", this.startDeadline);
    }
    if (completionDeadline!=null) {
      persistentState.put("completionDeadline", this.completionDeadline);
    }
    if (skippable) {
      persistentState.put("skippable", Boolean.TRUE);
    }
    return persistentState;
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

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public boolean isSkippable() {
    return skippable;
  }

  public void setSkippable(boolean skippable) {
    this.skippable = skippable;
  }

  public Date getStartDeadline() {
    return startDeadline;
  }

  public void setStartDeadline(Date startDeadline) {
    this.startDeadline = startDeadline;
  }

  public Date getCompletionDeadline() {
    return completionDeadline;
  }

  public void setCompletionDeadline(Date completionDeadline) {
    this.completionDeadline = completionDeadline;
  }
	
	public String getActivityInstanceId() {
	  return activityInstanceId;
	}
  
  public ActivityInstanceEntity getActivityInstance() {
    if ( (activityInstance==null) && (activityInstanceId!=null) ) {
      this.activityInstance = CommandContext
        .getCurrent()
        .getPersistenceSession()
        .findExecution(activityInstanceId);
    }
    return activityInstance;
  }
  
  public void setActivityInstance(PvmActivityInstance activityInstance) {
    if (activityInstance!=null) {
      this.activityInstance = (ActivityInstanceEntity) activityInstance;
      this.activityInstanceId = this.activityInstance.getId();
      this.processInstanceId = this.activityInstance.getProcessInstanceId();
      this.processDefinitionId = this.activityInstance.getProcessDefinitionId();
    } else {
      this.activityInstance = null;
      this.activityInstanceId = null;
      this.processInstanceId = null;
      this.processDefinitionId = null;
    }
  }
    
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }  
  
  /*
   * TASK ASSIGNMENT
   */


  public String getAssignee() {
    return assignee;
  }
  
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  
  public TaskInvolvement createTaskInvolvement() {
    TaskInvolvement taskInvolvement = TaskInvolvement.createAndInsert();
    getTaskInvolvements().add(taskInvolvement);
    taskInvolvement.setTask(this);
    return taskInvolvement;
  }
  
  public Set<TaskInvolvement> getCandidates() {
    Set<TaskInvolvement> potentialOwners = new HashSet<TaskInvolvement>();
    for (TaskInvolvement taskInvolvement : getTaskInvolvements()) {
      if (TaskInvolvementType.CANDIDATE.equals(taskInvolvement.getType())) {
        potentialOwners.add(taskInvolvement);
      }
    }
    return potentialOwners;
  }
  
  public void addCandidateUser(String userId) {
    TaskInvolvement involvement = createTaskInvolvement();
    involvement.setUserId(userId);
    involvement.setType(TaskInvolvementType.CANDIDATE);
  }
  
  public void addCandidateGroup(String groupId) {
    TaskInvolvement involvement = createTaskInvolvement();
    involvement.setGroupId(groupId);
    involvement.setType(TaskInvolvementType.CANDIDATE);
  }
  
  public List<TaskInvolvement> getTaskInvolvements() {
    if (!isTaskInvolvementsInitialized) {
      taskInvolvements = CommandContext
          .getCurrent()
          .getPersistenceSession()
          .findTaskInvolvementsByTask(id);
      isTaskInvolvementsInitialized = true;
    }
    return taskInvolvements;
  }

  public boolean isNew() {
    return isNew;
  }

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public Map<String, Object> getExecutionVariables() {
    if (activityInstance!=null) {
      return activityInstance.getVariables();
    }
    return Collections.EMPTY_MAP;
  }
  public void setExecutionVariables(Map<String, Object> parameters) {
    if (getActivityInstance()!=null) {
      activityInstance.setVariables(parameters);
    }
  }
}
