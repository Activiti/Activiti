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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteable;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;

/**




 */
public class TaskEntityImpl extends VariableScopeImpl implements TaskEntity, Serializable, BulkDeleteable {

  public static final String DELETE_REASON_COMPLETED = "completed";
  public static final String DELETE_REASON_DELETED = "deleted";

  private static final long serialVersionUID = 1L;

  protected String owner;
  protected int assigneeUpdatedCount; // needed for v5 compatibility
  protected String originalAssignee; // needed for v5 compatibility
  protected String assignee;
  protected DelegationState delegationState;

  protected String parentTaskId;

  protected String name;
  protected String localizedName;
  protected String description;
  protected String localizedDescription;
  protected int priority = DEFAULT_PRIORITY;
  protected Date createTime; // The time when the task has been created
  protected Date dueDate;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected String category;

  protected boolean isIdentityLinksInitialized;
  protected List<IdentityLinkEntity> taskIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();

  protected String executionId;
  protected ExecutionEntity execution;

  protected String processInstanceId;
  protected ExecutionEntity processInstance;

  protected String processDefinitionId;

  protected String taskDefinitionKey;
  protected String formKey;

  protected boolean isDeleted;
  protected boolean isCanceled;
  protected String eventName;
  protected ActivitiListener currentActivitiListener;

  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;

  protected List<VariableInstanceEntity> queryVariables;

  protected boolean forcedUpdate;

  protected Date claimTime;

  protected Integer appVersion;

  protected String businessKey;

  public TaskEntityImpl() {
    
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("assignee", this.assignee);
    persistentState.put("owner", this.owner);
    persistentState.put("name", this.name);
    persistentState.put("priority", this.priority);
    if (businessKey != null) {
      persistentState.put("businessKey", this.businessKey);
    }
    if (executionId != null) {
      persistentState.put("executionId", this.executionId);
    }
    if (processDefinitionId != null) {
      persistentState.put("processDefinitionId", this.processDefinitionId);
    }
    if (createTime != null) {
      persistentState.put("createTime", this.createTime);
    }
    if (description != null) {
      persistentState.put("description", this.description);
    }
    if (dueDate != null) {
      persistentState.put("dueDate", this.dueDate);
    }
    if (parentTaskId != null) {
      persistentState.put("parentTaskId", this.parentTaskId);
    }
    if (delegationState != null) {
      persistentState.put("delegationState", this.delegationState);
    }

    persistentState.put("suspensionState", this.suspensionState);

    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }

    if (claimTime != null) {
      persistentState.put("claimTime", this.claimTime);
    }

    return persistentState;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public void forceUpdate() {
    this.forcedUpdate = true;
  }

  // variables //////////////////////////////////////////////////////////////////

