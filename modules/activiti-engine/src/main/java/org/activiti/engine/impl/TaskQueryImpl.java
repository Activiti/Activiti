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

import org.activiti.engine.ActivitiException;
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
 * @author Tijs Rademakers
 */
public class TaskQueryImpl extends AbstractVariableQueryImpl<TaskQuery, Task> implements TaskQuery {
  
  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  protected String name;
  protected String nameLike;
  protected String nameLikeIgnoreCase;
  protected List<String> nameList;
  protected List<String> nameListIgnoreCase;
  protected String description;
  protected String descriptionLike;
  protected String descriptionLikeIgnoreCase;
  protected Integer priority;
  protected Integer minPriority;
  protected Integer maxPriority;
  protected String assignee;
  protected String assigneeLike;
  protected String assigneeLikeIgnoreCase;
  protected String involvedUser;

  protected String involvedUserLike;
  protected String involvedUserLikeIgnoreCase;
  protected String owner;
  protected String ownerLike;
  protected String ownerLikeIgnoreCase;
  protected boolean unassigned = false;
  protected boolean noDelegationState = false;
  protected DelegationState delegationState;
  protected String candidateUser;
  protected String candidateGroup;
  protected List<String> candidateGroups;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected String processInstanceId;
  protected String executionId;
  protected Date createTime;
  protected Date createTimeBefore;
  protected Date createTimeAfter;
  protected String category;
  protected String key;
  protected String keyLike;
  protected String processDefinitionKey;
  protected String processDefinitionKeyLike;
  protected String processDefinitionKeyLikeIgnoreCase;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String processDefinitionNameLike;
  protected List<String> processCategoryInList;
  protected List<String> processCategoryNotInList;
  protected String deploymentId;
  protected List<String> deploymentIds;
  protected String processInstanceBusinessKey;
  protected String processInstanceBusinessKeyLike;
  protected String processInstanceBusinessKeyLikeIgnoreCase;
  protected Date dueDate;
  protected Date dueBefore;
  protected Date dueAfter;
  protected boolean withoutDueDate = false;
  protected SuspensionState suspensionState;
  protected boolean excludeSubtasks = false;
  protected boolean includeTaskLocalVariables = false;
  protected boolean includeProcessVariables = false;
  protected String userIdForCandidateAndAssignee;
  protected boolean bothCandidateAndAssigned = false;
  protected boolean orActive;
  protected TaskQueryImpl orQueryObject;
  
  public TaskQueryImpl() {
  }
  
  public TaskQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public TaskQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public TaskQueryImpl(CommandExecutor commandExecutor, String databaseType) {
    super(commandExecutor);
    this.databaseType = databaseType;
  }
  
