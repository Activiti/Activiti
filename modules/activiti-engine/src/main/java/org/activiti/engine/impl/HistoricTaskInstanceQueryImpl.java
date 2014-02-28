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
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceQueryImpl extends AbstractVariableQueryImpl<HistoricTaskInstanceQuery, HistoricTaskInstance> implements HistoricTaskInstanceQuery {
  
  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionKeyLike;
  protected String processDefinitionName;
  protected String processDefinitionNameLike;
  protected String processInstanceId;
  protected String processInstanceBusinessKey;
  protected String processInstanceBusinessKeyLike;
  protected String executionId;
  protected String taskId;
  protected String taskName;
  protected String taskNameLike;
  protected String taskParentTaskId;
  protected String taskDescription;
  protected String taskDescriptionLike;
  protected String taskDeleteReason;
  protected String taskDeleteReasonLike;
  protected String taskOwner;
  protected String taskOwnerLike;
  protected String taskAssignee;
  protected String taskAssigneeLike;
  protected String taskDefinitionKey;
  protected String taskDefinitionKeyLike;
  protected String candidateUser;
  protected String candidateGroup;
  private List<String> candidateGroups;
  protected String involvedUser;
  protected Integer taskPriority;
  protected Integer taskMinPriority;
  protected Integer taskMaxPriority;
  protected boolean finished;
  protected boolean unfinished;
  protected boolean processFinished;
  protected boolean processUnfinished;
  protected Date dueDate;
  protected Date dueAfter;
  protected Date dueBefore;
  protected boolean withoutDueDate = false;
  protected Date creationDate;
  protected Date creationAfterDate;
  protected Date creationBeforeDate;
  protected Date completedDate;
  protected Date completedAfterDate;
  protected Date completedBeforeDate;
  protected String category;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected boolean includeTaskLocalVariables = false;
  protected boolean includeProcessVariables = false;

  public HistoricTaskInstanceQueryImpl() {
  }

  public HistoricTaskInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    ensureVariablesInitialized();
    checkQueryOk();
    return commandContext
      .getHistoricTaskInstanceEntityManager()
      .findHistoricTaskInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricTaskInstance> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    if (includeTaskLocalVariables || includeProcessVariables) {
      return commandContext
          .getHistoricTaskInstanceEntityManager()
          .findHistoricTaskInstancesAndVariablesByQueryCriteria(this);
    } else {
      return commandContext
          .getHistoricTaskInstanceEntityManager()
          .findHistoricTaskInstancesByQueryCriteria(this);
    }
  }


  public HistoricTaskInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl processInstanceBusinessKey(String processInstanceBusinessKey) {
    this.processInstanceBusinessKey = processInstanceBusinessKey;
    return this;
  }

  public HistoricTaskInstanceQueryImpl processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike) {
    this.processInstanceBusinessKeyLike = processInstanceBusinessKeyLike;
    return this;
  }

  public HistoricTaskInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricTaskInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public HistoricTaskInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public HistoricTaskInstanceQuery processDefinitionKeyLike(String processDefinitionKeyLike) {
    this.processDefinitionKeyLike = processDefinitionKeyLike;
    return this;
  }
  
  public HistoricTaskInstanceQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }
  
  public HistoricTaskInstanceQuery processDefinitionNameLike(String processDefinitionNameLike) {
    this.processDefinitionNameLike = processDefinitionNameLike;
    return this;
  }

  public HistoricTaskInstanceQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  public HistoricTaskInstanceQuery taskName(String taskName) {
    this.taskName = taskName;
    return this;
  }

  public HistoricTaskInstanceQuery taskNameLike(String taskNameLike) {
    this.taskNameLike = taskNameLike;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskParentTaskId(String parentTaskId) {
    this.taskParentTaskId = parentTaskId;
    return this;
  }

  public HistoricTaskInstanceQuery taskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
    return this;
  }

  public HistoricTaskInstanceQuery taskDescriptionLike(String taskDescriptionLike) {
    this.taskDescriptionLike = taskDescriptionLike;
    return this;
  }

  public HistoricTaskInstanceQuery taskDeleteReason(String taskDeleteReason) {
    this.taskDeleteReason = taskDeleteReason;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDeleteReasonLike(String taskDeleteReasonLike) {
    this.taskDeleteReasonLike = taskDeleteReasonLike;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
    return this;
  }

  public HistoricTaskInstanceQuery taskAssigneeLike(String taskAssigneeLike) {
    this.taskAssigneeLike = taskAssigneeLike;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskOwner(String taskOwner) {
    this.taskOwner = taskOwner;
    return this;
  }

  public HistoricTaskInstanceQuery taskOwnerLike(String taskOwnerLike) {
    this.taskOwnerLike = taskOwnerLike;
    return this;
  }
  
  public HistoricTaskInstanceQuery finished() {
    this.finished = true;
    return this;
  }
  
  public HistoricTaskInstanceQuery unfinished() {
    this.unfinished = true;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskVariableValueEquals(String variableName, Object variableValue) {
    return variableValueEquals(variableName, variableValue);
  }
  
  public HistoricTaskInstanceQuery taskVariableValueEquals(Object variableValue) {
    return variableValueEquals(variableValue);
  }
  
  public HistoricTaskInstanceQuery taskVariableValueEqualsIgnoreCase(String name, String value) {
    return variableValueEqualsIgnoreCase(name, value);
  }
  
  public HistoricTaskInstanceQuery taskVariableValueNotEqualsIgnoreCase(String name, String value) {
    return variableValueNotEqualsIgnoreCase(name, value);
  }

  public HistoricTaskInstanceQuery taskVariableValueNotEquals(String variableName, Object variableValue) {
    return variableValueNotEquals(variableName, variableValue);
  }
  
  public HistoricTaskInstanceQuery taskVariableValueGreaterThan(String name, Object value) {
    return variableValueGreaterThan(name, value);
  }

  public HistoricTaskInstanceQuery taskVariableValueGreaterThanOrEqual(String name, Object value) {
    return variableValueGreaterThanOrEqual(name, value);
  }

  public HistoricTaskInstanceQuery taskVariableValueLessThan(String name, Object value) {
    return variableValueLessThan(name, value);
  }

  public HistoricTaskInstanceQuery taskVariableValueLessThanOrEqual(String name, Object value) {
    return variableValueLessThanOrEqual(name, value);
  }

  public HistoricTaskInstanceQuery taskVariableValueLike(String name, String value) {
    return variableValueLike(name, value);
  }

  public HistoricTaskInstanceQuery processVariableValueEquals(String variableName, Object variableValue) {
    return variableValueEquals(variableName, variableValue, false);
  }

  public HistoricTaskInstanceQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    return variableValueNotEquals(variableName, variableValue, false);
  }
  
  public HistoricTaskInstanceQuery processVariableValueEquals(Object variableValue) {
    return variableValueEquals(variableValue, false);
  }
  
  public HistoricTaskInstanceQuery processVariableValueEqualsIgnoreCase(String name, String value) {
    return variableValueEqualsIgnoreCase(name, value, false);
  }
  
  public HistoricTaskInstanceQuery processVariableValueNotEqualsIgnoreCase(String name, String value) {
    return variableValueNotEqualsIgnoreCase(name, value, false);
  }
  
  public HistoricTaskInstanceQuery processVariableValueGreaterThan(String name, Object value) {
    return variableValueGreaterThan(name, value, false);
  }

  public HistoricTaskInstanceQuery processVariableValueGreaterThanOrEqual(String name, Object value) {
    return variableValueGreaterThanOrEqual(name, value, false);
  }

  public HistoricTaskInstanceQuery processVariableValueLessThan(String name, Object value) {
    return variableValueLessThan(name, value, false);
  }

  public HistoricTaskInstanceQuery processVariableValueLessThanOrEqual(String name, Object value) {
    return variableValueLessThanOrEqual(name, value, false);
  }

  public HistoricTaskInstanceQuery processVariableValueLike(String name, String value) {
    return variableValueLike(name, value, false);
  }
  
  public HistoricTaskInstanceQuery taskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDefinitionKeyLike(String taskDefinitionKeyLike) {
    this.taskDefinitionKeyLike = taskDefinitionKeyLike;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskPriority(Integer taskPriority) {
    this.taskPriority = taskPriority;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskMinPriority(Integer taskMinPriority) {
    this.taskMinPriority = taskMinPriority;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskMaxPriority(Integer taskMaxPriority) {
    this.taskMaxPriority = taskMaxPriority;
    return this;
  }
  
  public HistoricTaskInstanceQuery processFinished() {
    this.processFinished = true;
    return this;
  }
  
  public HistoricTaskInstanceQuery processUnfinished() {
    this.processUnfinished = true;
    return this;
  }
  
  protected void ensureVariablesInitialized() {    
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for (QueryVariableValue var : queryVariableValues) {
      var.initialize(types);
    }
  }
  
  public HistoricTaskInstanceQuery taskDueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCreatedOn(Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCreatedBefore(Date creationBeforeDate) {
    this.creationBeforeDate = creationBeforeDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCreatedAfter(Date creationAfterDate) {
    this.creationAfterDate = creationAfterDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCompletedOn(Date completedDate) {
    this.completedDate = completedDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCompletedBefore(Date completedBeforeDate) {
    this.completedBeforeDate = completedBeforeDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCompletedAfter(Date completedAfterDate) {
    this.completedAfterDate = completedAfterDate;
    return this;
  }
  
  public HistoricTaskInstanceQuery withoutTaskDueDate() {
    this.withoutDueDate = true;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCategory(String category) {
    this.category = category;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskCandidateUser(String candidateUser) {
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
  
  public HistoricTaskInstanceQuery taskCandidateGroup(String candidateGroup) {
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
  
  public HistoricTaskInstanceQuery taskCandidateGroupIn(List<String> candidateGroups) {
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
  
  @Override
  public HistoricTaskInstanceQuery taskInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
    return this;
  }
  
  public HistoricTaskInstanceQuery taskTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("task tenant id is null");
  	}
  	this.tenantId = tenantId;
  	return this;
  }
  
  public HistoricTaskInstanceQuery taskTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("task tenant id is null");
  	}
  	this.tenantIdLike = tenantIdLike;
  	return this;
  }
  
  public HistoricTaskInstanceQuery taskWithoutTenantId() {
  	this.withoutTenantId = true;
  	return this;
  }
  
  
  public HistoricTaskInstanceQuery includeTaskLocalVariables() {
    this.includeTaskLocalVariables = true;
    return this;
  }
  
  public HistoricTaskInstanceQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }

  // ordering /////////////////////////////////////////////////////////////////

  public HistoricTaskInstanceQueryImpl orderByTaskId() {
    orderBy(HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID);
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricTaskInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricTaskInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceDuration() {
    orderBy(HistoricTaskInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricTaskInstanceEndTime() {
    orderBy(HistoricTaskInstanceQueryProperty.END);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricTaskInstanceQueryProperty.START);
    return this;
  }
  
  @Override
  public HistoricTaskInstanceQuery orderByHistoricTaskInstanceStartTime() {
    orderBy(HistoricTaskInstanceQueryProperty.START);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskName() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_NAME);
    return this;
  }

  public HistoricTaskInstanceQueryImpl orderByTaskDescription() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTaskAssignee() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_ASSIGNEE);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskOwner() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_OWNER);
    return this;
  }

  public HistoricTaskInstanceQuery orderByTaskDueDate() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DUE_DATE);
    return this;
  }
  
  public HistoricTaskInstanceQueryImpl orderByDeleteReason() {
    orderBy(HistoricTaskInstanceQueryProperty.DELETE_REASON);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTaskDefinitionKey() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_DEFINITION_KEY);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTaskPriority() {
    orderBy(HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
    return this;
  }
  
  public HistoricTaskInstanceQuery orderByTenantId() {
  	orderBy(HistoricTaskInstanceQueryProperty.TENANT_ID_);
  	return this;
  }
  
  public String getMssqlOrDB2OrderBy() {
    String specialOrderBy = super.getOrderBy();
    if (specialOrderBy != null && specialOrderBy.length() > 0) {
      specialOrderBy = specialOrderBy.replace("RES.", "TEMPRES_");
    }
    return specialOrderBy;
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
      .getGroupIdentityManager()
      .findGroupsByUser(candidateUser);
    List<String> groupIds = new ArrayList<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getProcessInstanceBusinessKey() {
    return processInstanceBusinessKey;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public boolean isFinished() {
    return finished;
  }
  public boolean isUnfinished() {
    return unfinished;
  }
  public String getTaskName() {
    return taskName;
  }
  public String getTaskNameLike() {
    return taskNameLike;
  }
  public String getTaskDescription() {
    return taskDescription;
  }
  public String getTaskDescriptionLike() {
    return taskDescriptionLike;
  }
  public String getTaskDeleteReason() {
    return taskDeleteReason;
  }
  public String getTaskDeleteReasonLike() {
    return taskDeleteReasonLike;
  }
  public String getTaskAssignee() {
    return taskAssignee;
  }
  public String getTaskAssigneeLike() {
    return taskAssigneeLike;
  }
  public String getTaskId() {
    return taskId;
  }
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  public String getTaskOwnerLike() {
    return taskOwnerLike;
  }
  public String getTaskOwner() {
    return taskOwner;
  }
  public String getTaskParentTaskId() {
    return taskParentTaskId;
  }
  public Date getCreationDate() {
    return creationDate;
  }
  public String getCandidateUser() {
    return candidateUser;
  }
  public String getCandidateGroup() {
    return candidateGroup;
  }
  public String getInvolvedUser() {
    return involvedUser;
  }
}
