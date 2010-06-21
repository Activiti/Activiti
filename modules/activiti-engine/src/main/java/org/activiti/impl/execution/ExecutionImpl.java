/* Licensed under the Apache License, ersion 2.0 (the "License");
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
package org.activiti.impl.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.calendar.BusinessCalendar;
import org.activiti.impl.calendar.BusinessCalendarManager;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.definition.TransitionImpl;
import org.activiti.impl.definition.VariableDeclarationImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.job.TimerImpl;
import org.activiti.impl.timer.TimerDeclarationImpl;
import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.ConcurrencyController;
import org.activiti.pvm.ListenerExecution;
import org.activiti.pvm.ObjectProcessInstance;
import org.activiti.pvm.Transition;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ExecutionImpl implements
        Serializable,
        ActivityExecution, 
        ListenerExecution, 
        ObjectProcessInstance, 
        ConcurrencyController {
  
  private static final long serialVersionUID = 1L;
  
  private static Logger log = Logger.getLogger(ExecutionImpl.class.getName());

  /** the process instance.  this is the root of the execution tree.  
   * the processInstance of a process instance is a self reference. */
  protected ExecutionImpl processInstance;
  
  /** the parent execution */
  protected ExecutionImpl parent;
  
  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionImpl> executions = new ArrayList<ExecutionImpl>();
  
  /** indicates if this execution represents an active path of execution.
   * Executions are made inactive in the following situations:
   * <ul>
   *   <li>an execution enters a nested scope</li>
   *   <li>an execution is split up into multiple concurrent executions, then the parent is made inactive.</li>
   *   <li>an execution has arrived in a parallel gateway or join and that join has not yet activated/fired.</li>
   * </ul>*/ 
  protected boolean isActive = true;

  /** indicates if this execution is a concurrency scope.
   * concurrent executions are also modeled as {@link #executions nested executions} and those 
   * have isConcurrencyScope=false */
  protected boolean isConcurrencyScope = true;
  
  protected VariableMap variableMap = null;
  
  protected Object cachedElContext = null;

  // transient fields /////////////////////////////////////////////////////////

  /** indicates that this execution is taking a transition */
  transient protected TransitionImpl transition = null;
  transient protected int eventListenerIndex = 0;

  /** next operation.  process execution is in fact runtime interpretation of the process model.
   * each operation is a logical unit of interpretation of the process.  so sequentially processing 
   * the operations drives the interpretation or execution of a process. 
   * @see ExeOp
   * @see #performOperation(ExeOp) */
  transient protected ExeOp nextOperation;
  transient protected boolean isOperating = false;
  
  /** non-persisted pointer to the process definition.
   * @see #processDefinitionName
   * @see #getProcessDefinition()
   * @see #setProcessDefinition(ProcessDefinitionImpl) */
  transient protected ProcessDefinitionImpl processDefinition;

  /** non-persisted pointer to the current position in the diagram.
   * @see #activity
   * @see #getActivity() 
   * @see #setActivity(ActivityImpl) */
  transient protected ActivityImpl activity;
  
  /** non-persisted indicator that this execution has ended (ie. end() has been
   * called). This is only usable in the case that a process instance reference
   * is kept, but the process instance actually is already deleted (which also
   * means it can't be fetched from the database and also that there is a need
   * to store this boolean).
   */
  transient boolean ended = true;
 
  /* Default constructor for ibatis/jpa/etc. */
  protected ExecutionImpl() {
  }
  
  /** constructor for new process instances.
   * @see ProcessDefinitionImpl#createProcessInstance() */
  public ExecutionImpl(ProcessDefinitionImpl processDefinition) {
    setProcessDefinition(processDefinition);
    setProcessInstance(this);
  }
  
  /** constructor for new child executions (both nested scopes and concurrent executions) */
  public ExecutionImpl(ExecutionImpl parent) {
    setProcessDefinition(parent.getProcessDefinition());
    setProcessInstance(parent.getProcessInstance());
    setActivity(parent.getActivity());
    setParent(parent);
    parent.addExecution(this);    
  }

  private void addExecution(ExecutionImpl child) {
    child.setParent(this);
    getExecutions(); // making sure it's initialized (@see DbExecutionImpl#getExecutions())
    executions.add(child);
  }

  // special caching getters and setters for process definition and activity //
  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  public ActivityImpl getActivity() {
    return activity;
  }
  
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  // process instance start implementation ////////////////////////////////////

  /** implements {@link ObjectProcessInstance#variable(String, Object)} */
  public ObjectProcessInstance variable(String name, Object value) {
    setVariable(name, value);
    return this;
  }

  public ObjectProcessInstance start() {
    ActivityImpl initial = getProcessDefinition().getInitial();
    setActivity(initial);
    performOperation(ExeOp.EXECUTE_CURRENT_ACTIVITY);
    return this;
  }
  
  // TODO: remove !!!!
  public void startEmbedded() {
    ActivityImpl initial = getProcessDefinition().getInitial();
    setActivity(initial);
    performOperation(ExeOp.EXECUTE_CURRENT_ACTIVITY);
  }

  // methods that translate to operations /////////////////////////////////////

  public void event(Object event) {
    performOperation(new ExeOpEvent(event));
  }
  
  public void takeDefaultOutgoingTransition() {
    TransitionImpl defaultOutgoingTransition = getActivity().getDefaultOutgoingTransition();
    take(defaultOutgoingTransition);
  }
  
  public void take(String transitionId) {
    TransitionImpl transition = activity.findOutgoingTransition(transitionId);
    if (transition==null) {
      throw new ActivitiException("coudn't find outgoing transition '"+transitionId+"' in activity '"+activity.getId()+"'");
    }
    take(transition);
  }

  public void take(Transition transition) {
    if (this.transition!=null) {
      throw new ActivitiException("already taking a transition");
    }
    setTransition((TransitionImpl) transition);
    performOperation(ExeOp.TRANSITION_NOTIFY_LISTENER_END);
  }
  
  public void executeActivity(ActivityImpl activity) {
    setActivity(activity);
    performOperation(ExeOp.EXECUTE_CURRENT_ACTIVITY);
  }

  // perform operations methods ///////////////////////////////////////////////

  protected void performOperation(ExeOp executionOperation) {
    if (executionOperation.isAsync()) {
      throw new UnsupportedOperationException("async continuations not yet supported");
    } else {
      performOperationSync(executionOperation);
    }
  }
  protected void performOperationSync(ExeOp executionOperation) {
    this.nextOperation = executionOperation;
    if (!isOperating) {
      isOperating = true;
      while (nextOperation!=null) {
        ExeOp currentOperation = this.nextOperation;
        this.nextOperation = null;
        log.fine("ExeOp: "+currentOperation+" on "+this);
        currentOperation.execute(this);
      }
      isOperating = false;
    }
  }

  public ExecutionImpl findExecution(String activityId) {
    if ( (activity!=null)
         && (activity.getId().equals(activityId))
       ) {
      return this;
    }
    for (ExecutionImpl nestedExecution : getExecutions()) {
      ExecutionImpl result = nestedExecution.findExecution(activityId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
  
  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  public TransitionImpl getDefaultOutgoingTransition() {
    if ( (activity==null)
         || (activity.getOutgoingTransitions()==null)
         || (activity.getOutgoingTransitions().isEmpty())
       ) {
      return null;
//      throw new ActivitiException("current activity "+activity+" doesn't have outgoing transitions");
    }
    return activity.getOutgoingTransitions().get(0);
  }

  public List<Transition> getIncomingTransitions() {
    if (activity != null) {
      return (List)activity.getIncomingTransitions();      
    }
    return Collections.emptyList();
  }
  public List<Transition> getOutgoingTransitions() {
    if (activity != null) {
      return (List)activity.getOutgoingTransitions();
    }
    return Collections.emptyList();
  }
  
  public ExecutionImpl getProcessInstance() {
    return processInstance;
  }
  public void setProcessInstance(ExecutionImpl processInstance) {
    this.processInstance = processInstance;
  }

  public ExecutionImpl getParent() {
    return parent;
  }
  public void setParent(ExecutionImpl parent) {
    this.parent = parent;
  }

  // executions ///////////////////////////////////////////////////////////////
  
  public List<? extends ExecutionImpl> getExecutions() {
    return executions;
  }
  
  public List<? extends ExecutionImpl> getActiveExecutions() {
    return getExecutions(false);
  }
  public List<? extends ExecutionImpl> getExecutions(boolean includeInactive) {
    if (!isConcurrencyScope) {
      throw new ActivitiException("this method should not be available on concurrent executions (==non-scope instances)");
    }
    List<ExecutionImpl> concurrentExecutions = new ArrayList<ExecutionImpl>();
    if (includeInactive || isActive) {
      concurrentExecutions.add(this);
    }
    for (ExecutionImpl execution: getExecutions()) {
      if (includeInactive || execution.isActive()) {
        concurrentExecutions.add(execution);
      }
    }
    return concurrentExecutions;
  }
  
  public ActivityExecution createExecution() {
    if (!isConcurrencyScope) {
      throw new ActivitiException("this method should not be available on concurrent executions (==non-scope instances)");
    }
    if (hasExecutions()) {
      ExecutionImpl concurrentExecution = createChildExecution();
      concurrentExecution.setConcurrencyScope(false);
      return concurrentExecution;

    } else if (isActive) {
      // clone execution for concurrency
      ExecutionImpl clonedExecution = createChildExecution();
      clonedExecution.setActivity(activity);
      clonedExecution.setTransition(transition);
      clonedExecution.setConcurrencyScope(false);
      
      // this execution turns into an inactive scope containing 
      // a nested list of concurrent executions
      setActive(false);
      setTransition(null);

      // create the new concurrent execution
      ExecutionImpl concurrentExecution = createChildExecution();
      concurrentExecution.setConcurrencyScope(false);
      
      // migrate the next operation
      if (nextOperation!=null) {
        nextOperation = new ExeOpTransferOperationLoop(clonedExecution, nextOperation);
      }
      
      return concurrentExecution;

    } else {
      // re-activate and leverage this execution
      isActive = true;
      
      return this;
    }
  }

  public ConcurrencyController getConcurrencyController() {
    if (isConcurrencyScope) {
      return this;
    }
    return getParent();
  }

  public boolean hasExecutions() {
    return !getExecutions().isEmpty();
  }

  /** allows specific process languages to instantiate 
   * process language specific execution implementations */
  protected ExecutionImpl createChildExecution() {
    return new ExecutionImpl(this);
  }

  public void end() {
    if (!isConcurrencyScope) {
      getParent().removeExecution(this);
    } else {
      isActive = false;
    }
    
    ended = true;
    
    // Create simple copy of children to avoid concurrentModificationExceptions
    // (since calling end() on the child will cause a remove in the same collection)
    List<ExecutionImpl> childExecutions = new ArrayList<ExecutionImpl>(getExecutions());
    for (ExecutionImpl childExecution : childExecutions) {
      childExecution.end();
    }
  }
  
  public ExecutionImpl createScope() {
    ExecutionImpl scopeExecution = createChildExecution();
    scopeExecution.setTransition(getTransition());
    setTransition(null);
    setActive(false);

    // create scoped variables
    for (VariableDeclarationImpl variableDeclaration: getActivity().getVariableDeclarations()) {
      scopeExecution.createVariable(variableDeclaration.getName(), variableDeclaration.getType());
    }

    // create scoped timers
    for (TimerDeclarationImpl timerDeclaration: getActivity().getTimerDeclarations()) {
      scopeExecution.createTimer(timerDeclaration);
    }

    return scopeExecution;
  }

  public void createTimer(TimerDeclarationImpl timerDeclaration) {
    BusinessCalendarManager businessCalendarManager = CommandContext
      .getCurrent()
      .getBusinessCalendarManager();
    
    BusinessCalendar businessCalendar = businessCalendarManager
      .getBusinessCalendar(timerDeclaration.getBusinessCalendarRef());
    
    Date duedate = businessCalendar.resolveDuedate(timerDeclaration.getDuedate());
    
    TimerImpl timer = new TimerImpl();
    timer.setExecution(this);
    timer.setDuedate( duedate );
    timer.setJobHandlerType( timerDeclaration.getJobHandlerType() );
    timer.setJobHandlerConfiguration( timerDeclaration.getJobHandlerConfiguration() );
    timer.setExclusive(timerDeclaration.isExclusive());
    timer.setRepeat(timerDeclaration.getRepeat());
    timer.setRetries(timerDeclaration.getRetries());
    
    CommandContext
      .getCurrent()
      .getTimerSession()
      .schedule(timer);
  }

  public void destroyScope() {
    CommandContext commandContext = CommandContext.getCurrent();
    if (commandContext!=null) {
      commandContext
        .getTimerSession()
        .cancelTimers(this);
    }

    // destroy scoped variables
    for (VariableDeclarationImpl variableDeclaration: getActivity().getVariableDeclarations()) {
      variableMap.deleteVariable(variableDeclaration.getName());
    }
    
    parent.setActivity(getActivity());
    parent.setTransition(getTransition());
    parent.setActive(true);
    parent.removeExecution(this);
    
    end();
  }


  public void removeExecution(ExecutionImpl execution) {
    getExecutions().remove(execution);
    execution.setParent(null);
  }

  public List<Activity> getActivities() {
    List<Activity> activities = new ArrayList<Activity>();
    
    if (getActivity() != null) {
      activities.add(getActivity());
    }
    
    for (ExecutionImpl childExecution : getExecutions()) {
      if (childExecution.getActivity() != null) {
        activities.add(childExecution.getActivity());
      }
    }
    return activities;
  }
  
  public List<String> getActivityNames() {
    List<Activity> activities = getActivities();
    List<String> activityNames = new ArrayList<String>();
    for (Activity activity : activities) {
      activityNames.add(activity.getId());
    }
    return activityNames;
  }

  public boolean isActive() {
    return isActive;
  }
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
  
  public boolean isEnded() {
    return ended;
  }
  
  // variables ////////////////////////////////////////////////////////////////

  public Object getVariable(String variableName) {
    if (variableMap==null) {
      initializeVariableMap();
    }
    
    // If value is found in this scope, return it
    Object value = variableMap.getVariable(variableName);
    if (value != null) {
      return value;
    }
    
    // If value not found in this scope, check the parent scope
    if (getParent() != null) {
      return getParent().getVariable(variableName);        
    }
    
    // Variable is nowhere to be found
    return null;
  }

  public Map<String, Object> getVariables() {
    if (variableMap==null) {
      initializeVariableMap();
    }
    return variableMap.getVariables();
  }
  
  public void createVariable(String name, String type) {
    if (variableMap==null) {
      initializeVariableMap();
    }
    variableMap.createVariable(name, type);
  }

  public void setVariables(Map<String, Object> variables) {
    if (variables!=null) {
      for (Map.Entry<String, Object> entry: variables.entrySet()) {
        setVariable(entry.getKey(), entry.getValue());
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    if (variableMap==null) {
      initializeVariableMap();
    }
    variableMap.setVariable(variableName, value);
  }
  
  public boolean hasVariable(String variableName) {
    if (variableMap==null) {
      initializeVariableMap();
    }
    return variableMap.hasVariable(variableName);
  }

  protected void initializeVariableMap() {
    variableMap = new VariableMap();
  }
  
  // toString /////////////////////////////////////////////////////////////////
  
  public String toString() {
    return toString(new StringBuilder()); 
  }
  
  public String toString(StringBuilder text) {
    if (isProcessInstance()) {
      text.append("ProcInst");
    } else {
      text.append("Exe");
    }
    if (activity!=null) {
      text.append("(");
      text.append(activity.getId());
      text.append(")");
    }
    if (hasExecutions()) {
      text.append("[");
      for (ExecutionImpl child: executions) {
        child.toString(text);
      }
      text.append("]");
    }
    return text.toString();
  }

  protected boolean isProcessInstance() {
    return parent==null;
  }

  public void setActivity(Activity activity) {
    this.setActivity((ActivityImpl)activity);
  }

  public String getId() {
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////

  public boolean isConcurrencyScope() {
    return isConcurrencyScope;
  }
  public void setConcurrencyScope(boolean isConcurrencyScope) {
    this.isConcurrencyScope = isConcurrencyScope;
  }
  public TransitionImpl getTransition() {
    return transition;
  }
  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
  }
  public Integer getEventListenerIndex() {
    return eventListenerIndex;
  }
  public void setEventListenerIndex(Integer eventListenerIndex) {
    this.eventListenerIndex = eventListenerIndex;
  }
  public String getProcessDefinitionId() {
    return processDefinition.getId();
  }
  public Object getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(Object cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
}
