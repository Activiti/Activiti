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

package org.activiti.engine.impl.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.history.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.history.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.history.HistoricVariableUpdateEntity;
import org.activiti.engine.impl.history.handler.ActivityInstanceEndHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessElement;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.pvm.runtime.OutgoingExecution;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ExecutionEntity extends VariableScopeImpl implements ActivityExecution, ExecutionListenerExecution, Execution, PvmExecution, ProcessInstance, InterpretableExecution, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  private static Logger log = Logger.getLogger(ExecutionEntity.class.getName());
  
  
  // current position /////////////////////////////////////////////////////////
  
  protected ProcessDefinitionImpl processDefinition;

  /** current activity */
  protected ActivityImpl activity;
  
  /** current transition.  is null when there is no transition being taken. */
  protected TransitionImpl transition = null;

  /** the process instance.  this is the root of the execution tree.  
   * the processInstance of a process instance is a self reference. */
  protected ExecutionEntity processInstance;
  
  /** the parent execution */
  protected ExecutionEntity parent;
  
  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionEntity> executions;
  
  /** super execution, not-null if this execution is part of a subprocess */
  protected ExecutionEntity superExecution;
  
  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected ExecutionEntity subProcessInstance;
  
  // state/type of execution ////////////////////////////////////////////////// 
  
  /** indicates if this execution represents an active path of execution.
   * Executions are made inactive in the following situations:
   * <ul>
   *   <li>an execution enters a nested scope</li>
   *   <li>an execution is split up into multiple concurrent executions, then the parent is made inactive.</li>
   *   <li>an execution has arrived in a parallel gateway or join and that join has not yet activated/fired.</li>
   *   <li>an execution is ended.</li>
   * </ul>*/ 
  protected boolean isActive = true;
  protected boolean isScope = true;
  protected boolean isConcurrent = false;
  protected boolean isEnded = false;
  
  // events ///////////////////////////////////////////////////////////////////
  
  protected String eventName;
  protected PvmProcessElement eventSource;
  protected int executionListenerIndex = 0;
  
  // cascade deletion ////////////////////////////////////////////////////////
  
  protected boolean deleteRoot;
  protected String deleteReason;
  
  // replaced by //////////////////////////////////////////////////////////////
  
  /** when execution structure is pruned during a takeAll, then 
   * the original execution has to be resolved to the replaced execution.
   * @see {@link #takeAll(List, List)} {@link OutgoingExecution} */
  protected ExecutionEntity replacedBy;
  
  // atomic operations ////////////////////////////////////////////////////////

  /** next operation.  process execution is in fact runtime interpretation of the process model.
   * each operation is a logical unit of interpretation of the process.  so sequentially processing 
   * the operations drives the interpretation or execution of a process. 
   * @see AtomicOperation
   * @see #performOperation(AtomicOperation) */
  protected AtomicOperation nextOperation;
  protected boolean isOperating = false;


  

  protected String id = null;
  protected int revision = 1;

  /**
   * persisted reference to the processDefinition.
   * 
   * @see #processDefinition
   * @see #setProcessDefinition(ProcessDefinitionImpl)
   * @see #getProcessDefinition()
   */
  protected String processDefinitionId;

  /**
   * persisted reference to the current position in the diagram within the
   * {@link #processDefinition}.
   * 
   * @see #activity
   * @see #setActivity(ActivityImpl)
   * @see #getActivity()
   */
  protected String activityId;
  
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
   * @see #setParent(ExecutionEntity)
   */
  protected String parentId;
  
  /**
   * persisted reference to the super execution of this execution
   * 
   * @See {@link #getSuperExecution()}
   * @see #setSuperExecution(ExecutionEntity)
   */
  protected String superExecutionId;
  
  protected boolean forcedUpdate;

  public ExecutionEntity() {
  }
  
  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */  
  public ExecutionEntity createExecution() {
    // create the new child execution
    ExecutionEntity createdExecution = newExecution();

    // manage the bidirectional parent-child relation
    ensureExecutionsInitialized();
    executions.add(createdExecution); 
    createdExecution.setParent(this);
    
    // initialize the new execution
    createdExecution.setProcessDefinition(getProcessDefinition());
    createdExecution.setProcessInstance(getProcessInstance());
    createdExecution.setActivity(getActivity());
    
    if (log.isLoggable(Level.FINE)) {
      log.fine("Child execution "+createdExecution+" created with parent "+this);
    }
    
    return createdExecution;
  }
  
  public PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition) {
    ExecutionEntity subProcessInstance = newExecution();
    
    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);
    
    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);
    
    CommandContext commandContext = Context.getCommandContext();
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity((ExecutionEntity) subProcessInstance);
      dbSqlSession.insert(historicProcessInstance);
    }

    return subProcessInstance;
  }

  protected ExecutionEntity newExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    newExecution.executions = new ArrayList<ExecutionEntity>();

    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(newExecution);

    return newExecution;
  }

  
  // scopes ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public void initialize() {
    log.fine("initializing "+this);

    ScopeImpl scope = getScope();
    ensureParentInitialized();

    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(BpmnParse.PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations!=null) {
      for (VariableDeclaration variableDeclaration : variableDeclarations) {
        variableDeclaration.initialize(this, parent);
      }
    }
    
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        BusinessCalendar businessCalendar = Context
          .getProcessEngineConfiguration()
          .getBusinessCalendarManager()
          .getBusinessCalendar(DurationBusinessCalendar.NAME);
        Date duedate = businessCalendar.resolveDuedate(timerDeclaration.getDuedateDescription());

        TimerEntity timer = new TimerEntity(timerDeclaration);
        timer.setDuedate(duedate);
        timer.setExecution(this);

        Context
          .getCommandContext()
          .getTimerSession()
          .schedule(timer);
      }
    }
  }
  
  public void start() {
    performOperation(AtomicOperation.PROCESS_START);
  }

  public void destroy() {
    log.fine("destroying "+this);
    
    ensureParentInitialized();
    removeVariablesLocal();
    removeVariables();

    setScope(false);
  }

  public void deleteCascade(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(AtomicOperation.DELETE_CASCADE);
  }
  
  /** removes an execution. if there are nested executions, those will be ended recursively.
   * if there is a parent, this method removes the bidirectional relation 
   * between parent and this execution. */
  public void end() {
    isActive = false;
    isEnded = true;
    performOperation(AtomicOperation.ACTIVITY_END);
  }


  // methods that translate to operations /////////////////////////////////////

  public void signal(String signalName, Object signalData) {
    ensureActivityInitialized();
    SignallableActivityBehavior activityBehavior = (SignallableActivityBehavior) activity.getActivityBehavior();
    try {
      activityBehavior.signal(this, signalName, signalData);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't process signal '"+signalName+"' on activity '"+activity.getId()+"': "+e.getMessage(), e);
    }
  }
  
  public void take(PvmTransition transition) {
    if (this.transition!=null) {
      throw new PvmException("already taking a transition");
    }
    if (transition==null) {
      throw new PvmException("transition is null");
    }
    setActivity((ActivityImpl)transition.getSource());
    setTransition((TransitionImpl) transition);
    performOperation(AtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
  }
  
  public void executeActivity(PvmActivity activity) {
    setActivity((ActivityImpl) activity);
    performOperation(AtomicOperation.ACTIVITY_START);
  }

  public List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity) {
    List<ActivityExecution> inactiveConcurrentExecutionsInActivity = new ArrayList<ActivityExecution>();
    List<ActivityExecution> otherConcurrentExecutions = new ArrayList<ActivityExecution>();
    if (isConcurrent()) {
      List< ? extends ActivityExecution> concurrentExecutions = getParent().getExecutions();
      for (ActivityExecution concurrentExecution: concurrentExecutions) {
        if (concurrentExecution.getActivity()==activity) {
          if (!concurrentExecution.isActive()) {
            inactiveConcurrentExecutionsInActivity.add(concurrentExecution);
          }
        } else {
          otherConcurrentExecutions.add(concurrentExecution);
        }
      }
    } else {
      if (!isActive()) {
        inactiveConcurrentExecutionsInActivity.add(this);
      } else {
        otherConcurrentExecutions.add(this);
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("inactive concurrent executions in '"+activity+"': "+inactiveConcurrentExecutionsInActivity);
      log.fine("other concurrent executions: "+otherConcurrentExecutions);
    }
    return inactiveConcurrentExecutionsInActivity;
  }
  
  @SuppressWarnings("unchecked")
  public void takeAll(List<PvmTransition> transitions, List<ActivityExecution> recyclableExecutions) {
    transitions = new ArrayList<PvmTransition>(transitions);
    recyclableExecutions = (recyclableExecutions!=null ? new ArrayList<ActivityExecution>(recyclableExecutions) : new ArrayList<ActivityExecution>());
    
    if (recyclableExecutions.size()>1) {
      for (ActivityExecution recyclableExecution: recyclableExecutions) {
        if (((ExecutionEntity)recyclableExecution).isScope()) {
          throw new PvmException("joining scope executions is not allowed");
        }
      }
    }

    ExecutionEntity concurrentRoot = ((isConcurrent && !isScope) ? getParent() : this);
    List<ExecutionEntity> concurrentActiveExecutions = new ArrayList<ExecutionEntity>();
    List<ExecutionEntity> concurrentInActiveExecutions = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity execution: concurrentRoot.getExecutions()) {
      if (execution.isActive()) {
        concurrentActiveExecutions.add(execution);
      } else {
        concurrentInActiveExecutions.add(execution);
      }
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine("transitions to take concurrent: " + transitions);
      log.fine("active concurrent executions: " + concurrentActiveExecutions);
    }

    if ( (transitions.size()==1)
         && (concurrentActiveExecutions.isEmpty())
         && allExecutionsInSameActivity(concurrentInActiveExecutions)
       ) {

      List<ExecutionEntity> recyclableExecutionImpls = (List) recyclableExecutions;
      for (ExecutionEntity prunedExecution: recyclableExecutionImpls) {
        // End the pruned executions if necessary.
        // Some recyclable executions are inactivated (joined executions)
        // Others are already ended (end activities)
        if (!prunedExecution.isEnded()) {
          log.fine("pruning execution " + prunedExecution);
          prunedExecution.remove();
        }
      }

      log.fine("activating the concurrent root "+concurrentRoot+" as the single path of execution going forward");
      concurrentRoot.setActive(true);
      concurrentRoot.setActivity(activity);
      concurrentRoot.setConcurrent(false);
      concurrentRoot.take(transitions.get(0));

    } else {
      
      List<OutgoingExecution> outgoingExecutions = new ArrayList<OutgoingExecution>();

      recyclableExecutions.remove(concurrentRoot);
  
      log.fine("recyclable executions for reuse: " + recyclableExecutions);
      
      // first create the concurrent executions
      while (!transitions.isEmpty()) {
        PvmTransition outgoingTransition = transitions.remove(0);

        ExecutionEntity outgoingExecution = null;
        if (recyclableExecutions.isEmpty()) {
          outgoingExecution = concurrentRoot.createExecution();
          log.fine("new "+outgoingExecution+" with parent " 
                  + outgoingExecution.getParent()+" created to take transition "+outgoingTransition);
        } else {
          outgoingExecution = (ExecutionEntity) recyclableExecutions.remove(0);
          log.fine("recycled "+outgoingExecution+" to take transition "+outgoingTransition);
        }
        
        outgoingExecution.setActive(true);
        outgoingExecution.setScope(false);
        outgoingExecution.setConcurrent(true);
        outgoingExecutions.add(new OutgoingExecution(outgoingExecution, outgoingTransition, true));
      }

      // prune the executions that are not recycled 
      for (ActivityExecution prunedExecution: recyclableExecutions) {
        log.fine("pruning execution "+prunedExecution);
        prunedExecution.end();
      }

      // then launch all the concurrent executions
      for (OutgoingExecution outgoingExecution: outgoingExecutions) {
        outgoingExecution.take();
      }
    }
  }
  
  private boolean allExecutionsInSameActivity(List<ExecutionEntity> executions) {
    if (executions.size() > 1) {
      String activityId = executions.get(0).getActivityId();
      for (ExecutionEntity execution : executions) {
        if (!execution.isEnded && !execution.getActivityId().equals(activityId)) {
          return false;
        }
      }
    }
    return true;
  }
  
  public void performOperation(AtomicOperation executionOperation) {
    Context
      .getCommandContext()
      .performOperation(executionOperation, this);
  }
  
  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  public void inactivate() {
    this.isActive = false;
  }
  
  // executions ///////////////////////////////////////////////////////////////
  
  /** ensures initialization and returns the non-null executions list */
  public List<ExecutionEntity> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }

  @SuppressWarnings("unchecked")
  protected void ensureExecutionsInitialized() {
    if (executions==null) {
      this.executions = (List) Context
        .getCommandContext()
        .getRuntimeSession()
        .findChildExecutionsByParentExecutionId(id);
    }
  }

  public void setExecutions(List<ExecutionEntity> executions) {
    this.executions = executions;
  }
  
  /** searches for an execution positioned in the given activity */
  public ExecutionEntity findExecution(String activityId) {
    if ( (getActivity()!=null)
         && (getActivity().getId().equals(activityId))
       ) {
      return this;
    }
    for (ExecutionEntity nestedExecution : getExecutions()) {
      ExecutionEntity result = nestedExecution.findExecution(activityId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
  
  public List<String> findActiveActivityIds() {
    List<String> activeActivityIds = new ArrayList<String>();
    collectActiveActivityIds(activeActivityIds);
    return activeActivityIds;
  }

  protected void collectActiveActivityIds(List<String> activeActivityIds) {
    ensureActivityInitialized();
    if (isActive && activity!=null) {
      activeActivityIds.add(activity.getId());
    }
    ensureExecutionsInitialized();
    for (ExecutionEntity execution: executions) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }

  
  // bussiness key ////////////////////////////////////////////////////////////
  
  public String getBusinessKey() {
    return businessKey;
  }
  
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  // process definition ///////////////////////////////////////////////////////

  /** ensures initialization and returns the process definition. */
  public ProcessDefinitionImpl getProcessDefinition() {
    ensureProcessDefinitionInitialized();
    return processDefinition;
  }
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  /** for setting the process definition, this setter must be used as subclasses can override */  
  protected void ensureProcessDefinitionInitialized() {
    if ((processDefinition == null) && (processDefinitionId != null)) {
      ProcessDefinitionEntity deployedProcessDefinition = Context
        .getCommandContext()
        .getRepositorySession()
        .findDeployedProcessDefinitionById(processDefinitionId);
      setProcessDefinition(deployedProcessDefinition);
    }
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.processDefinitionId = processDefinition.getId();
  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public ExecutionEntity getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }
  
  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {
      processInstance =  Context
        .getCommandContext()
        .getRuntimeSession()
        .findExecutionById(processInstanceId);
    }
  }

  public void setProcessInstance(InterpretableExecution processInstance) {
    this.processInstance = (ExecutionEntity) processInstance;
    if (processInstance != null) {
      this.processInstanceId = this.processInstance.getId();
    }
  }
  
  public boolean isProcessInstance() {
    return parentId == null;
  }

  // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return activity;
  }
  
  /** must be called before the activity member field or getActivity() is called */
  protected void ensureActivityInitialized() {
    if ((activity == null) && (activityId != null)) {
      activity = getProcessDefinition().findActivity(activityId);
    }
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if (activity != null) {
      this.activityId = activity.getId();
    } else {
      this.activityId = null;
    }
  }
  
  // parent ///////////////////////////////////////////////////////////////////
  
  /** ensures initialization and returns the parent */
  public ExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = Context
        .getCommandContext()
        .getRuntimeSession()
        .findExecutionById(parentId);
    }
  }

  public void setParent(InterpretableExecution parent) {
    this.parent = (ExecutionEntity) parent;

    if (parent != null) {
      this.parentId = ((ExecutionEntity)parent).getId();
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
      this.superExecutionId = ((ExecutionEntity)superExecution).getId();
    } else {
      this.superExecutionId = null;
    }
  }
  
  protected void ensureSuperExecutionInitialized() {
    if (superExecution == null && superExecutionId != null) {
      superExecution = Context
        .getCommandContext()
        .getRuntimeSession()
        .findExecutionById(superExecutionId);
    }
  }
  
  public ExecutionEntity getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }
  
  public void setSubProcessInstance(InterpretableExecution subProcessInstance) {
    this.subProcessInstance = (ExecutionEntity) subProcessInstance;
  }

  protected void ensureSubProcessInstanceInitialized() {
    if (subProcessInstance == null) {
      subProcessInstance = Context
        .getCommandContext()
        .getRuntimeSession()
        .findSubProcessInstanceBySuperExecutionId(id);
    }
  }
  
  // scopes ///////////////////////////////////////////////////////////////////
  
  protected ScopeImpl getScope() {
    ScopeImpl scope = null;
    if (isProcessInstance()) {
      scope = getProcessDefinition();
    } else {
      scope = getActivity();
    }
    return scope;
  }
  
  public boolean isScope() {
    return isScope;
  }

  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }
  
  // customized persistence behaviour /////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public void remove() {
    ensureParentInitialized();
    if (parent!=null) {
      parent.ensureExecutionsInitialized();
      parent.executions.remove(this);
    }

    // delete all the variable instances
    ensureVariableInstancesInitialized();
    removeVariablesLocal();
    
    // TODO add cancellation of timers

    // delete all the tasks
    CommandContext commandContext = Context.getCommandContext();
    List<TaskEntity> tasks = (List) new TaskQueryImpl(commandContext)
      .executionId(id)
      .list();
    for (TaskEntity task : tasks) {
      if (replacedBy!=null) {
        task.setExecution(replacedBy);
      } else {
        task.delete(TaskEntity.DELETE_REASON_DELETED);
      }
    }

    List<Job> jobs = new JobQueryImpl(commandContext)
      .executionId(id)
      .list();
    for (Job job: jobs) {
      if (replacedBy!=null) {
        ((JobEntity)job).setExecution((ExecutionEntity) replacedBy);
      } else {
        ((JobEntity)job).delete();
      }
    }

    // then delete execution
    commandContext
      .getDbSqlSession()
      .delete(ExecutionEntity.class, id);
  }
  
  public ExecutionEntity getReplacedBy() {
    return replacedBy;
  }

  @SuppressWarnings("unchecked")
  public void setReplacedBy(InterpretableExecution replacedBy) {
    this.replacedBy = (ExecutionEntity) replacedBy;
    
    // update the cached historic activity instances that are open
    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = dbSqlSession.findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
      if ( (cachedHistoricActivityInstance.getEndTime()==null)
           && (id.equals(cachedHistoricActivityInstance.getExecutionId())) 
         ) {
        cachedHistoricActivityInstance.setExecutionId(replacedBy.getId());
      }
    }
    
    // update the persisted historic activity instances that are open
    if (Context.getProcessEngineConfiguration().getHistoryLevel()>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      List<HistoricActivityInstanceEntity> historicActivityInstances = (List) new HistoricActivityInstanceQueryImpl(commandContext)
        .executionId(id)
        .unfinished()
        .list();
      for (HistoricActivityInstanceEntity historicActivityInstance: historicActivityInstances) {
        historicActivityInstance.setExecutionId(replacedBy.getId());
      }
    }
  }

  // variables ////////////////////////////////////////////////////////////////

  @Override
  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setProcessInstanceId(processInstanceId);
    variableInstance.setExecutionId(id);
  }

  @Override
  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context.getCommandContext().getSession(RuntimeSession.class)
      .findVariableInstancesByExecutionId(id);
  }

  @Override
  protected VariableScopeImpl getParentVariableScope() {
    return getParent();
  }
  
  @Override
  protected void initializeActivityInstanceId(HistoricVariableUpdateEntity historicVariableUpdate) {
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      HistoricActivityInstanceEntity historicActivityInstance = ActivityInstanceEndHandler.findActivityInstance(this);
      if (historicActivityInstance!=null) {
        historicVariableUpdate.setActivityInstanceId(historicActivityInstance.getId());
      }
    }
  }

  // persistent state /////////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("businessKey", businessKey);
    persistentState.put("activityId", this.activityId);
    persistentState.put("isActive", this.isActive);
    persistentState.put("isConcurrent", this.isConcurrent);
    persistentState.put("isScope", this.isScope);
    persistentState.put("parentId", parentId);
    persistentState.put("superExecution", this.superExecutionId);
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }
    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }
  
  public void forceUpdate() {
    this.forcedUpdate = true;
  }

  // toString /////////////////////////////////////////////////////////////////
  
  public String toString() {
    if (isProcessInstance()) {
      return "ProcessInstance["+getToStringIdentity()+"]";
    } else {
      return (isConcurrent? "Concurrent" : "")+(isScope() ? "Scope" : "")+"Execution["+getToStringIdentity()+"]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
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
  
  public TransitionImpl getTransition() {
    return transition;
  }
  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
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
  public PvmProcessElement getEventSource() {
    return eventSource;
  }
  public void setEventSource(PvmProcessElement eventSource) {
    this.eventSource = eventSource;
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
}