  @Override
  protected VariableScopeImpl getParentVariableScope() {
    if (getExecution() != null) {
      return (ExecutionEntityImpl) execution;
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
    return Context.getCommandContext().getVariableInstanceEntityManager().findVariableInstancesByTaskId(id);
  }

  @Override
  protected VariableInstanceEntity createVariableInstance(String variableName, Object value, ExecutionEntity sourceActivityExecution) {
    VariableInstanceEntity result = super.createVariableInstance(variableName, value, sourceActivityExecution);

    // Dispatch event, if needed
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context
          .getProcessEngineConfiguration()
          .getEventDispatcher()
          .dispatchEvent(
              ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_CREATED, variableName, value, result.getType(), result.getTaskId(), result.getExecutionId(), getProcessInstanceId(),
                  getProcessDefinitionId()));
    }
    return result;
  }

  @Override
  protected void updateVariableInstance(VariableInstanceEntity variableInstance, Object value, ExecutionEntity sourceActivityExecution) {
    super.updateVariableInstance(variableInstance, value, sourceActivityExecution);

    // Dispatch event, if needed
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context
          .getProcessEngineConfiguration()
          .getEventDispatcher()
          .dispatchEvent(
              ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_UPDATED, variableInstance.getName(), value, variableInstance.getType(), variableInstance.getTaskId(),
                  variableInstance.getExecutionId(), getProcessInstanceId(), getProcessDefinitionId()));
    }
  }

  // execution //////////////////////////////////////////////////////////////////

  public ExecutionEntity getExecution() {
    if ((execution == null) && (executionId != null)) {
      this.execution = Context.getCommandContext().getExecutionEntityManager().findById(executionId);
    }
    return execution;
  }

  // task assignment ////////////////////////////////////////////////////////////

  @Override
  public void addCandidateUser(String userId) {
    Context.getCommandContext().getIdentityLinkEntityManager().addCandidateUser(this, userId);
  }

  @Override
  public void addCandidateUsers(Collection<String> candidateUsers) {
    Context.getCommandContext().getIdentityLinkEntityManager().addCandidateUsers(this, candidateUsers);
  }

  @Override
  public void addCandidateGroup(String groupId) {
    Context.getCommandContext().getIdentityLinkEntityManager().addCandidateGroup(this, groupId);
  }

  @Override
  public void addCandidateGroups(Collection<String> candidateGroups) {
    Context.getCommandContext().getIdentityLinkEntityManager().addCandidateGroups(this, candidateGroups);
  }

  @Override
  public void addUserIdentityLink(String userId, String identityLinkType) {
    Context.getCommandContext().getIdentityLinkEntityManager().addUserIdentityLink(this, userId, identityLinkType);
  }

  @Override
  public void addGroupIdentityLink(String groupId, String identityLinkType) {
    Context.getCommandContext().getIdentityLinkEntityManager().addGroupIdentityLink(this, groupId, identityLinkType);
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

  public void deleteCandidateGroup(String groupId) {
    deleteGroupIdentityLink(groupId, IdentityLinkType.CANDIDATE);
  }

  public void deleteCandidateUser(String userId) {
    deleteUserIdentityLink(userId, IdentityLinkType.CANDIDATE);
  }

  public void deleteGroupIdentityLink(String groupId, String identityLinkType) {
    if (groupId != null) {
      Context.getCommandContext().getIdentityLinkEntityManager().deleteIdentityLink(this, null, groupId, identityLinkType);
    }
  }

  public void deleteUserIdentityLink(String userId, String identityLinkType) {
    if (userId != null) {
      Context.getCommandContext().getIdentityLinkEntityManager().deleteIdentityLink(this, userId, null, identityLinkType);
    }
  }

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      taskIdentityLinkEntities = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinksByTaskId(id);
      isIdentityLinksInitialized = true;
    }

    return taskIdentityLinkEntities;
  }

  public void setExecutionVariables(Map<String, Object> parameters) {
    if (getExecution() != null) {
      execution.setVariables(parameters);
    }
  }

  public void setName(String taskName) {
    this.name = taskName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setAssignee(String assignee) {
    this.originalAssignee = this.assignee;
    this.assignee = assignee;
    assigneeUpdatedCount++;
  }
  
  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getFormKey() {
    return formKey;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  // Override from VariableScopeImpl

  @Override
  protected boolean isActivityIdUsedForDetails() {
    return false;
  }

  // Overridden to avoid fetching *all* variables (as is the case in the super // call)
  @Override
  protected VariableInstanceEntity getSpecificVariable(String variableName) {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext == null) {
      throw new ActivitiException("lazy loading outside command context");
    }
    VariableInstanceEntity variableInstance = commandContext.getVariableInstanceEntityManager().findVariableInstanceByTaskAndName(id, variableName);

    return variableInstance;
  }

  @Override
  protected List<VariableInstanceEntity> getSpecificVariables(Collection<String> variableNames) {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext == null) {
      throw new ActivitiException("lazy loading outside command context");
    }
    return commandContext.getVariableInstanceEntityManager().findVariableInstancesByTaskAndNames(id, variableNames);
  }

  // regular getters and setters ////////////////////////////////////////////////////////

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getName() {
    if (localizedName != null && localizedName.length() > 0) {
      return localizedName;
    } else {
      return name;
    }
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }

  public String getDescription() {
    if (localizedDescription != null && localizedDescription.length() > 0) {
      return localizedDescription;
    } else {
      return description;
    }
  }
  
  public String getLocalizedDescription() {
    return localizedDescription;
  }

  public void setLocalizedDescription(String localizedDescription) {
    this.localizedDescription = localizedDescription;
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
  
  public String getOriginalAssignee() {
    // Don't ask. A stupid hack for v5 compatibility
    if (assigneeUpdatedCount > 1) {
      return originalAssignee;
    } else {
      return assignee;
    }
  }
  
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  
  public ActivitiListener getCurrentActivitiListener() {
    return currentActivitiListener;
  }

  public void setCurrentActivitiListener(ActivitiListener currentActivitiListener) {
    this.currentActivitiListener = currentActivitiListener;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public ExecutionEntity getProcessInstance() {
    if (processInstance == null && processInstanceId != null) {
      processInstance = Context.getCommandContext().getExecutionEntityManager().findById(processInstanceId);
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

  public String getDelegationStateString() { //Needed for Activiti 5 compatibility, not exposed in terface
    return (delegationState != null ? delegationState.toString() : null);
  }

  public void setDelegationStateString(String delegationStateString) {
    this.delegationState = (delegationStateString != null ? DelegationState.valueOf(DelegationState.class, delegationStateString) : null);
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public boolean isCanceled() {
	  return isCanceled;
  }

  public void setCanceled(boolean isCanceled) {
	  this.isCanceled = isCanceled;
  }
  
  public String getParentTaskId() {
    return parentTaskId;
  }


  @Override
  public String getBusinessKey() {
    return businessKey;
  }

  public Map<String, VariableInstanceEntity> getVariableInstanceEntities() {
    ensureVariableInstancesInitialized();
    return variableInstances;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }

  public String getCategory() {
    return category;
  }

  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  public Map<String, Object> getTaskLocalVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    if (queryVariables != null) {
      for (VariableInstanceEntity variableInstance : queryVariables) {
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
      for (VariableInstanceEntity variableInstance : queryVariables) {
        if (variableInstance.getId() != null && variableInstance.getTaskId() == null) {
          variables.put(variableInstance.getName(), variableInstance.getValue());
        }
      }
    }
    return variables;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public List<VariableInstanceEntity> getQueryVariables() {
    if (queryVariables == null && Context.getCommandContext() != null) {
      queryVariables = new VariableInitializingList();
    }
    return queryVariables;
  }

  public void setQueryVariables(List<VariableInstanceEntity> queryVariables) {
    this.queryVariables = queryVariables;
  }

  public Date getClaimTime() {
    return claimTime;
  }

  public void setClaimTime(Date claimTime) {
    this.claimTime = claimTime;
  }

  public Integer getAppVersion(){
      return this.appVersion;
  }

  public void setAppVersion (Integer appVersion){
      this.appVersion = appVersion;
  }

  public String toString() {
    return "Task[id=" + id + ", name=" + name + "]";
  }

}
