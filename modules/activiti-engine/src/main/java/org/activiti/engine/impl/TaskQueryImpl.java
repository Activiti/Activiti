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
package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

/**
 * @author Joram Barrez
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class TaskQueryImpl extends AbstractQuery<TaskQuery, Task> implements TaskQuery {
  
  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String name;
  protected String nameLike;
  protected String description;
  protected String descriptionLike;
  protected Integer priority;
  protected Integer minPriority;
  protected Integer maxPriority;
  protected String assignee;
  protected String involvedUser;
  protected String owner;
  protected boolean unassigned = false;
  protected boolean noDelegationState = false;
  protected DelegationState delegationState;
  protected String candidateUser;
  protected String candidateGroup;
  private List<String> candidateGroups;
  protected String processInstanceId;
  protected String executionId;
  protected Date createTime;
  protected Date createTimeBefore;
  protected Date createTimeAfter;
  protected String key;
  protected String keyLike;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String processInstanceBusinessKey;
  protected List<TaskQueryVariableValue> variables = new ArrayList<TaskQueryVariableValue>();
  protected Date dueDate;
  protected Date dueBefore;
  protected Date dueAfter;
  protected SuspensionState suspensionState;
  protected boolean excludeSubtasks = false;

  public TaskQueryImpl() {
  }
  
  public TaskQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TaskQueryImpl taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("Task id is null");
    }
    this.taskId = taskId;
    return this;
  }
  
  public TaskQueryImpl taskName(String name) {
    if (name == null) {
      throw new ActivitiIllegalArgumentException("Task name is null");
    }
    this.name = name;
    return this;
  }
  
  public TaskQueryImpl taskNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiIllegalArgumentException("Task namelike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  public TaskQueryImpl taskDescription(String description) {
    if (description == null) {
      throw new ActivitiIllegalArgumentException("Description is null");
    }
    this.description = description;
    return this;
  }
  
  public TaskQuery taskDescriptionLike(String descriptionLike) {
    if (descriptionLike == null) {
      throw new ActivitiIllegalArgumentException("Task descriptionlike is null");
    }
    this.descriptionLike = descriptionLike;
    return this;
  }
  
  public TaskQuery taskPriority(Integer priority) {
    if (priority == null) {
      throw new ActivitiIllegalArgumentException("Priority is null");
    }
    this.priority = priority;
    return this;
  }

  public TaskQuery taskMinPriority(Integer minPriority) {
    if (minPriority == null) {
      throw new ActivitiIllegalArgumentException("Min Priority is null");
    }
    this.minPriority = minPriority;
    return this;
  }

  public TaskQuery taskMaxPriority(Integer maxPriority) {
    if (maxPriority == null) {
      throw new ActivitiIllegalArgumentException("Max Priority is null");
    }
    this.maxPriority = maxPriority;
    return this;
  }

  public TaskQueryImpl taskAssignee(String assignee) {
    if (assignee == null) {
      throw new ActivitiIllegalArgumentException("Assignee is null");
    }
    this.assignee = assignee;
    return this;
  }
  
  public TaskQueryImpl taskOwner(String owner) {
    if (owner == null) {
      throw new ActivitiIllegalArgumentException("Owner is null");
    }
    this.owner = owner;
    return this;
  }
  
  /** @see {@link #taskUnassigned} */
  @Deprecated
  public TaskQuery taskUnnassigned() {
    return taskUnassigned();
  }

  public TaskQuery taskUnassigned() {
    this.unassigned = true;
    return this;
  }

  public TaskQuery taskDelegationState(DelegationState delegationState) {
    if (delegationState == null) {
      this.noDelegationState = true;
    } else {
      this.delegationState = delegationState;
    }
    return this;
  }

  public TaskQueryImpl taskCandidateUser(String candidateUser) {
    if (candidateUser == null) {
      throw new ActivitiIllegalArgumentException("Candidate user is null");
    }
    if (candidateGroup != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateUser and candidateGroup");
    }
    if (candidateGroups != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateUser and candidateGroupIn");
    }
    this.candidateUser = candidateUser;
    return this;
  }
  
  public TaskQueryImpl taskInvolvedUser(String involvedUser) {
    if (involvedUser == null) {
      throw new ActivitiIllegalArgumentException("Involved user is null");
    }
    this.involvedUser = involvedUser;
    return this;
  }
  
  public TaskQueryImpl taskCandidateGroup(String candidateGroup) {
    if (candidateGroup == null) {
      throw new ActivitiIllegalArgumentException("Candidate group is null");
    }
    if (candidateUser != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroup and candidateUser");
    }
    if (candidateGroups != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroup and candidateGroupIn");
    }
    this.candidateGroup = candidateGroup;
    return this;
  }
  
  public TaskQuery taskCandidateGroupIn(List<String> candidateGroups) {
    if(candidateGroups == null) {
      throw new ActivitiIllegalArgumentException("Candidate group list is null");
    }
    if(candidateGroups.size()== 0) {
      throw new ActivitiIllegalArgumentException("Candidate group list is empty");
    }
    
    if (candidateUser != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroupIn and candidateUser");
    }
    if (candidateGroup != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroupIn and candidateGroup");
    }
    
    this.candidateGroups = candidateGroups;
    return this;
  }
  
  public TaskQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public TaskQueryImpl processInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
    return this;
  }
  
  public TaskQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }
  
  public TaskQueryImpl taskCreatedOn(Date createTime) {
    this.createTime = createTime;
    return this;
  }
  
  public TaskQuery taskCreatedBefore(Date before) {
    this.createTimeBefore = before;
    return this;
  }
  
  public TaskQuery taskCreatedAfter(Date after) {
    this.createTimeAfter = after;
    return this;
  }
  
  public TaskQuery taskDefinitionKey(String key) {
    this.key = key;
    return this;
  }
  
  public TaskQuery taskDefinitionKeyLike(String keyLike) {
    this.keyLike = keyLike;
    return this;
  }
  
  public TaskQuery taskVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true));
    return this;
  }
  
  public TaskQuery taskVariableValueEquals(Object variableValue) {
    variables.add(new TaskQueryVariableValue(null, variableValue, QueryOperator.EQUALS, true));
    return this;
  }
  
  public TaskQuery taskVariableValueEqualsIgnoreCase(String name, String value) {
    if(value == null) {
      throw new ActivitiIllegalArgumentException("value is null");
    }
    variables.add(new TaskQueryVariableValue(name, value.toLowerCase(), QueryOperator.EQUALS_IGNORE_CASE, true));
    return this;
  }
  
  public TaskQuery taskVariableValueNotEqualsIgnoreCase(String name, String value) {
    if(value == null) {
      throw new ActivitiIllegalArgumentException("value is null");
    }
    variables.add(new TaskQueryVariableValue(name, value.toLowerCase(), QueryOperator.NOT_EQUALS_IGNORE_CASE, true));
    return this;
  }

  public TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.NOT_EQUALS, true));
    return this;
  }

  public TaskQuery processVariableValueEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, false));
    return this;
  }

  public TaskQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    variables.add(new TaskQueryVariableValue(variableName, variableValue, QueryOperator.NOT_EQUALS, false));
    return this;
  }
  
  public TaskQuery processVariableValueEquals(Object variableValue) {
    variables.add(new TaskQueryVariableValue(null, variableValue, QueryOperator.EQUALS, false));
    return this;
  }
  
  public TaskQuery processVariableValueEqualsIgnoreCase(String name, String value) {
    if(value == null) {
      throw new ActivitiIllegalArgumentException("value is null");
    }
    variables.add(new TaskQueryVariableValue(name, value.toLowerCase(), QueryOperator.EQUALS_IGNORE_CASE, false));
    return this;
  }
  
  public TaskQuery processVariableValueNotEqualsIgnoreCase(String name, String value) {
    if(value == null) {
      throw new ActivitiIllegalArgumentException("value is null");
    }
    variables.add(new TaskQueryVariableValue(name, value.toLowerCase(), QueryOperator.NOT_EQUALS_IGNORE_CASE, false));
    return this;
  }

  public TaskQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public TaskQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public TaskQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }
  
  public TaskQuery dueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  
  public TaskQuery dueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
    return this;
  }
  
  public TaskQuery dueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }

  public TaskQuery excludeSubtasks() {
    this.excludeSubtasks = true;
    return this;
  }
  
  public TaskQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  public TaskQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  public List<String> getCandidateGroups() {
    if (candidateGroup!=null) {
      List<String> candidateGroupList = new java.util.ArrayList<String>(1);
      candidateGroupList.add(candidateGroup);
      return candidateGroupList;
    } else if (candidateUser != null) {
      return getGroupsForCandidateUser(candidateUser);
    } else if(candidateGroups != null) {
      return candidateGroups;
    }
    return null;
  }
  
  protected List<String> getGroupsForCandidateUser(String candidateUser) {
    // TODO: Discuss about removing this feature? Or document it properly and maybe recommend to not use it
    // and explain alternatives
    List<Group> groups = Context
      .getCommandContext()
      .getGroupEntityManager()
      .findGroupsByUser(candidateUser);
    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }
  
  protected void ensureVariablesInitialized() {    
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for(QueryVariableValue var : variables) {
      var.initialize(types);
    }
  }

  //ordering ////////////////////////////////////////////////////////////////
  
  public TaskQuery orderByTaskId() {
    return orderBy(TaskQueryProperty.TASK_ID);
  }
  
  public TaskQuery orderByTaskName() {
    return orderBy(TaskQueryProperty.NAME);
  }
  
  public TaskQuery orderByTaskDescription() {
    return orderBy(TaskQueryProperty.DESCRIPTION);
  }
  
  public TaskQuery orderByTaskPriority() {
    return orderBy(TaskQueryProperty.PRIORITY);
  }
  
  public TaskQuery orderByProcessInstanceId() {
    return orderBy(TaskQueryProperty.PROCESS_INSTANCE_ID);
  }
  
  public TaskQuery orderByExecutionId() {
    return orderBy(TaskQueryProperty.EXECUTION_ID);
  }
  
  public TaskQuery orderByTaskAssignee() {
    return orderBy(TaskQueryProperty.ASSIGNEE);
  }
  
  public TaskQuery orderByTaskCreateTime() {
    return orderBy(TaskQueryProperty.CREATE_TIME);
  }
  
  public TaskQuery orderByDueDate() {
    return orderBy(TaskQueryProperty.DUE_DATE);
  }
  
  //results ////////////////////////////////////////////////////////////////
  
  public List<Task> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getTaskEntityManager()
      .findTasksByQueryCriteria(this);
  }
  
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getTaskEntityManager()
      .findTaskCountByQueryCriteria(this);
  }
  
  //getters ////////////////////////////////////////////////////////////////

  public String getName() {
    return name;
  }
  public String getNameLike() {
    return nameLike;
  }
  public String getAssignee() {
    return assignee;
  }
  public boolean getUnassigned() {
    return unassigned;
  }
  public DelegationState getDelegationState() {
    return delegationState;
  }
  public boolean getNoDelegationState() {
    return noDelegationState;
  }
  public String getDelegationStateString() {
    return (delegationState!=null ? delegationState.toString() : null);
  }
  public String getCandidateUser() {
    return candidateUser;
  }
  public String getCandidateGroup() {
    return candidateGroup;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getTaskId() {
    return taskId;
  }
  public String getDescription() {
    return description;
  }
  public String getDescriptionLike() {
    return descriptionLike;
  }
  public Integer getPriority() {
    return priority;
  }
  public Date getCreateTime() {
    return createTime;
  }
  public Date getCreateTimeBefore() {
    return createTimeBefore;
  }
  public Date getCreateTimeAfter() {
    return createTimeAfter;
  }
  public String getKey() {
    return key;
  }
  public String getKeyLike() {
    return keyLike;
  }
  public List<TaskQueryVariableValue> getVariables() {
    return variables;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionName() {
    return processDefinitionName;
  }
  public String getProcessInstanceBusinessKey() {
    return processInstanceBusinessKey;
  }
  public boolean getExcludeSubtasks() {
    return excludeSubtasks;
  }
}
