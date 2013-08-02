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
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.delegate.TaskListenerInvocation;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Tijs Rademakers
 */ 
public class TaskEntity extends VariableScopeImpl implements Task, DelegateTask, Serializable, PersistentObject, HasRevision {

  public static final String DELETE_REASON_COMPLETED = "completed";
  public static final String DELETE_REASON_DELETED = "deleted";

  private static final long serialVersionUID = 1L;

  protected int revision;

  protected String owner;
  protected String assignee;
  protected DelegationState delegationState;
  
  protected String parentTaskId;
  
  protected String name;
  protected String description;
  protected int priority = DEFAULT_PRIORITY;
  protected Date createTime; // The time when the task has been created
  protected Date dueDate;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  
  protected boolean isIdentityLinksInitialized = false;
  protected List<IdentityLinkEntity> taskIdentityLinkEntities = new ArrayList<IdentityLinkEntity>(); 
  
  protected String executionId;
  protected ExecutionEntity execution;
  
  protected String processInstanceId;
  protected ExecutionEntity processInstance;
  
  protected String processDefinitionId;
  
  protected TaskDefinition taskDefinition;
  protected String taskDefinitionKey;
  
  protected boolean isDeleted;
  
  protected String eventName;
  
  protected List<VariableInstanceEntity> queryVariables;
  
  public TaskEntity() {
  }

  public TaskEntity(String taskId) {
    this.id = taskId;
  }
  
  /** creates and initializes a new persistent task. */
  public static TaskEntity createAndInsert(ActivityExecution execution) {
    TaskEntity task = create();
    task.insert((ExecutionEntity) execution);
    return task;
  }

  public void insert(ExecutionEntity execution) {
    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.insert(this);
    
    if(execution != null) {
      execution.addTask(this);
    }
    
    commandContext.getHistoryManager().recordTaskCreated(this, execution);
  }
  
  public void update() {
    // Needed to make history work: the setter will also update the historic task
    setOwner(this.getOwner());
    setAssignee(this.getAssignee());
    setDelegationState(this.getDelegationState());
    setName(this.getName());
    setDescription(this.getDescription());
    setPriority(this.getPriority());
    setCreateTime(this.getCreateTime());
    setDueDate(this.getDueDate());
    setParentTaskId(this.getParentTaskId());
    
    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.update(this);
  }
  
  /**  Creates a new task.  Embedded state and create time will be initialized.
   * But this task still will have to be persisted. See {@link #insert(ExecutionEntity)}. */
  public static TaskEntity create() {
    TaskEntity task = new TaskEntity();
    task.isIdentityLinksInitialized = true;
    task.createTime = ClockUtil.getCurrentTime();
    return task;
  }

  public void complete() {
    fireEvent(TaskListener.EVENTNAME_COMPLETE);

    if (Authentication.getAuthenticatedUserId() != null && processInstanceId != null) {
      getProcessInstance().involveUser(Authentication.getAuthenticatedUserId(), IdentityLinkType.PARTICIPANT);
    }
    
    Context
      .getCommandContext()
      .getTaskEntityManager()
      .deleteTask(this, TaskEntity.DELETE_REASON_COMPLETED, false);
    
    if (executionId!=null) {
      ExecutionEntity execution = getExecution();
      execution.removeTask(this);
      execution.signal(null, null);
    }
  }
  
  public void delegate(String userId) {
    setDelegationState(DelegationState.PENDING);
    if (getOwner() == null) {
      setOwner(getAssignee());
    }
    setAssignee(userId);
  }

  public void resolve() {
    setDelegationState(DelegationState.RESOLVED);
    setAssignee(this.owner);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("assignee", this.assignee);
    persistentState.put("owner", this.owner);
    persistentState.put("name", this.name);
    persistentState.put("priority", this.priority);
    if (executionId != null) {
      persistentState.put("executionId", this.executionId);
    }
    if (processDefinitionId != null) {
      persistentState.put("processDefinitionId", this.processDefinitionId);
    }
    if (createTime != null) {
      persistentState.put("createTime", this.createTime);
    }
    if(description != null) {
      persistentState.put("description", this.description);
    }
    if(dueDate != null) {
      persistentState.put("dueDate", this.dueDate);
    }
    if (parentTaskId != null) {
      persistentState.put("parentTaskId", this.parentTaskId);
    }
    if (delegationState != null) {
      persistentState.put("delegationState", this.delegationState);
    }
    
    persistentState.put("suspensionState", this.suspensionState);
    
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  // variables ////////////////////////////////////////////////////////////////
  
  @Override
  protected VariableScopeImpl getParentVariableScope() {
    if (getExecution()!=null) {
      return execution;
    }
    return null;
  }

  @Override
  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setTaskId(id);
    variableInstance.setExecutionId(executionId);
    variableInstance.setProcessInstanceId(processInstanceId);
  }

