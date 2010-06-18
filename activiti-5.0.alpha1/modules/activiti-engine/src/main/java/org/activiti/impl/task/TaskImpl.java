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
package org.activiti.impl.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.Task;
import org.activiti.activity.ActivityExecution;
import org.activiti.impl.execution.ExecutionDbImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.execution.ScopeInstanceImpl;
import org.activiti.impl.json.JSONObject;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.time.Clock;
import org.activiti.impl.tx.TransactionContext;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */ 
public class TaskImpl extends ScopeInstanceImpl implements Task, Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected int dbversion;
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
  protected List<TaskInvolvement> taskInvolvements = new ArrayList<TaskInvolvement>(); 
  
  protected String executionId;
  protected ExecutionDbImpl execution;
  
  protected String processDefinitionId;
  
  public TaskImpl() {
  }

  public TaskImpl(String taskId) {
    this.id = taskId;
    this.isNew = true;
  }
  
  /** creates and initializes a new persistent task. */
  public static TaskImpl createAndInsert() {
    TaskImpl task = create();
    TransactionContext
        .getCurrent()
        .getTransactionalObject(PersistenceSession.class)
        .insert(task);
    return task;
  }
  
  /** new task.  Embedded state and create time will be initialized.
   * But this task still will have to be persisted with 
   * TransactionContext
   *     .getCurrent()
   *     .getTransactionalObject(PersistenceSession.class)
   *     .insert(task);
   */
  public static TaskImpl create() {
    TaskImpl task = new TaskImpl();
    task.isTaskInvolvementsInitialized = true;
    task.createTime = Clock.getCurrentTime();
    return task;
  }

  public void delete() {
    // cascade deletion to task assignments
    for (TaskInvolvement taskInvolvements: getTaskInvolvements()) {
      taskInvolvements.delete();
    }
    
    TransactionContext
        .getCurrent()
        .getTransactionalObject(PersistenceSession.class)
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
  

  public int getDbVersion() {
    return dbversion;
  }

  public void setDbVersion(int dbversion) {
    this.dbversion = dbversion;
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
  
  public ExecutionImpl getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = TransactionContext
        .getCurrent()
        .getTransactionalObject(PersistenceSession.class)
        .findExecution(executionId);
    }
    return execution;
  }
  
  public void setExecution(ActivityExecution execution) {
    if (execution!=null) {
      this.execution = (ExecutionDbImpl) execution;
      this.executionId = this.execution.getId();
      this.processDefinitionId = this.execution.getProcessDefinitionId();
    } else {
      this.execution = null;
      this.executionId = null;
      this.execution = null;
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
      taskInvolvements = TransactionContext
          .getCurrent()
          .getTransactionalObject(PersistenceSession.class)
          .findTaskInvolvementsByTask(id);
      isTaskInvolvementsInitialized = true;
    }
    return taskInvolvements;
  }

  public JSONObject getJsonObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", id);
    jsonObject.put("dbversion", (long)dbversion);
    jsonObject.put("assignee", assignee);
    jsonObject.put("name", name);
    jsonObject.put("priority", priority);
    jsonObject.put("createTime", createTime);
    jsonObject.put("skippable", skippable);
    if (startDeadline!=null) {
      jsonObject.put("startDeadline", startDeadline);
    }
    if (completionDeadline!=null) {
      jsonObject.put("completionDeadline", completionDeadline);
    }
    if (execution!=null) {
      jsonObject.put("execution", execution.getId());
    }
    return jsonObject;
  }

  public boolean isNew() {
    return isNew;
  }
}
