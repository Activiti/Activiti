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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */

public class ExecutionEntity extends VariableScopeImpl 
    implements DelegateExecution, Execution, ProcessInstance, Entity, HasRevision {

  private static final long serialVersionUID = 1L;

  // current position /////////////////////////////////////////////////////////

  protected FlowElement currentFlowElement;

  /**
   * the process instance. this is the root of the execution tree. the processInstance of a process instance is a self reference.
   */
  protected ExecutionEntity processInstance;

  /** the parent execution */
  protected ExecutionEntity parent;

  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionEntity> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected ExecutionEntity superExecution;

  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected ExecutionEntity subProcessInstance;

  /** The tenant identifier (if any) */
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected String name;

  protected Date lockTime;

  // state/type of execution //////////////////////////////////////////////////
  
  // TODO: remnants of Activti 5. Check which ones should be kept and which ones deleted.

  protected boolean isActive = true;
  protected boolean isScope = true;
  protected boolean isConcurrent;
  protected boolean isEnded;
  protected boolean isEventScope;

  // events ///////////////////////////////////////////////////////////////////
  
  // TODO: still needed in v6?

  protected String eventName;

  // associated entities /////////////////////////////////////////////////////

  // (we cache associated entities here to minimize db queries)
  protected List<EventSubscriptionEntity> eventSubscriptions;
  protected List<JobEntity> jobs;
  protected List<TaskEntity> tasks;
  protected List<IdentityLinkEntity> identityLinks;
  protected int cachedEntityState;

  // cascade deletion ////////////////////////////////////////////////////////

  protected String deleteReason;
  
  protected int revision = 1;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();

  /**
   * persisted reference to the processDefinition.
   * 
   * @see #processDefinition
   * @see #setProcessDefinition(ProcessDefinitionImpl)
   * @see #getProcessDefinition()
   */
  protected String processDefinitionId;

  /**
   * persisted reference to the process definition key.
   */
  protected String processDefinitionKey;

  /**
   * persisted reference to the process definition name.
   */
  protected String processDefinitionName;

  /**
   * persisted reference to the process definition version.
   */
  protected Integer processDefinitionVersion;

  /**
   * persisted reference to the deployment id.
   */
  protected String deploymentId;

  /**
   * persisted reference to the current position in the diagram within the {@link #processDefinition}.
   * 
   * @see #activity
   * @see #setActivity(ActivityImpl)
   * @see #getActivity()
   */
  protected String activityId;

  /**
   * The name of the current activity position
   */
  protected String activityName;

  /**
   * persisted reference to the process instance.
   * 
   * @see #getProcessInstance()
   */
  protected String processInstanceId;

  /**
   * persisted reference to the business key.
   */
  protected String businessKey;

  /**
   * persisted reference to the parent of this execution.
   * 
   * @see #getParent()
   * @see #setParentId(String)
   */
  protected String parentId;

  /**
   * persisted reference to the super execution of this execution
   * 
   * @See {@link #getSuperExecution()}
   * @see #setSuperExecution(ExecutionEntity)
   */
  protected String superExecutionId;
  
  protected String rootProcessInstanceId;

  protected boolean forcedUpdate;

  protected List<VariableInstanceEntity> queryVariables;
  
  protected boolean isDeleted; // TODO: should be in entity superclass probably

  public ExecutionEntity() {
  }
  
  //persistent state /////////////////////////////////////////////////////////

 public Object getPersistentState() {
   Map<String, Object> persistentState = new HashMap<String, Object>();
   persistentState.put("processDefinitionId", this.processDefinitionId);
   persistentState.put("businessKey", this.businessKey);
   persistentState.put("activityId", this.activityId);
   persistentState.put("isActive", this.isActive);
   persistentState.put("isConcurrent", this.isConcurrent);
   persistentState.put("isScope", this.isScope);
   persistentState.put("isEventScope", this.isEventScope);
   persistentState.put("parentId", parentId);
   persistentState.put("name", name);
   persistentState.put("lockTime", lockTime);
   persistentState.put("superExecution", this.superExecutionId);
   persistentState.put("rootProcessInstanceId", this.rootProcessInstanceId);
   if (forcedUpdate) {
     persistentState.put("forcedUpdate", Boolean.TRUE);
   }
   persistentState.put("suspensionState", this.suspensionState);
   persistentState.put("cachedEntityState", this.cachedEntityState);
   return persistentState;
 }
  
  // The current flow element, will be filled during operation execution

  public FlowElement getCurrentFlowElement() {
    if (currentFlowElement == null) {
      String processDefinitionId = getProcessDefinitionId();
      if (processDefinitionId != null) {
        org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        currentFlowElement = process.getFlowElement(getCurrentActivityId(), true);
      }
    }
    return currentFlowElement;
  }

  public void setCurrentFlowElement(FlowElement currentFlowElement) {
    this.currentFlowElement = currentFlowElement;
    if (currentFlowElement != null) {
      this.activityId = currentFlowElement.getId();
    } else {
      this.activityId = null;
    }
  }

  // executions ///////////////////////////////////////////////////////////////

  /** ensures initialization and returns the non-null executions list */
  public List<ExecutionEntity> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureExecutionsInitialized() {
    if (executions == null) {
      this.executions = (List) Context.getCommandContext().getExecutionEntityManager().findChildExecutionsByParentExecutionId(id);
    }
  }

  public void setExecutions(List<ExecutionEntity> executions) {
    this.executions = executions;
  }

  // business key ////////////////////////////////////////////////////////////

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessInstanceBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  // process definition ///////////////////////////////////////////////////////

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
    this.processDefinitionVersion = processDefinitionVersion;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public ExecutionEntity getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }

  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance = Context.getCommandContext().getExecutionEntityManager().findById(processInstanceId);
    }
  }

  public void setProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = processInstance;
    if (processInstance != null) {
      this.processInstanceId = this.processInstance.getId();
    }
  }

  public boolean isProcessInstanceType() {
    return parentId == null;
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = Context.getCommandContext().getExecutionEntityManager().findById(parentId);
    }
  }

  public void setParent(ExecutionEntity parent) {
    this.parent = parent;

    if (parent != null) {
      this.parentId = parent.getId();
    } else {
      this.parentId = null;
    }
  }

  // super- and subprocess executions /////////////////////////////////////////

  public String getSuperExecutionId() {
    return superExecutionId;
  }

  public ExecutionEntity getSuperExecution() {
    ensureSuperExecutionInitialized();
    return superExecution;
  }

  public void setSuperExecution(ExecutionEntity superExecution) {
    this.superExecution = superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }

    if (superExecution != null) {
      this.superExecutionId = ((ExecutionEntity) superExecution).getId();
    } else {
      this.superExecutionId = null;
    }
  }

  protected void ensureSuperExecutionInitialized() {
    if (superExecution == null && superExecutionId != null) {
      superExecution = Context.getCommandContext().getExecutionEntityManager().findById(superExecutionId);
    }
  }

  public ExecutionEntity getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }

  public void setSubProcessInstance(ExecutionEntity subProcessInstance) {
    this.subProcessInstance = subProcessInstance;
  }

  protected void ensureSubProcessInstanceInitialized() {
    if (subProcessInstance == null) {
      subProcessInstance = Context.getCommandContext().getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(id);
    }
  }
  
  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  // scopes ///////////////////////////////////////////////////////////////////

  public boolean isScope() {
    return isScope;
  }

  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public void forceUpdate() {
    this.forcedUpdate = true;
  }
  
 // VariableScopeImpl methods //////////////////////////////////////////////////////////////////

  
  // TODO: this should ideally move to another place  
  @Override
  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    if (processInstanceId != null) {
      variableInstance.setProcessInstanceId(processInstanceId);
    } else {
      variableInstance.setProcessInstanceId(id);
    }
    variableInstance.setExecutionId(id);
  }

  @Override
  protected Collection<VariableInstanceEntity> loadVariableInstances() {
    return Context.getCommandContext().getVariableInstanceEntityManager().findVariableInstancesByExecutionId(id);
  }

  @Override
  protected VariableScopeImpl getParentVariableScope() {
    return getParent();
  }

  /**
   * used to calculate the sourceActivityExecution for method {@link #updateActivityInstanceIdInHistoricVariableUpdate(HistoricDetailVariableInstanceUpdateEntity, ExecutionEntity)}
   */
  protected ExecutionEntity getSourceActivityExecution() {
    return this;
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

  @Override
  protected VariableInstanceEntity getSpecificVariable(String variableName) {

    CommandContext commandContext = Context.getCommandContext();
    if (commandContext == null) {
      throw new ActivitiException("lazy loading outside command context");
    }
    VariableInstanceEntity variableInstance = commandContext.getVariableInstanceEntityManager().findVariableInstanceByExecutionAndName(id, variableName);

    return variableInstance;
  }

  @Override
  protected List<VariableInstanceEntity> getSpecificVariables(Collection<String> variableNames) {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext == null) {
      throw new ActivitiException("lazy loading outside command context");
    }
    return commandContext.getVariableInstanceEntityManager().findVariableInstancesByExecutionAndNames(id, variableNames);
  }

  // event subscription support //////////////////////////////////////////////

  public List<EventSubscriptionEntity> getEventSubscriptions() {
    ensureEventSubscriptionsInitialized();
    return eventSubscriptions;
  }

  protected void ensureEventSubscriptionsInitialized() {
    if (eventSubscriptions == null) {
      eventSubscriptions = Context.getCommandContext().getEventSubscriptionEntityManager().findEventSubscriptionsByExecution(id);
    }
  }

  // referenced job entities //////////////////////////////////////////////////

  public List<JobEntity> getJobs() {
    ensureJobsInitialized();
    return jobs;
  }
  
  protected void ensureJobsInitialized() {
    if (jobs == null) {
      jobs = Context.getCommandContext().getJobEntityManager().findJobsByExecutionId(id);
    }
  }

  // referenced task entities ///////////////////////////////////////////////////

  protected void ensureTasksInitialized() {
    if (tasks == null) {
      tasks = Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(id);
    }
  }

  protected List<TaskEntity> getTasks() {
    ensureTasksInitialized();
    return tasks;
  }

  // identity links ///////////////////////////////////////////////////////////

  public List<IdentityLinkEntity> getIdentityLinks() {
    ensureIdentityLinksInitialized();
    return identityLinks;
  }

  protected void ensureIdentityLinksInitialized() {
    if (identityLinks == null) {
      identityLinks = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinksByProcessInstanceId(id);
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
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

  public String getActivityId() {
    return activityId;
  }

  public boolean isConcurrent() {
    return isConcurrent;
  }

  public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
  
  public void inactivate() {
    this.isActive = false;
  }

  public boolean isEnded() {
    return isEnded;
  }
  
  public void setEnded(boolean isEnded) {
    this.isEnded = isEnded;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
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

  public boolean isEventScope() {
    return isEventScope;
  }

  public void setEventScope(boolean isEventScope) {
    this.isEventScope = isEventScope;
  }

  public String getCurrentActivityId() {
    return activityId;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Date getLockTime() {
    return lockTime;
  }

  public void setLockTime(Date lockTime) {
    this.lockTime = lockTime;
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

  public List<VariableInstanceEntity> getQueryVariables() {
    if (queryVariables == null && Context.getCommandContext() != null) {
      queryVariables = new VariableInitializingList();
    }
    return queryVariables;
  }

  public void setQueryVariables(List<VariableInstanceEntity> queryVariables) {
    this.queryVariables = queryVariables;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
  
  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isProcessInstanceType()) {
      return "ProcessInstance[" + getId() + "]";
    } else {
      return (isScope ? "Scope" : "") + "Execution[ id '" + getId() + "'" 
          + (activityId != null ? " - activity '" + activityId + "'" : "") 
          + (parentId != null ? " - parent '" + parentId + "'" : "") + "] ";
    }
  }

}