  @Override
  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
      .getCommandContext()
      .getVariableInstanceEntityManager()
      .findVariableInstancesByTaskId(id);
  }

  // execution ////////////////////////////////////////////////////////////////

  public ExecutionEntity getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = Context
        .getCommandContext()
        .getExecutionEntityManager()
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
      
      Context.getCommandContext().getHistoryManager().recordTaskExecutionIdChange(this.id, executionId);
      
    } else {
      this.execution = null;
      this.executionId = null;
      this.processInstanceId = null;
      this.processDefinitionId = null;
    }
  }
    
  // task assignment //////////////////////////////////////////////////////////
  
  public IdentityLinkEntity addIdentityLink(String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setTask(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    identityLinkEntity.insert();
    if (userId != null && processInstanceId != null) {
      getProcessInstance().involveUser(userId, IdentityLinkType.PARTICIPANT);
    }
    return identityLinkEntity;
  }
  
  public void deleteIdentityLink(String userId, String groupId, String type) {
    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkEntityManager()
      .findIdentityLinkByTaskUserGroupAndType(id, userId, groupId, type);
    
    for (IdentityLinkEntity identityLink: identityLinks) {
      Context
        .getCommandContext()
        .getIdentityLinkEntityManager()
        .deleteIdentityLink(identityLink, true);
    }
    
	// fix deleteCandidate() in create TaskListener
	List<IdentityLinkEntity> removedIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
    for (IdentityLinkEntity identityLinkEntity : this.getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType())) {
		  if ((userId != null && identityLinkEntity.getUserId().equals(userId))
			  || (groupId != null && identityLinkEntity.getGroupId().equals(groupId))) {
			  removedIdentityLinkEntities.add(identityLinkEntity);
		  }
      }
    }
	getIdentityLinks().removeAll(removedIdentityLinkEntities);
  }
  
  public Set<IdentityLink> getCandidates() {
    Set<IdentityLink> potentialOwners = new HashSet<IdentityLink>();
    for (IdentityLinkEntity identityLinkEntity : getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType())) {
        potentialOwners.add(identityLinkEntity);
      }
    }
    return potentialOwners;
  }
  
  public void addCandidateUser(String userId) {
    addIdentityLink(userId, null, IdentityLinkType.CANDIDATE);
  }
  
  public void addCandidateUsers(Collection<String> candidateUsers) {
    for (String candidateUser : candidateUsers) {
      addCandidateUser(candidateUser);
    }
  }
  
  public void addCandidateGroup(String groupId) {
    addIdentityLink(null, groupId, IdentityLinkType.CANDIDATE);
  }
  
  public void addCandidateGroups(Collection<String> candidateGroups) {
    for (String candidateGroup : candidateGroups) {
      addCandidateGroup(candidateGroup);
    }
  }
  
  public void addGroupIdentityLink(String groupId, String identityLinkType) {
    addIdentityLink(null, groupId, identityLinkType);
  }

  public void addUserIdentityLink(String userId, String identityLinkType) {
    addIdentityLink(userId, null, identityLinkType);
  }

  public void deleteCandidateGroup(String groupId) {
    deleteGroupIdentityLink(groupId, IdentityLinkType.CANDIDATE);
  }

  public void deleteCandidateUser(String userId) {
    deleteUserIdentityLink(userId, IdentityLinkType.CANDIDATE);
  }

  public void deleteGroupIdentityLink(String groupId, String identityLinkType) {
    if (groupId!=null) {
      deleteIdentityLink(null, groupId, identityLinkType);
    }
  }

  public void deleteUserIdentityLink(String userId, String identityLinkType) {
    if (userId!=null) {
      deleteIdentityLink(userId, null, identityLinkType);
    }
  }

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      taskIdentityLinkEntities = Context
        .getCommandContext()
        .getIdentityLinkEntityManager()
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
      execution.setVariables(parameters);
    }
  }
  
  public String toString() {
    return "Task[id=" + id + ", name=" + name + "]";
  }
  
  // special setters //////////////////////////////////////////////////////////
  
  public void setName(String taskName) {
    this.name = taskName;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskNameChange(id, taskName);
    }
  }

  /* plain setter for persistence */
  public void setNameWithoutCascade(String taskName) {
    this.name = taskName;
  }

  public void setDescription(String description) {
    this.description = description;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskDescriptionChange(id, description);
    }
  }

  /* plain setter for persistence */
  public void setDescriptionWithoutCascade(String description) {
    this.description = description;
  }

  public void setAssignee(String assignee) {
    if (assignee==null && this.assignee==null) {
      return;
    }
    this.assignee = assignee;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the 
    // setAssignee outside a service method.  E.g. while creating a new task.
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskAssigneeChange(id, assignee);
      
      if (assignee != null && processInstanceId != null) {
        getProcessInstance().involveUser(assignee, IdentityLinkType.PARTICIPANT);
      }
      
      fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
    }
  }

  /* plain setter for persistence */
  public void setAssigneeWithoutCascade(String assignee) {
    this.assignee = assignee;
  }
  
  public void setOwner(String owner) {
    if (owner==null && this.owner==null) {
      return;
    }
//    if (owner!=null && owner.equals(this.owner)) {
//      return;
//    }
    this.owner = owner;

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskOwnerChange(id, owner);
      
      if (owner != null && processInstanceId != null) {
        getProcessInstance().involveUser(owner, IdentityLinkType.PARTICIPANT);
      }
    }
  }

  /* plain setter for persistence */
  public void setOwnerWithoutCascade(String owner) {
    this.owner = owner;
  }
  
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
    
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskDueDateChange(id, dueDate);
    }
  }

  public void setDueDateWithoutCascade(Date dueDate) {
    this.dueDate = dueDate;
  }
  
  public void setPriority(int priority) {
    this.priority = priority;
    
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskPriorityChange(id, priority);
    }
  }

  public void setPriorityWithoutCascade(int priority) {
    this.priority = priority;
  }
  
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
    
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext
        .getHistoryManager()
        .recordTaskParentTaskIdChange(id, parentTaskId);
    }
  }

  public void setParentTaskIdWithoutCascade(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }
  
  public void setTaskDefinitionKeyWithoutCascade(String taskDefinitionKey) {
       this.taskDefinitionKey = taskDefinitionKey;
  }       

  public void fireEvent(String taskEventName) {
    TaskDefinition taskDefinition = getTaskDefinition();
    if (taskDefinition != null) {
      List<TaskListener> taskEventListeners = getTaskDefinition().getTaskListener(taskEventName);
      if (taskEventListeners != null) {
        for (TaskListener taskListener : taskEventListeners) {
          ExecutionEntity execution = getExecution();
          if (execution != null) {
            setEventName(taskEventName);
          }
          try {
            Context.getProcessEngineConfiguration()
              .getDelegateInterceptor()
              .handleInvocation(new TaskListenerInvocation(taskListener, (DelegateTask)this));
          }catch (Exception e) {
            throw new ActivitiException("Exception while invoking TaskListener: "+e.getMessage(), e);
          }
        }
      }
    }
  }
  
  @Override
  protected boolean isActivityIdUsedForDetails() {
    return false;
  }

  // modified getters and setters /////////////////////////////////////////////
  
  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
    this.taskDefinitionKey = taskDefinition.getKey();
    
    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      commandContext.getHistoryManager().recordTaskDefinitionKeyChange(this, taskDefinitionKey);
    }
  }

  public TaskDefinition getTaskDefinition() {
    if (taskDefinition==null && taskDefinitionKey!=null) {
      ProcessDefinitionEntity processDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentManager()
        .findDeployedProcessDefinitionById(processDefinitionId);
      taskDefinition = processDefinition.getTaskDefinitions().get(taskDefinitionKey);
    }
    return taskDefinition;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
  
  public Date getDueDate() {
    return dueDate;
  }
  
  public int getPriority() {
    return priority;
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
    
    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      commandContext.getHistoryManager().recordTaskDefinitionKeyChange(this, taskDefinitionKey);
    }
  }

  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public ExecutionEntity getProcessInstance() {
    if (processInstance == null && processInstanceId != null) {
      processInstance = Context
          .getCommandContext()
          .getExecutionEntityManager()
          .findExecutionById(processInstanceId);
    } 
    return processInstance;
  }
  public void setProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = processInstance;
  }
  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public String getOwner() {
    return owner;
  }
  public DelegationState getDelegationState() {
    return delegationState;
  }
  public void setDelegationState(DelegationState delegationState) {
    this.delegationState = delegationState;
  }
  public String getDelegationStateString() {
    return (delegationState!=null ? delegationState.toString() : null);
  }
  public void setDelegationStateString(String delegationStateString) {
    this.delegationState = (delegationStateString!=null ? DelegationState.valueOf(DelegationState.class, delegationStateString) : null);
  }
  public boolean isDeleted() {
    return isDeleted;
  }
  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
  public String getParentTaskId() {
    return parentTaskId;
  }
  public Map<String, VariableInstanceEntity> getVariableInstances() {
    ensureVariableInstancesInitialized();
    return variableInstances;
  }
  public int getSuspensionState() {
    return suspensionState;
  }
  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }
  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }
  public Map<String, Object> getTaskLocalVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (queryVariables != null) {
      for (VariableInstanceEntity variableInstance: queryVariables) {
        if (variableInstance.getId() != null && variableInstance.getTaskId() != null) {
          variables.put(variableInstance.getName(), variableInstance.getValue());
        }
      }
    }
    return variables;
  }
  public Map<String, Object> getProcessVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (queryVariables != null) {
      for (VariableInstanceEntity variableInstance: queryVariables) {
        if (variableInstance.getId() != null && variableInstance.getTaskId() == null) {
          variables.put(variableInstance.getName(), variableInstance.getValue());
        }
      }
    }
    return variables;
  }
  public List<VariableInstanceEntity> getQueryVariables() {
    if(queryVariables == null && Context.getCommandContext() != null) {
      queryVariables = new VariableInitializingList();
    }
    return queryVariables;
  }
  
  public void setQueryVariables(List<VariableInstanceEntity> queryVariables) {
    this.queryVariables = queryVariables;
  }
  
}