  public TaskQueryImpl taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("Task id is null");
    }
    
    if(orActive) {
      orQueryObject.taskId = taskId;
    } else {
      this.taskId = taskId;
    }
    return this;
  }
  
  public TaskQueryImpl taskName(String name) {
    if (name == null) {
      throw new ActivitiIllegalArgumentException("Task name is null");
    }
    
    if(orActive) {
      orQueryObject.name = name;
    } else {
      this.name = name;
    }
    return this;
  }

  @Override
  public TaskQuery taskNameIn(List<String> nameList) {
    if(nameList == null) {
      throw new ActivitiIllegalArgumentException("Task name list is null");
    }
    if(nameList.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Task name list is empty");
    }
    for (String name : nameList) {
      if (name == null) {
        throw new ActivitiIllegalArgumentException("None of the given task names can be null");
      }
    }

    if (name != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both taskNameIn and name");
    }
    if (nameLike != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both taskNameIn and nameLike");
    }
    if (nameLikeIgnoreCase != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both taskNameIn and nameLikeIgnoreCase");
    }

    if(orActive) {
      orQueryObject.nameList = nameList;
    } else {
      this.nameList = nameList;
    }
    return this;
  }

  @Override
  public TaskQuery taskNameInIgnoreCase(List<String> nameList) {
    if(nameList == null) {
      throw new ActivitiIllegalArgumentException("Task name list is null");
    }
    if(nameList.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Task name list is empty");
    }
    for (String name : nameList) {
      if (name == null) {
        throw new ActivitiIllegalArgumentException("None of the given task names can be null");
      }
    }

    if (name != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both taskNameInIgnoreCase and name");
    }
    if (nameLike != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both taskNameInIgnoreCase and nameLike");
    }
    if (nameLikeIgnoreCase != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both taskNameInIgnoreCase and nameLikeIgnoreCase");
    }

    final int nameListSize = nameList.size();
    final List<String> caseIgnoredNameList = new ArrayList<String>(nameListSize);
    for (String name : nameList) {
      caseIgnoredNameList.add(name.toLowerCase());
    }

    if (orActive) {
      this.orQueryObject.nameListIgnoreCase = caseIgnoredNameList;
    } else {
      this.nameListIgnoreCase = caseIgnoredNameList;
    }
    return this;
  }

  public TaskQueryImpl taskNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiIllegalArgumentException("Task namelike is null");
    }
    
    if(orActive) {
      orQueryObject.nameLike = nameLike;
    } else {
      this.nameLike = nameLike;
    }
    return this;
  }
  
  public TaskQuery taskNameLikeIgnoreCase(String nameLikeIgnoreCase) {
  	 if (nameLikeIgnoreCase == null) {
       throw new ActivitiIllegalArgumentException("Task nameLikeIgnoreCase is null");
     }
     
     if(orActive) {
       orQueryObject.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
     } else {
       this.nameLikeIgnoreCase = nameLikeIgnoreCase.toLowerCase();
     }
     return this;
  }

  public TaskQueryImpl taskDescription(String description) {
    if (description == null) {
      throw new ActivitiIllegalArgumentException("Description is null");
    }
    
    if(orActive) {
      orQueryObject.description = description;
    } else {
      this.description = description;
    }
    return this;
  }
  
  public TaskQuery taskDescriptionLike(String descriptionLike) {
    if (descriptionLike == null) {
      throw new ActivitiIllegalArgumentException("Task descriptionlike is null");
    }
    if(orActive) {
      orQueryObject.descriptionLike = descriptionLike;
    } else {
      this.descriptionLike = descriptionLike;
    }
    return this;
  }
  
  public TaskQuery taskDescriptionLikeIgnoreCase(String descriptionLikeIgnoreCase) {
  	if (descriptionLikeIgnoreCase == null) {
      throw new ActivitiIllegalArgumentException("Task descriptionLikeIgnoreCase is null");
    }
    if(orActive) {
      orQueryObject.descriptionLikeIgnoreCase = descriptionLikeIgnoreCase.toLowerCase();
    } else {
      this.descriptionLikeIgnoreCase = descriptionLikeIgnoreCase.toLowerCase();
    }
    return this;
  }
  
  public TaskQuery taskPriority(Integer priority) {
    if (priority == null) {
      throw new ActivitiIllegalArgumentException("Priority is null");
    }
    if(orActive) {
      orQueryObject.priority = priority;
    } else {
      this.priority = priority;
    }
    return this;
  }

  public TaskQuery taskMinPriority(Integer minPriority) {
    if (minPriority == null) {
      throw new ActivitiIllegalArgumentException("Min Priority is null");
    }
    if(orActive) {
      orQueryObject.minPriority = minPriority;
    } else {
      this.minPriority = minPriority;
    }
    return this;
  }

  public TaskQuery taskMaxPriority(Integer maxPriority) {
    if (maxPriority == null) {
      throw new ActivitiIllegalArgumentException("Max Priority is null");
    }
    if(orActive) {
      orQueryObject.maxPriority = maxPriority;
    } else {
      this.maxPriority = maxPriority;
    }
    return this;
  }

  public TaskQueryImpl taskAssignee(String assignee) {
    if (assignee == null) {
      throw new ActivitiIllegalArgumentException("Assignee is null");
    }
    if(orActive) {
      orQueryObject.assignee = assignee;
    } else {
      this.assignee = assignee;
    }
    return this;
  }
  
  public TaskQueryImpl taskAssigneeLike(String assigneeLike) {
    if (assigneeLike == null) {
      throw new ActivitiIllegalArgumentException("AssigneeLike is null");
    }
    if(orActive) {
      orQueryObject.assigneeLike = assignee;
    } else {
      this.assigneeLike = assigneeLike;
    }
    return this;
  }
  
  public TaskQuery taskAssigneeLikeIgnoreCase(String assigneeLikeIgnoreCase) {
  	 if (assigneeLikeIgnoreCase == null) {
       throw new ActivitiIllegalArgumentException("assigneeLikeIgnoreCase is null");
     }
     if(orActive) {
       orQueryObject.assigneeLikeIgnoreCase = assigneeLikeIgnoreCase.toLowerCase();
     } else {
       this.assigneeLikeIgnoreCase = assigneeLikeIgnoreCase.toLowerCase();
     }
     return this;
  }
  
  public TaskQueryImpl taskOwner(String owner) {
    if (owner == null) {
      throw new ActivitiIllegalArgumentException("Owner is null");
    }
    if(orActive) {
      orQueryObject.owner = owner;
    } else {
      this.owner = owner;
    }
    return this;
  }
  
  public TaskQueryImpl taskOwnerLike(String ownerLike) {
    if (ownerLike == null) {
      throw new ActivitiIllegalArgumentException("Owner is null");
    }
    if(orActive) {
      orQueryObject.ownerLike = ownerLike;
    } else {
      this.ownerLike = ownerLike;
    }
    return this;
  }
  
  public TaskQuery taskOwnerLikeIgnoreCase(String ownerLikeIgnoreCase) {
  	if (ownerLikeIgnoreCase == null) {
      throw new ActivitiIllegalArgumentException("OwnerLikeIgnoreCase");
    }
    if(orActive) {
      orQueryObject.ownerLikeIgnoreCase = ownerLikeIgnoreCase.toLowerCase();
    } else {
      this.ownerLikeIgnoreCase = ownerLikeIgnoreCase.toLowerCase();
    }
    return this;
  }
  
  /** @see {@link #taskUnassigned} */
  @Deprecated
  public TaskQuery taskUnnassigned() {
    return taskUnassigned();
  }

  public TaskQuery taskUnassigned() {
    if(orActive) {
      orQueryObject.unassigned = true;
    } else {
      this.unassigned = true;
    }
    return this;
  }

  public TaskQuery taskDelegationState(DelegationState delegationState) {
    if(orActive) {
      if (delegationState == null) {
        orQueryObject.noDelegationState = true;
      } else {
        orQueryObject.delegationState = delegationState;
      }
    } else {
      if (delegationState == null) {
        this.noDelegationState = true;
      } else {
        this.delegationState = delegationState;
      }
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
    
    if(orActive) {
      orQueryObject.candidateUser = candidateUser;
    } else {
      this.candidateUser = candidateUser;
    }
    
    return this;
  }
  
  public TaskQueryImpl taskInvolvedUser(String involvedUser) {
    if (involvedUser == null) {
      throw new ActivitiIllegalArgumentException("Involved user is null");
    }
    if(orActive) {
      orQueryObject.involvedUser = involvedUser;
    } else {
      this.involvedUser = involvedUser;
    }
    return this;
  }
  public TaskQueryImpl taskInvolvedUserLike(String involvedUserLike) {
    if (involvedUserLike == null) {
      throw new ActivitiIllegalArgumentException("Involved user is null");
    }
    if(orActive) {
      orQueryObject.involvedUserLike = involvedUserLike;
    } else {
      this.involvedUserLike = involvedUserLike;
    }
    return this;
  }
  public TaskQueryImpl taskInvolvedUserLikeIgnoreCase(String involvedUserLikeIgnoreCase) {
    if (involvedUserLikeIgnoreCase == null) {
      throw new ActivitiIllegalArgumentException("Involved user is null");
    }
    if(orActive) {
      orQueryObject.involvedUserLikeIgnoreCase = involvedUserLikeIgnoreCase;
    } else {
      this.involvedUserLikeIgnoreCase = involvedUserLikeIgnoreCase;
    }
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
    if(orActive) {
      orQueryObject.candidateGroup = candidateGroup;
    } else {
      this.candidateGroup = candidateGroup;
    }
    return this;
  }

  @Override
  public TaskQuery taskCandidateOrAssigned(String userIdForCandidateAndAssignee) {
    if (candidateGroup != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set candidateGroup");
    }
    if (candidateUser != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroup and candidateUser");
    }
    
    if(orActive) {
      orQueryObject.bothCandidateAndAssigned = true;
      orQueryObject.userIdForCandidateAndAssignee = userIdForCandidateAndAssignee;
    } else {
      this.bothCandidateAndAssigned = true;
      this.userIdForCandidateAndAssignee = userIdForCandidateAndAssignee;
    }
    
    return this;
  }

  public TaskQuery taskCandidateGroupIn(List<String> candidateGroups) {
    if(candidateGroups == null) {
      throw new ActivitiIllegalArgumentException("Candidate group list is null");
    }
    if(candidateGroups.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Candidate group list is empty");
    }
    
    if (candidateUser != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroupIn and candidateUser");
    }
    if (candidateGroup != null) {
      throw new ActivitiIllegalArgumentException("Invalid query usage: cannot set both candidateGroupIn and candidateGroup");
    }
    
    if(orActive) {
      orQueryObject.candidateGroups = candidateGroups;
    } else {
      this.candidateGroups = candidateGroups;
    }
    return this;
  }
  
  public TaskQuery taskTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("task tenant id is null");
  	}
  	 if(orActive) {
       orQueryObject.tenantId = tenantId;
     } else {
       this.tenantId = tenantId;
     }
  	return this;
  }
  
  public TaskQuery taskTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("task tenant id is null");
  	}
  	if(orActive) {
      orQueryObject.tenantIdLike = tenantIdLike;
    } else {
      this.tenantIdLike = tenantIdLike;
    }
  	return this;
  }
  
  public TaskQuery taskWithoutTenantId() {
    if(orActive) {
      orQueryObject.withoutTenantId = true;
    } else {
      this.withoutTenantId = true;
    }
  	return this;
  }
  
  public TaskQueryImpl processInstanceId(String processInstanceId) {
    if(orActive) {
      orQueryObject.processInstanceId = processInstanceId;
    } else {
      this.processInstanceId = processInstanceId;
    }
    return this;
  }
  
  public TaskQueryImpl processInstanceBusinessKey(String processInstanceBusinessKey) {
    if(orActive) {
      orQueryObject.processInstanceBusinessKey = processInstanceBusinessKey;
    } else {
      this.processInstanceBusinessKey = processInstanceBusinessKey;
    }
    return this;
  }
  
  public TaskQueryImpl processInstanceBusinessKeyLike(String processInstanceBusinessKeyLike) {
    if(orActive) {
      orQueryObject.processInstanceBusinessKeyLike = processInstanceBusinessKeyLike;
    } else {
      this.processInstanceBusinessKeyLike = processInstanceBusinessKeyLike;
    }
    return this;
  }
  
  public TaskQuery processInstanceBusinessKeyLikeIgnoreCase(String processInstanceBusinessKeyLikeIgnoreCase) {
  	 if(orActive) {
       orQueryObject.processInstanceBusinessKeyLikeIgnoreCase = processInstanceBusinessKeyLikeIgnoreCase.toLowerCase();
     } else {
       this.processInstanceBusinessKeyLikeIgnoreCase = processInstanceBusinessKeyLikeIgnoreCase.toLowerCase();
     }
     return this;
  }
  
  public TaskQueryImpl executionId(String executionId) {
    if(orActive) {
      orQueryObject.executionId = executionId;
    } else {
      this.executionId = executionId;
    }
    return this;
  }
  
  public TaskQueryImpl taskCreatedOn(Date createTime) {
    if(orActive) {
      orQueryObject.createTime = createTime;
    } else {
      this.createTime = createTime;
    }
    return this;
  }
  
  public TaskQuery taskCreatedBefore(Date before) {
    if(orActive) {
      orQueryObject.createTimeBefore = before;
    } else {
      this.createTimeBefore = before;
    }
    return this;
  }
  
  public TaskQuery taskCreatedAfter(Date after) {
    if(orActive) {
      orQueryObject.createTimeAfter = after;
    } else {
      this.createTimeAfter = after;
    }
    return this;
  }
  
  public TaskQuery taskCategory(String category) {
    if(orActive) {
      orQueryObject.category = category;
    } else {
      this.category = category;
    }
  	return this;
  }
  
  public TaskQuery taskDefinitionKey(String key) {
    if(orActive) {
      orQueryObject.key = key;
    } else {
      this.key = key;
    }
    return this;
  }
  
  public TaskQuery taskDefinitionKeyLike(String keyLike) {
    if(orActive) {
      orQueryObject.keyLike = keyLike;
    } else {
      this.keyLike = keyLike;
    }
    return this;
  }
  
  public TaskQuery taskVariableValueEquals(String variableName, Object variableValue) {
    if(orActive) {
      orQueryObject.variableValueEquals(variableName, variableValue);
    } else {
      this.variableValueEquals(variableName, variableValue);
    }
    return this;
  }
  
  public TaskQuery taskVariableValueEquals(Object variableValue) {
    if(orActive) {
      orQueryObject.variableValueEquals(variableValue);
    } else {
      this.variableValueEquals(variableValue);
    }
    return this;
  }
  
  public TaskQuery taskVariableValueEqualsIgnoreCase(String name, String value) {
    if(orActive) {
      orQueryObject.variableValueEqualsIgnoreCase(name, value);
    } else {
      this.variableValueEqualsIgnoreCase(name, value);
    }
    return this;
  }
  
  public TaskQuery taskVariableValueNotEqualsIgnoreCase(String name, String value) {
    if(orActive) {
      orQueryObject.variableValueNotEqualsIgnoreCase(name, value);
    } else {
      this.variableValueNotEqualsIgnoreCase(name, value);
    }
    return this;
  }

  public TaskQuery taskVariableValueNotEquals(String variableName, Object variableValue) {
    if(orActive) {
      orQueryObject.variableValueNotEquals(variableName, variableValue);
    } else {
      this.variableValueNotEquals(variableName, variableValue);
    }
    return this;
  }
  
  public TaskQuery taskVariableValueGreaterThan(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueGreaterThan(name, value);
    } else {
      this.variableValueGreaterThan(name, value);
    }
    return this;
  }

  public TaskQuery taskVariableValueGreaterThanOrEqual(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueGreaterThanOrEqual(name, value);
    } else {
      this.variableValueGreaterThanOrEqual(name, value);
    }
    return this;
  }

  public TaskQuery taskVariableValueLessThan(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueLessThan(name, value);
    } else {
      this.variableValueLessThan(name, value);
    }
    return this;
  }

  public TaskQuery taskVariableValueLessThanOrEqual(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueLessThanOrEqual(name, value);
    } else {
      this.variableValueLessThanOrEqual(name, value);
    }
    return this;
  }

  public TaskQuery taskVariableValueLike(String name, String value) {
    if(orActive) {
      orQueryObject.variableValueLike(name, value);
    } else {
      this.variableValueLike(name, value);
    }
    return this;
  }

  public TaskQuery processVariableValueEquals(String variableName, Object variableValue) {
    if(orActive) {
      orQueryObject.variableValueEquals(variableName, variableValue, false);
    } else {
      this.variableValueEquals(variableName, variableValue, false);
    }
    return this;
  }

  public TaskQuery processVariableValueNotEquals(String variableName, Object variableValue) {
    if(orActive) {
      orQueryObject.variableValueNotEquals(variableName, variableValue, false);
    } else {
      this.variableValueNotEquals(variableName, variableValue, false);
    }
    return this;
  }
  
  public TaskQuery processVariableValueEquals(Object variableValue) {
    if(orActive) {
      orQueryObject.variableValueEquals(variableValue, false);
    } else {
      this.variableValueEquals(variableValue, false);
    }
    return this;
  }
  
  public TaskQuery processVariableValueEqualsIgnoreCase(String name, String value) {
    if(orActive) {
      orQueryObject.variableValueEqualsIgnoreCase(name, value, false);
    } else {
      this.variableValueEqualsIgnoreCase(name, value, false);
    }
    return this;
  }
  
  public TaskQuery processVariableValueNotEqualsIgnoreCase(String name, String value) {
    if(orActive) {
      orQueryObject.variableValueNotEqualsIgnoreCase(name, value, false);
    } else {
      this.variableValueNotEqualsIgnoreCase(name, value, false);
    }
    return this;
  }
  
  public TaskQuery processVariableValueGreaterThan(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueGreaterThan(name, value, false);
    } else {
      this.variableValueGreaterThan(name, value, false);
    }
    return this;
  }

  public TaskQuery processVariableValueGreaterThanOrEqual(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueGreaterThanOrEqual(name, value, false);
    } else {
      this.variableValueGreaterThanOrEqual(name, value, false);
    }
    return this;
  }

  public TaskQuery processVariableValueLessThan(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueLessThan(name, value, false);
    } else {
      this.variableValueLessThan(name, value, false);
    }
    return this;
  }

  public TaskQuery processVariableValueLessThanOrEqual(String name, Object value) {
    if(orActive) {
      orQueryObject.variableValueLessThanOrEqual(name, value, false);
    } else {
      this.variableValueLessThanOrEqual(name, value, false);
    }
    return this;
  }

  public TaskQuery processVariableValueLike(String name, String value) {
    if(orActive) {
      orQueryObject.variableValueLike(name, value, false);
    } else {
      this.variableValueLike(name, value, false);
    }
    return this;
  }

  public TaskQuery processDefinitionKey(String processDefinitionKey) {
    if(orActive) {
      orQueryObject.processDefinitionKey = processDefinitionKey;
    } else {
      this.processDefinitionKey = processDefinitionKey;
    }
    return this;
  }
  
  public TaskQuery processDefinitionKeyLike(String processDefinitionKeyLike) {
    if(orActive) {
      orQueryObject.processDefinitionKeyLike = processDefinitionKeyLike;
    } else {
      this.processDefinitionKeyLike = processDefinitionKeyLike;
    }
    return this;
  }
  
  public TaskQuery processDefinitionKeyLikeIgnoreCase(String processDefinitionKeyLikeIgnoreCase) {
  	if(orActive) {
      orQueryObject.processDefinitionKeyLikeIgnoreCase = processDefinitionKeyLikeIgnoreCase.toLowerCase();
    } else {
      this.processDefinitionKeyLikeIgnoreCase = processDefinitionKeyLikeIgnoreCase.toLowerCase();
    }
    return this;
  }

  public TaskQuery processDefinitionId(String processDefinitionId) {
    if(orActive) {
      orQueryObject.processDefinitionId = processDefinitionId;
    } else {
      this.processDefinitionId = processDefinitionId;
    }
    return this;
  }
  
  public TaskQuery processDefinitionName(String processDefinitionName) {
    if(orActive) {
      orQueryObject.processDefinitionName = processDefinitionName;
    } else {
      this.processDefinitionName = processDefinitionName;
    }
    return this;
  }
  
  public TaskQuery processDefinitionNameLike(String processDefinitionNameLike) {
    if(orActive) {
      orQueryObject.processDefinitionNameLike = processDefinitionNameLike;
    } else {
      this.processDefinitionNameLike = processDefinitionNameLike;
    }
    return this;
  }

  @Override
  public TaskQuery processCategoryIn(List<String> processCategoryInList) {
    if(processCategoryInList == null) {
      throw new ActivitiIllegalArgumentException("Process category list is null");
    }
    if(processCategoryInList.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Process category list is empty");
    }
    for (String processCategory : processCategoryInList) {
      if (processCategory == null) {
        throw new ActivitiIllegalArgumentException("None of the given process categories can be null");
      }
    }

    if(orActive) {
      orQueryObject.processCategoryInList = processCategoryInList;
    } else {
      this.processCategoryInList = processCategoryInList;
    }
    return this;
  }

  @Override
  public TaskQuery processCategoryNotIn(List<String> processCategoryNotInList) {
    if(processCategoryNotInList == null) {
      throw new ActivitiIllegalArgumentException("Process category list is null");
    }
    if(processCategoryNotInList.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Process category list is empty");
    }
    for (String processCategory : processCategoryNotInList) {
      if (processCategory == null) {
        throw new ActivitiIllegalArgumentException("None of the given process categories can be null");
      }
    }

    if(orActive) {
      orQueryObject.processCategoryNotInList = processCategoryNotInList;
    } else {
      this.processCategoryNotInList = processCategoryNotInList;
    }
    return this;
  }

  public TaskQuery deploymentId(String deploymentId) {
    if(orActive) {
      orQueryObject.deploymentId = deploymentId;
    } else {
      this.deploymentId = deploymentId;
    }
    return this;
  }
  
  public TaskQuery deploymentIdIn(List<String> deploymentIds) {
    if(orActive) {
      orQueryObject.deploymentIds = deploymentIds;
    } else {
      this.deploymentIds = deploymentIds;
    }
    return this;
  }
  
  public TaskQuery dueDate(Date dueDate) {
    if(orActive) {
      orQueryObject.dueDate = dueDate;
      orQueryObject.withoutDueDate = false;
    } else {
      this.dueDate = dueDate;
      this.withoutDueDate = false;
    }
    return this;
  }
  
  @Override
  public TaskQuery taskDueDate(Date dueDate) {
  	return dueDate(dueDate);
  }
  
  public TaskQuery dueBefore(Date dueBefore) {
    if(orActive) {
      orQueryObject.dueBefore = dueBefore;
      orQueryObject.withoutDueDate = false;
    } else {
      this.dueBefore = dueBefore;
      this.withoutDueDate = false;
    }
    return this;
  }
  
  @Override
  public TaskQuery taskDueBefore(Date dueDate) {
  	return dueBefore(dueDate);
  }
  
  public TaskQuery dueAfter(Date dueAfter) {
    if(orActive) {
      orQueryObject.dueAfter = dueAfter;
      orQueryObject.withoutDueDate = false;
    } else {
      this.dueAfter = dueAfter;
      this.withoutDueDate = false;
    }
    return this;
  }
  
  public TaskQuery taskDueAfter(Date dueDate) {
    return dueAfter(dueDate);
  }
  
  public TaskQuery withoutDueDate() {
    if(orActive) {
      orQueryObject.withoutDueDate = true;
    } else {
      this.withoutDueDate = true;
    }
    return this;
  }
  
  @Override
  public TaskQuery withoutTaskDueDate() {
  	return withoutDueDate();
  }

  public TaskQuery excludeSubtasks() {
    if(orActive) {
      orQueryObject.excludeSubtasks = true;
    } else {
      this.excludeSubtasks = true;
    }
    return this;
  }
  
  public TaskQuery suspended() {
    if(orActive) {
      orQueryObject.suspensionState = SuspensionState.SUSPENDED;
    } else {
      this.suspensionState = SuspensionState.SUSPENDED;
    }
    return this;
  }

  public TaskQuery active() {
    if(orActive) {
      orQueryObject.suspensionState = SuspensionState.ACTIVE;
    } else {
      this.suspensionState = SuspensionState.ACTIVE;
    }
    return this;
  }
  
  public TaskQuery includeTaskLocalVariables() {
    this.includeTaskLocalVariables = true;
    return this;
  }
  
  public TaskQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }

  public List<String> getCandidateGroups() {
    if (candidateGroup!=null) {
      List<String> candidateGroupList = new ArrayList<String>(1);
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
  
  protected void ensureVariablesInitialized() {    
    VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
    for (QueryVariableValue var : queryVariableValues) {
      var.initialize(types);
    }
    
    if(orQueryObject != null) {
      orQueryObject.ensureVariablesInitialized();
    }
  }
  
  //or query ////////////////////////////////////////////////////////////////
  
  @Override
  public TaskQuery or() {
    if(orActive || orQueryObject != null) {
        throw new ActivitiException("or() can only be called once");
    }
    
    // Create instance of the orQuery
    orActive = true;
    orQueryObject = new TaskQueryImpl();
    return this;
  }
  
  @Override
  public TaskQuery endOr() {
    if(!orActive || orQueryObject == null) {
      throw new ActivitiException("endOr() can only be called after calling or()");
    }
    orActive = false;
    return this;
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
  
  @Override
  public TaskQuery orderByProcessDefinitionId() {
  	return orderBy(TaskQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  public TaskQuery orderByTaskAssignee() {
    return orderBy(TaskQueryProperty.ASSIGNEE);
  }
  
  @Override
  public TaskQuery orderByTaskOwner() {
  	return orderBy(TaskQueryProperty.OWNER);
  }
  
  public TaskQuery orderByTaskCreateTime() {
    return orderBy(TaskQueryProperty.CREATE_TIME);
  }
  
  public TaskQuery orderByDueDate() {
    return orderBy(TaskQueryProperty.DUE_DATE);
  }
  
  @Override
  public TaskQuery orderByTaskDueDate() {
  	return orderByDueDate();
  }
  
  @Override
  public TaskQuery orderByTaskDefinitionKey() {
  	return orderBy(TaskQueryProperty.TASK_DEFINITION_KEY);
  }
  
  public TaskQuery orderByDueDateNullsFirst() {
  	return orderBy(TaskQueryProperty.DUE_DATE, NullHandlingOnOrder.NULLS_FIRST);
  }
  
  @Override
  public TaskQuery orderByDueDateNullsLast() {
  	return orderBy(TaskQueryProperty.DUE_DATE, NullHandlingOnOrder.NULLS_LAST);
  }
  
  @Override
  public TaskQuery orderByTenantId() {
  	return orderBy(TaskQueryProperty.TENANT_ID);
  }
  
  public String getMssqlOrDB2OrderBy() {
    String specialOrderBy = super.getOrderBy();
    if (specialOrderBy != null && specialOrderBy.length() > 0) {
      specialOrderBy = specialOrderBy.replace("RES.", "TEMPRES_");
    }
    return specialOrderBy;
  }
  
  //results ////////////////////////////////////////////////////////////////

  public List<Task> executeList(CommandContext commandContext, Page page) {
    ensureVariablesInitialized();
    checkQueryOk();
    if (includeTaskLocalVariables || includeProcessVariables) {
      return commandContext
          .getTaskEntityManager()
          .findTasksAndVariablesByQueryCriteria(this);
    } else {
      return commandContext
          .getTaskEntityManager()
          .findTasksByQueryCriteria(this);
    }
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
  public List<String> getNameList() {
    return nameList;
  }
  public List<String> getNameListIgnoreCase() {
    return nameListIgnoreCase;
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
  public String getTenantId() {
	return tenantId;
  }
  public String getTenantIdLike() {
	return tenantIdLike;
  }
  public boolean isWithoutTenantId() {
	return withoutTenantId;
  }
  public String getUserIdForCandidateAndAssignee() {
    return userIdForCandidateAndAssignee;
  }
  public TaskQueryImpl getOrQueryObject() {
    return orQueryObject;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public Integer getMinPriority() {
    return minPriority;
  }

  public Integer getMaxPriority() {
    return maxPriority;
  }

  public String getAssigneeLike() {
    return assigneeLike;
  }

  public String getInvolvedUser() {
    return involvedUser;
  }
  public String getInvolvedUserLike() {
    return involvedUserLike;
  }
  public String getInvolvedUserLikeIgnoreCase() {
    return involvedUserLikeIgnoreCase;
  }



  public String getOwner() {
    return owner;
  }

  public String getOwnerLike() {
    return ownerLike;
  }

  public String getCategory() {
    return category;
  }

  public String getProcessDefinitionKeyLike() {
    return processDefinitionKeyLike;
  }

  public String getProcessDefinitionNameLike() {
    return processDefinitionNameLike;
  }

  public List<String> getProcessCategoryInList() {
    return processCategoryInList;
  }

  public List<String> getProcessCategoryNotInList() {
    return processCategoryNotInList;
  }

  public String getDeploymentId() {
    return deploymentId;
  }
  
  public List<String> getDeploymentIds() {
    return deploymentIds;
  }

  public String getProcessInstanceBusinessKeyLike() {
    return processInstanceBusinessKeyLike;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public Date getDueBefore() {
    return dueBefore;
  }

  public Date getDueAfter() {
    return dueAfter;
  }

  public boolean isWithoutDueDate() {
    return withoutDueDate;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  public boolean isIncludeTaskLocalVariables() {
    return includeTaskLocalVariables;
  }

  public boolean isIncludeProcessVariables() {
    return includeProcessVariables;
  }

  public boolean isBothCandidateAndAssigned() {
    return bothCandidateAndAssigned;
  }
  
  public String getNameLikeIgnoreCase() {
		return nameLikeIgnoreCase;
	}

	public String getDescriptionLikeIgnoreCase() {
		return descriptionLikeIgnoreCase;
	}

	public String getAssigneeLikeIgnoreCase() {
		return assigneeLikeIgnoreCase;
	}

	public String getOwnerLikeIgnoreCase() {
		return ownerLikeIgnoreCase;
	}

	public String getProcessInstanceBusinessKeyLikeIgnoreCase() {
		return processInstanceBusinessKeyLikeIgnoreCase;
	}
	
	public String getProcessDefinitionKeyLikeIgnoreCase() {
		return processDefinitionKeyLikeIgnoreCase;
	}

	public boolean isOrActive() {
    return orActive;
  }
  
}
