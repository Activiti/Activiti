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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.EngineServices;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.BitMaskUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 * @author Saeid Mirzaei
 */

public class ExecutionEntity extends VariableScopeImpl implements ActivityExecution, Execution, ProcessInstance, PersistentObject,
    HasRevision {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(ExecutionEntity.class);

  // Persistent referenced entities state
  // //////////////////////////////////////
  protected static final int EVENT_SUBSCRIPTIONS_STATE_BIT = 1;
  protected static final int TASKS_STATE_BIT = 2;
  protected static final int JOBS_STATE_BIT = 3;

  // current position
  // /////////////////////////////////////////////////////////

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

  /**
   * reference to a subprocessinstance, not-null if currently subprocess is started from this execution
   */
  protected ExecutionEntity subProcessInstance;

  /** The tenant identifier (if any) */
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected String name;

  protected Date lockTime;

  // state/type of execution
  // //////////////////////////////////////////////////

  /**
   * indicates if this execution represents an active path of execution. Executions are made inactive in the following situations:
   * <ul>
   * <li>an execution enters a nested scope</li>
   * <li>an execution is split up into multiple concurrent executions, then the parent is made inactive.</li>
   * <li>an execution has arrived in a parallel gateway or join and that join has not yet activated/fired.</li>
   * <li>an execution is ended.</li>
   * </ul>
   */
  protected boolean isActive = true;
  protected boolean isScope = true;
  protected boolean isConcurrent;
  protected boolean isEnded;
  protected boolean isEventScope;

  // events
  // ///////////////////////////////////////////////////////////////////

  protected String eventName;
  protected int executionListenerIndex;

  // associated entities /////////////////////////////////////////////////////

  // (we cache associated entities here to minimize db queries)
  protected List<EventSubscriptionEntity> eventSubscriptions;
  protected List<JobEntity> jobs;
  protected List<TaskEntity> tasks;
  protected List<IdentityLinkEntity> identityLinks;
  protected int cachedEntityState;

  // cascade deletion ////////////////////////////////////////////////////////

  protected boolean deleteRoot;
  protected String deleteReason;
  
  // replaced by
  // //////////////////////////////////////////////////////////////

  /**
   * when execution structure is pruned during a takeAll, then the original execution has to be resolved to the replaced execution.
   * 
   * @see {@link #takeAll(List, List)} {@link OutgoingExecution}
   */
  protected ExecutionEntity replacedBy;

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
  
  protected boolean isDeleted; // TODO: should be in PersistentObject probably

  public ExecutionEntity() {
  }

  /**
   * creates a new execution. properties processDefinition, processInstance and activity will be initialized.
   */
  public ExecutionEntity createExecution() {
    // create the new child execution
    ExecutionEntity createdExecution = newExecution();

    // manage the bidirectional parent-child relation
    ensureExecutionsInitialized();
    executions.add(createdExecution);
    createdExecution.setParent(this);

    // initialize the new execution
    createdExecution.setProcessDefinitionId(this.getProcessDefinitionId());
    createdExecution.setProcessInstanceId(this.getProcessInstanceId() != null ? this.getProcessInstanceId() : this.getId());
    createdExecution.setRootProcessInstanceId(this.getRootProcessInstanceId());
    createdExecution.setScope(false);

    if (log.isDebugEnabled()) {
      log.debug("Child execution {} created with parent {}", createdExecution, this.getId());
    }

    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, createdExecution));
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, createdExecution));
    }

    return createdExecution;
  }

  protected ExecutionEntity newExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    newExecution.executions = new ArrayList<ExecutionEntity>();

    // Inherit tenant id (if any)
    if (getTenantId() != null) {
      newExecution.setTenantId(getTenantId());
    }

    Context.getCommandContext().getDbSqlSession().insert(newExecution);

    return newExecution;
  }

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

  // scopes
  // ///////////////////////////////////////////////////////////////////

  public void initialize() {
    log.debug("initializing {}", this);

    ensureParentInitialized();

    // initialize the lists of referenced objects (prevents db queries)
    variableInstances = new HashMap<String, VariableInstanceEntity>();
    eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    jobs = new ArrayList<JobEntity>();
    tasks = new ArrayList<TaskEntity>();

    // Cached entity-state initialized to null, all bits are zero, indicating NO entities present
    cachedEntityState = 0;
  }

  public void destroy() {
    log.debug("destroying {}", this);

    ensureParentInitialized();
    deleteVariablesInstanceForLeavingScope();

    setScope(false);
  }

  /**
   * removes an execution. if there are nested executions, those will be ended recursively. if there is a parent, this method removes the bidirectional relation between parent and this execution.
   */
  public void end() {
    isActive = false;
    isEnded = true;
  }

  public void setEnded(boolean isEnded) {
    this.isEnded = isEnded;
  }

  protected List<ExecutionEntity> getAllChildExecutions() {
    List<ExecutionEntity> childExecutions = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity childExecution : getExecutions()) {
      childExecutions.add(childExecution);
      childExecutions.addAll(childExecution.getAllChildExecutions());
    }
    return childExecutions;
  }

  protected boolean allExecutionsInSameActivity(List<ExecutionEntity> executions) {
    if (executions.size() > 1) {
      String activityId = executions.get(0).getActivityId();
      for (ExecutionEntity execution : executions) {
        String otherActivityId = execution.getActivityId();
        if (!execution.isEnded) {
          if ((activityId == null && otherActivityId != null) || (activityId != null && otherActivityId == null)
              || (activityId != null && otherActivityId != null && !otherActivityId.equals(activityId))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public void inactivate() {
    this.isActive = false;
  }

  // executions
  // ///////////////////////////////////////////////////////////////

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

  public List<String> findActiveActivityIds() {
    List<String> activeActivityIds = new ArrayList<String>();
    collectActiveActivityIds(activeActivityIds);
    return activeActivityIds;
  }

  protected void collectActiveActivityIds(List<String> activeActivityIds) {
    if (isActive && activityId != null) {
      activeActivityIds.add(activityId);
    }
    ensureExecutionsInitialized();
    for (ExecutionEntity execution : executions) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }

  // business key
  // ////////////////////////////////////////////////////////////

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  // process definition
  // ///////////////////////////////////////////////////////

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

  // process instance
  // /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public ExecutionEntity getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }

  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance = Context.getCommandContext().getExecutionEntityManager().findExecutionById(processInstanceId);
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

  // parent
  // ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = Context.getCommandContext().getExecutionEntityManager().findExecutionById(parentId);
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

  // super- and subprocess executions
  // /////////////////////////////////////////

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
      superExecution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(superExecutionId);
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

  // scopes
  // ///////////////////////////////////////////////////////////////////

  public boolean isScope() {
    return isScope;
  }

  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  // customized persistence behaviour
  // /////////////////////////////////////////

  public void remove() {
    ensureParentInitialized();
    if (parent != null) {
      parent.ensureExecutionsInitialized();
      parent.executions.remove(this);
    }

    // delete all the variable instances
    ensureVariableInstancesInitialized();
    deleteVariablesInstanceForLeavingScope();

    // delete all the tasks
    removeTasks(null);

    // remove all jobs
    removeJobs();

    // remove all event subscriptions for this scope, if the scope has event
    // subscriptions:
    removeEventSubscriptions();

    // remove event scopes:
    removeEventScopes();

    // remove identity links
    removeIdentityLinks();

    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, this));
    }

    // finally delete this execution
    Context.getCommandContext().getDbSqlSession().delete(this);
  }

  public void destroyScope(String reason) {

    if (log.isDebugEnabled()) {
      log.debug("performing destroy scope behavior for execution {}", this);
    }

    // remove all child executions and sub process instances:
    List<ExecutionEntity> executions = new ArrayList<ExecutionEntity>(getExecutions());
    for (ExecutionEntity childExecution : executions) {
      if (childExecution.getSubProcessInstance() != null) {
        childExecution.getSubProcessInstance().deleteCascade(reason);
      }
      childExecution.deleteCascade(reason);
    }

    removeTasks(reason);
    removeJobs();
    // Daniel thought this would be needed, but it seems not:
    // removeEventSubscriptions();
  }

  private void removeEventScopes() {
    List<ExecutionEntity> childExecutions = new ArrayList<ExecutionEntity>(getExecutions());
    for (ExecutionEntity childExecution : childExecutions) {
      if (childExecution.isEventScope()) {
        log.debug("removing eventScope {}", childExecution);
        childExecution.destroy();
        childExecution.remove();
      }
    }
  }

  private void removeEventSubscriptions() {
    for (EventSubscriptionEntity eventSubscription : getEventSubscriptions()) {
      eventSubscription.delete();
    }
  }

  private void removeJobs() {
    for (Job job : getJobs()) {
      ((JobEntity) job).delete();
    }
  }

  private void removeTasks(String reason) {
    if (reason == null) {
      reason = TaskEntity.DELETE_REASON_DELETED;
    }
    for (TaskEntity task : getTasks()) {
      if (replacedBy != null) {
        if (task.getExecution() == null || task.getExecution() != replacedBy) {
          // All tasks should have been moved when "replacedBy" has
          // been set. Just in case tasks where added,
          // wo do an additional check here and move it
          task.setExecution(replacedBy);
          this.replacedBy.addTask(task);
        }
      } else {
        Context.getCommandContext().getTaskEntityManager().deleteTask(task, reason, false);
      }
    }
  }

  public ExecutionEntity getReplacedBy() {
    return replacedBy;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setReplacedBy(ExecutionEntity replacedBy) {
    this.replacedBy = replacedBy;

    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();

    // update the related tasks

    List<TaskEntity> allTasks = new ArrayList<TaskEntity>();
    allTasks.addAll(getTasks());

    List<TaskEntity> cachedTasks = dbSqlSession.findInCache(TaskEntity.class);
    for (TaskEntity cachedTask : cachedTasks) {
      if (cachedTask.getExecutionId().equals(this.getId())) {
        allTasks.add(cachedTask);
      }
    }

    for (TaskEntity task : allTasks) {
      task.setExecutionId(replacedBy.getId());
      task.setExecution(this.replacedBy);

      // update the related local task variables
      List<VariableInstanceEntity> variables = (List) commandContext.getVariableInstanceEntityManager().findVariableInstancesByTaskId(task.getId());

      for (VariableInstanceEntity variable : variables) {
        variable.setExecution(this.replacedBy);
      }

      this.replacedBy.addTask(task);
    }

    // All tasks have been moved to 'replacedBy', safe to clear the list
    this.tasks.clear();

    tasks = dbSqlSession.findInCache(TaskEntity.class);
    for (TaskEntity task : tasks) {
      if (id.equals(task.getExecutionId())) {
        task.setExecutionId(replacedBy.getId());
      }
    }

    // update the related jobs
    List<JobEntity> jobs = getJobs();
    for (JobEntity job : jobs) {
      job.setExecution(replacedBy);
    }

    // update the related event subscriptions
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptions();
    for (EventSubscriptionEntity subscriptionEntity : eventSubscriptions) {
      subscriptionEntity.setExecution(replacedBy);
    }

    // update the related process variables
    List<VariableInstanceEntity> variables = (List) commandContext.getVariableInstanceEntityManager().findVariableInstancesByExecutionId(id);

    for (VariableInstanceEntity variable : variables) {
      variable.setExecutionId(replacedBy.getId());
    }
    variables = dbSqlSession.findInCache(VariableInstanceEntity.class);
    for (VariableInstanceEntity variable : variables) {
      if (id.equals(variable.getExecutionId())) {
        variable.setExecutionId(replacedBy.getId());
      }
    }

    commandContext.getHistoryManager().recordExecutionReplacedBy(this, replacedBy);
  }

  // variables
  // ////////////////////////////////////////////////////////////////

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
  protected boolean isActivityIdUsedForDetails() {
    return true;
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

  // persistent state
  // /////////////////////////////////////////////////////////

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

  public void insert() {
    Context.getCommandContext().getDbSqlSession().insert(this);
  }

  public void deleteCascade(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public void forceUpdate() {
    this.forcedUpdate = true;
  }

  // process engine convenience access
  // /////////////////////////////////////////////////////////////////

  public EngineServices getEngineServices() {
    return Context.getProcessEngineConfiguration();
  }

  // toString
  // /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isProcessInstanceType()) {
      return "ProcessInstance[" + getToStringIdentity() + "]";
    } else {
      return (isScope ? "Scope" : "") + "Execution[ id '" + getToStringIdentity() + "'"
                + (activityId != null ? " - activity '" + activityId + "'" : "") 
                + (parentId != null ? " - parent '" + parentId + "'" : "") + "] ";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

  // event subscription support //////////////////////////////////////////////

  public List<EventSubscriptionEntity> getEventSubscriptionsInternal() {
    ensureEventSubscriptionsInitialized();
    return eventSubscriptions;
  }

  public List<EventSubscriptionEntity> getEventSubscriptions() {
    return new ArrayList<EventSubscriptionEntity>(getEventSubscriptionsInternal());
  }

  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions() {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
      }
    }
    return result;
  }

  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions(String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        if (activityId.equals(eventSubscriptionEntity.getActivityId())) {
          result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
        }
      }
    }
    return result;
  }

  protected void ensureEventSubscriptionsInitialized() {
    if (eventSubscriptions == null) {
      eventSubscriptions = Context.getCommandContext().getEventSubscriptionEntityManager().findEventSubscriptionsByExecution(id);
    }
  }

  public void addEventSubscription(EventSubscriptionEntity eventSubscriptionEntity) {
    getEventSubscriptionsInternal().add(eventSubscriptionEntity);
  }

  public void removeEventSubscription(EventSubscriptionEntity eventSubscriptionEntity) {
    getEventSubscriptionsInternal().remove(eventSubscriptionEntity);
  }

  // referenced job entities
  // //////////////////////////////////////////////////

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureJobsInitialized() {
    if (jobs == null) {
      jobs = (List) Context.getCommandContext().getJobEntityManager().findJobsByExecutionId(id);
    }
  }

  protected List<JobEntity> getJobsInternal() {
    ensureJobsInitialized();
    return jobs;
  }

  public List<JobEntity> getJobs() {
    return new ArrayList<JobEntity>(getJobsInternal());
  }

  public void addJob(JobEntity jobEntity) {
    getJobsInternal().add(jobEntity);
  }

  public void removeJob(JobEntity job) {
    getJobsInternal().remove(job);
  }

  // referenced task entities
  // ///////////////////////////////////////////////////

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureTasksInitialized() {
    if (tasks == null) {
      tasks = (List) Context.getCommandContext().getTaskEntityManager().findTasksByExecutionId(id);
    }
  }

  protected List<TaskEntity> getTasksInternal() {
    ensureTasksInitialized();
    return tasks;
  }

  public List<TaskEntity> getTasks() {
    return new ArrayList<TaskEntity>(getTasksInternal());
  }

  public void addTask(TaskEntity taskEntity) {
    getTasksInternal().add(taskEntity);
  }

  public void removeTask(TaskEntity task) {
    getTasksInternal().remove(task);
  }

  // identity links
  // ///////////////////////////////////////////////////////////

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (identityLinks == null) {
      identityLinks = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinksByProcessInstanceId(id);
    }

    return identityLinks;
  }

  public IdentityLinkEntity addIdentityLink(String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessInstance(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    identityLinkEntity.insert();
    return identityLinkEntity;
  }

  /**
   * Adds an IdentityLink for this user with the specified type, but only if the user is not associated with this instance yet.
   **/
  public IdentityLinkEntity involveUser(String userId, String type) {
    for (IdentityLinkEntity identityLink : getIdentityLinks()) {
      if (identityLink.isUser() && identityLink.getUserId().equals(userId)) {
        return identityLink;
      }
    }
    return addIdentityLink(userId, null, type);
  }

  public void removeIdentityLinks() {
    Context.getCommandContext().getIdentityLinkEntityManager().deleteIdentityLinksByProcInstance(id);
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public void setCachedEntityState(int cachedEntityState) {
    this.cachedEntityState = cachedEntityState;

    // Check for flags that are down. These lists can be safely initialized
    // as empty, preventing
    // additional queries that end up in an empty list anyway
    if (jobs == null && !BitMaskUtil.isBitOn(cachedEntityState, JOBS_STATE_BIT)) {
      jobs = new ArrayList<JobEntity>();
    }
    if (tasks == null && !BitMaskUtil.isBitOn(cachedEntityState, TASKS_STATE_BIT)) {
      tasks = new ArrayList<TaskEntity>();
    }
    if (eventSubscriptions == null && !BitMaskUtil.isBitOn(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT)) {
      eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    }
  }

  public int getCachedEntityState() {
    cachedEntityState = 0;

    // Only mark a flag as false when the list is not-null and empty. If
    // null, we can't be sure there are no entries in it since
    // the list hasn't been initialized/queried yet.
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, TASKS_STATE_BIT, (tasks == null || !tasks.isEmpty()));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT, (eventSubscriptions == null || !eventSubscriptions.isEmpty()));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, JOBS_STATE_BIT, (jobs == null || !jobs.isEmpty()));

    return cachedEntityState;
  }

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

  public Integer getExecutionListenerIndex() {
    return executionListenerIndex;
  }

  public void setExecutionListenerIndex(Integer executionListenerIndex) {
    this.executionListenerIndex = executionListenerIndex;
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

  public boolean isEnded() {
    return isEnded;
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

  public boolean isDeleteRoot() {
    return deleteRoot;
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

  public String updateProcessBusinessKey(String bzKey) {
    if (isProcessInstanceType() && bzKey != null) {
      setBusinessKey(bzKey);
      Context.getCommandContext().getHistoryManager().updateProcessBusinessKeyInHistory(this);

      if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, this));
      }

      return bzKey;
    }
    return null;
  }

  public void deleteIdentityLink(String userId, String groupId, String type) {
    List<IdentityLinkEntity> identityLinks = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinkByProcessInstanceUserGroupAndType(id, userId, groupId, type);

    for (IdentityLinkEntity identityLink : identityLinks) {
      Context.getCommandContext().getIdentityLinkEntityManager().deleteIdentityLink(identityLink, true);
    }

    getIdentityLinks().removeAll(identityLinks);

  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

}
