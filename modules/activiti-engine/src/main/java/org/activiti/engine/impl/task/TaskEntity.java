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
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.history.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.TaskListener;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.runtime.VariableScopeImpl;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */ 
public class TaskEntity extends VariableScopeImpl implements Task, DelegateTask, Serializable, PersistentObject {

  public static final String DELETE_REASON_COMPLETED = "completed";
  public static final String DELETE_REASON_DELETED = "deleted";

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
  
  protected TaskDefinition taskDefinition;
  protected String taskDefinitionKey;
  
  protected boolean isDeleted;
  
  protected String eventName;
  
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
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = new HistoricTaskInstanceEntity(this, execution);
      dbSqlSession.insert(historicTaskInstance);
    }
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

  public void delete(String deleteReason) {
    if (!isDeleted) {
      isDeleted = true;
      
      // cascade deletion to task assignments
      for (IdentityLinkEntity identityLinkEntities: getIdentityLinks()) {
        identityLinkEntities.delete();
      }

      ensureVariableInstancesInitialized();
      for (VariableInstanceEntity variableInstance: variableInstances.values()) {
        variableInstance.delete();
      }

      CommandContext commandContext = Context.getCommandContext();
      DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = dbSqlSession
          .selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.markEnded(deleteReason);
        }
      }

      dbSqlSession.delete(TaskEntity.class, id);
    }
  }

  public void update(TaskEntity task) {
    setAssignee(task.getAssignee());
    setName(task.getName());
    setDescription(task.getDescription());
    setPriority(task.getPriority());
    setCreateTime(task.getCreateTime());
  }

  public void complete() {
    fireEvent(TaskListener.EVENTNAME_COMPLETE);
    delete(DELETE_REASON_COMPLETED);
    if (executionId!=null) {
      getExecution().signal(null, null);
    }
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
      .getSession(RuntimeSession.class)
      .findVariablesByTaskId(id);
  }

  // execution ////////////////////////////////////////////////////////////////

  public ExecutionEntity getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = Context
        .getCommandContext()
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
      
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = Context
          .getCommandContext()
          .getDbSqlSession()
          .selectById(HistoricTaskInstanceEntity.class, id);
        historicTaskInstance.setExecutionId(executionId);
      }
      
    } else {
      this.execution = null;
      this.executionId = null;
      this.processInstanceId = null;
      this.processDefinitionId = null;
      
      throw new ActivitiException("huh?");
    }
  }
    
  // task assignment //////////////////////////////////////////////////////////
  
  public IdentityLinkEntity addIdentityLink(String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.createAndInsert();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setTask(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    return identityLinkEntity;
  }
  
  public void deleteIdentityLink(String userId, String groupId, String type) {
    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getTaskSession()
      .findIdentityLinkByTaskUserGroupAndType(id, userId, groupId, type);
    
    for (IdentityLinkEntity identityLink: identityLinks) {
      identityLink.delete();
    }
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
      execution.setVariables(parameters);
    }
  }
  
  public String toString() {
    return "Task["+id+"]";
  }
  
  // special setters //////////////////////////////////////////////////////////
  
  public void setName(String name) {
    this.name = name;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the 
    // setAssignee outside a service method.  E.g. while creating a new task.
    if (commandContext!=null) {
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setName(name);
        }
      }
    }
  }
  
  public void setDescription(String description) {
    this.description = description;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the 
    // setAssignee outside a service method.  E.g. while creating a new task.
    if (commandContext!=null) {
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setDescription(description);
        }
      }
    }
  }

  public void setAssignee(String assignee) {
    if (assignee==null && this.assignee==null) {
      return;
    }
    if (assignee!=null && assignee.equals(this.assignee)) {
      return;
    }
    
    this.assignee = assignee;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the 
    // setAssignee outside a service method.  E.g. while creating a new task.
    if (commandContext!=null) {
      fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
      
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setAssignee(assignee);
        }
      }
    }
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
          taskListener.notify(this);
        }
      }
    }
  }

  // modified getters and setters /////////////////////////////////////////////
  
  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
    this.taskDefinitionKey = taskDefinition.getKey();
    
    CommandContext commandContext = Context.getCommandContext();
    if(commandContext != null) {
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setTaskDefinitionKey(this.taskDefinitionKey);
        }
      }
    }
  }

  public TaskDefinition getTaskDefinition() {
    if (taskDefinition==null && taskDefinitionKey!=null) {
      RepositorySession repositorySession = Context.getCommandContext().getSession(RepositorySession.class);
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

  public String getDescription() {
    return description;
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
  
  // used in MyBatis mapping: no need to fire event every time the task is fetched from db and assignee is set by reflection
  // MyBatis usage:  <result property="assigneeWithoutFireEvent" column="ASSIGNEE_" jdbcType="VARCHAR"/>
  public void setAssigneeWithoutFireEvent(String assignee) {
    this.assignee = assignee;
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
      int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
        HistoricTaskInstanceEntity historicTaskInstance = commandContext.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
        if (historicTaskInstance!=null) {
          historicTaskInstance.setTaskDefinitionKey(this.taskDefinitionKey);
        }
      }
    }
  }

  public String getEventName() {
    return eventName;
  }
  
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

}
