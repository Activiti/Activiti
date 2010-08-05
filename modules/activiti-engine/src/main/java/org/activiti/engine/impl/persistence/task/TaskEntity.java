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
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.pvm.runtime.PvmExecution;

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
  protected boolean isTaskInvolvementsInitialized = false;
  protected List<TaskInvolvementEntity> taskInvolvementEntities = new ArrayList<TaskInvolvementEntity>(); 
  
  protected String executionId;
  protected ExecutionEntity execution;
  
  protected String processInstanceId;
  protected ExecutionEntity processInstance;
  
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
        .getTaskSession()
        .insertTask(task);
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
    for (TaskInvolvementEntity taskInvolvementEntities: getTaskInvolvements()) {
      taskInvolvementEntities.delete();
    }
    
    CommandContext
        .getCurrent()
        .getTaskSession()
        .deleteTask(id);
  }

  public void update(TaskEntity task) {
    this.assignee = task.getAssignee();
    this.name = task.getName();
    this.priority = task.getPriority();
    this.createTime = task.getCreateTime();
    this.startDeadline = task.getStartDeadline();
    this.completionDeadline = task.getCompletionDeadline();
    this.skippable = task.isSkippable();
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
	
	public String getExecutionId() {
	  return executionId;
	}
  
  public ExecutionEntity getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = CommandContext
        .getCurrent()
        .getRuntimeSession()
        .findExecutionById(executionId);
    }
    return execution;
  }
  
  public void setExecution(PvmExecution execution) {
    if (execution!=null) {
      this.execution = (ExecutionEntity) execution;
      this.executionId = this.execution.getId();
      this.processInstanceId = this.execution.getProcessInstanceId();
      this.processDefinitionId = this.execution.getProcessDefinitionId();
    } else {
      this.execution = null;
      this.executionId = null;
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
  
  public TaskInvolvementEntity createTaskInvolvement() {
    TaskInvolvementEntity taskInvolvementEntity = TaskInvolvementEntity.createAndInsert();
    getTaskInvolvements().add(taskInvolvementEntity);
    taskInvolvementEntity.setTask(this);
    return taskInvolvementEntity;
  }
  
  public Set<TaskInvolvementEntity> getCandidates() {
    Set<TaskInvolvementEntity> potentialOwners = new HashSet<TaskInvolvementEntity>();
    for (TaskInvolvementEntity taskInvolvementEntity : getTaskInvolvements()) {
      if (TaskInvolvementType.CANDIDATE.equals(taskInvolvementEntity.getType())) {
        potentialOwners.add(taskInvolvementEntity);
      }
    }
    return potentialOwners;
  }
  
  public void addCandidateUser(String userId) {
    TaskInvolvementEntity involvement = createTaskInvolvement();
    involvement.setUserId(userId);
    involvement.setType(TaskInvolvementType.CANDIDATE);
  }
  
  public void addCandidateGroup(String groupId) {
    TaskInvolvementEntity involvement = createTaskInvolvement();
    involvement.setGroupId(groupId);
    involvement.setType(TaskInvolvementType.CANDIDATE);
  }
  
  public List<TaskInvolvementEntity> getTaskInvolvements() {
    if (!isTaskInvolvementsInitialized) {
      taskInvolvementEntities = CommandContext
          .getCurrent()
          .getTaskSession()
          .findTaskInvolvementsByTaskId(id);
      isTaskInvolvementsInitialized = true;
    }
    return taskInvolvementEntities;
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

  @SuppressWarnings("unchecked")
  public Map<String, Object> getActivityInstanceVariables() {
    if (execution!=null) {
      return execution.getVariables();
    }
    return Collections.EMPTY_MAP;
  }
  public void setActivityInstanceVariables(Map<String, Object> parameters) {
    if (getExecution()!=null) {
      execution.setVariables(parameters);
    }
  }
}
