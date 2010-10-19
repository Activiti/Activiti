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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.VariableMap;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.pvm.delegate.DelegateExecution;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */ 
public class TaskEntity implements Task, Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String assignee;
  protected String name;
  protected String description;
  protected int priority = Task.PRIORITY_NORMAL;
  protected Date createTime; // The time when the task has been created
  protected boolean isIdentityLinksInitialized = false;
  protected List<IdentityLinkEntity> taskIdentityLinkEntities = new ArrayList<IdentityLinkEntity>(); 
  
  protected String executionId;
  protected ExecutionEntity execution;
  
  protected String processInstanceId;
  protected ExecutionEntity processInstance;
  
  protected String processDefinitionId;
  
  protected String taskDefinitionKey;
  protected TaskDefinition taskDefinition;
  
  public TaskEntity() {
  }

  public TaskEntity(String taskId) {
    this.id = taskId;
  }
  
  /** creates and initializes a new persistent task. */
  public static TaskEntity createAndInsert() {
    TaskEntity task = create();
    CommandContext
        .getCurrent()
        .getDbSqlSession()
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
    task.isIdentityLinksInitialized = true;
    task.createTime = ClockUtil.getCurrentTime();
    return task;
  }

  public void delete() {
    // cascade deletion to task assignments
    for (IdentityLinkEntity identityLinkEntities: getIdentityLinks()) {
      identityLinkEntities.delete();
    }
    
    CommandContext
        .getCurrent()
        .getDbSqlSession()
        .delete(TaskEntity.class, id);
  }

  public void update(TaskEntity task) {
    this.assignee = task.getAssignee();
    this.name = task.getName();
    this.priority = task.getPriority();
    this.createTime = task.getCreateTime();
    this.description = task.getDescription();
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("assignee", this.assignee);
    persistentState.put("name", this.name);
    persistentState.put("priority", this.priority);
    if (executionId!=null) {
      persistentState.put("executionId", this.executionId);
    }
    if (createTime!=null) {
      persistentState.put("createTime", this.createTime);
    }
    if(description != null) {
      persistentState.put("description", this.description);
    }
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
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
  
  public void setExecution(DelegateExecution execution) {
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
    

  /*
   * TASK ASSIGNMENT
   */


  public IdentityLinkEntity createIdentityLink() {
    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.createAndInsert();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setTask(this);
    return identityLinkEntity;
  }
  
  public Set<IdentityLinkEntity> getCandidates() {
    Set<IdentityLinkEntity> potentialOwners = new HashSet<IdentityLinkEntity>();
    for (IdentityLinkEntity identityLinkEntity : getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType())) {
        potentialOwners.add(identityLinkEntity);
      }
    }
    return potentialOwners;
  }
  
  public void addCandidateUser(String userId) {
    IdentityLinkEntity identityLink = createIdentityLink();
    identityLink.setUserId(userId);
    identityLink.setType(IdentityLinkType.CANDIDATE);
  }
  
  public void addCandidateGroup(String groupId) {
    IdentityLinkEntity identityLink = createIdentityLink();
    identityLink.setGroupId(groupId);
    identityLink.setType(IdentityLinkType.CANDIDATE);
  }
  
  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      taskIdentityLinkEntities = CommandContext
          .getCurrent()
          .getTaskSession()
          .findIdentityLinksByTaskId(id);
      isIdentityLinksInitialized = true;
    }
    
    return taskIdentityLinkEntities;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getActivityInstanceVariables() {
    if (execution!=null) {
      return execution.getVariables();
    }
    return Collections.EMPTY_MAP;
  }
  
  public void setExecutionVariables(Map<String, Object> parameters) {
    if (getExecution()!=null) {
      try {
        VariableMap.setExternalUpdate(Boolean.TRUE);
        execution.setVariables(parameters);
      } finally {
        VariableMap.setExternalUpdate(null);
      }
    }
  }
  
  public String toString() {
    return "Task["+id+"]";
  }
  
  // methods with event notification dispatching //////////////////////////////
  
  public void setAssignee(String assignee) {
    this.assignee = assignee;
    fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
  }

  public void fireEvent(String taskEventName) {
    Map<String, List<TaskListener>> taskListeners = CommandContext
      .getCurrent()
      .getProcessEngineConfiguration()
      .getTaskListeners();
    
    if (taskListeners!=null) {
      List<TaskListener> taskAssignmentListeners = taskListeners.get(taskEventName);
      if (taskAssignmentListeners!=null) {
        for (TaskListener taskListener: taskAssignmentListeners) {
          taskListener.notify(this);
        }
      }
    }
  }

  // modified getters and setters /////////////////////////////////////////////
  
  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
    this.taskDefinitionKey = taskDefinition.getKey();
  }

  public TaskDefinition getTaskDefinition() {
    if (taskDefinition==null && taskDefinitionKey!=null) {
      RepositorySession repositorySession = CommandContext.getCurrentSession(RepositorySession.class);
      ProcessDefinitionEntity processDefinition = repositorySession.findDeployedProcessDefinitionById(processDefinitionId);
      taskDefinition = processDefinition.getTaskDefinitions().get(taskDefinitionKey);
    }
    return taskDefinition;
  }
  
  // getters and setters //////////////////////////////////////////////////////

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

  public String getExecutionId() {
    return executionId;
  }
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }  

  public String getAssignee() {
    return assignee;
  }
  
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }
}
