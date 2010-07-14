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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.definition.TransitionImpl;
import org.activiti.impl.definition.VariableDeclarationImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandContextHolder;
import org.activiti.impl.job.TimerImpl;
import org.activiti.impl.timer.TimerDeclarationImpl;
import org.activiti.impl.variable.VariableTypes;
import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityExecution;
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
        ObjectProcessInstance {
  
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
  
  protected boolean isConcurrent = false;
  
  protected boolean isScope = false;
  
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
  transient boolean isEnded = false;

  private VariableTypes variableTypes;
  
  /* Default constructor for ibatis/jpa/etc. */
  protected ExecutionImpl() {
  }
  
  /** constructor for new process instances.
   * @see ProcessDefinitionImpl#createProcessInstance() */
  public ExecutionImpl(ProcessDefinitionImpl processDefinition) {
    setProcessDefinition(processDefinition);
    setProcessInstance(this);
  }
  
  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionImpl getParent() {
    ensureParentInitialized();
    return parent;
  }

  /** all updates need to go through this setter as subclasses can override this method */
  protected void setParent(ExecutionImpl parent) {
    this.parent = parent;
  }

  /** must be called before memberfield parent is used. 
   * can be used by subclasses to provide parent member field initialization. */
  protected void ensureParentInitialized() {
  }

  // executions ///////////////////////////////////////////////////////////////  

  /** ensures initialization and returns the non-null executions list */
  public List<? extends ExecutionImpl> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }
  
  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */  
  public ExecutionImpl createExecution() {
    // create the new child execution
    ExecutionImpl createdExecution = newExecution();
    
    // manage the bidirectional parent-child relation
    ensureExecutionsInitialized();
    executions.add(createdExecution); 
    createdExecution.setParent(this);
    
    // initialize the new execution
    createdExecution.setProcessDefinition(getProcessDefinition());
    createdExecution.setProcessInstance(getProcessInstance());
    createdExecution.setActivity(getActivity());
    
    return createdExecution;
  }
  
  /** removes an execution. if there are nested executions, those will be ended recursively.
   * if there is a parent, this method removes the bidirectional relation 
   * between parent and this execution. */
  public void end() {
    // first end the nested executions
    ensureExecutionsInitialized();
    // Create simple copy of children to avoid concurrentModificationExceptions
    // (since calling end() on the child will cause a remove in the same collection)
    List<ExecutionImpl> childExecutions = new ArrayList<ExecutionImpl>(executions);
    for (ExecutionImpl childExecution : childExecutions) {
      childExecution.end();
    }
    
    // if there is a parent 
    ensureParentInitialized();
    if (parent!=null) {
      // then remove the bidirectional relation
      parent.removeExecution(this);
    } else {
      isEnded = true;
    }
  }
  
  /** searches for an execution positioned in the given activity */
  public ExecutionImpl findExecution(String activityId) {
    if ( (getActivity()!=null)
         && (getActivity().getId().equals(activityId))
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

  /** instantiates a new execution.  can be overridden by subclasses */
  protected ExecutionImpl newExecution() {
    return new ExecutionImpl();
  }

  /** disconnects an execution */ 
  protected void removeExecution(ExecutionImpl execution) {
    ensureExecutionsInitialized();
    executions.remove(execution);
    execution.setParent(null);
  }

  /** must be called before memberfield executions is used. 
   * can be used by subclasses to provide executions member field initialization. */
  protected void ensureExecutionsInitialized() {
    if (executions==null) {
      executions = new ArrayList<ExecutionImpl>();
    }
  }

  // process definition ///////////////////////////////////////////////////////
  
  /** ensures initialization and returns the process definition. */
  public ProcessDefinitionImpl getProcessDefinition() {
    ensureProcessDefinitionInitialized();
    return processDefinition;
  }
  
  public String getProcessDefinitionId() {
    return getProcessDefinition().getId();
  }

  /** for setting the process definition, this setter must be used as subclasses can override */  
  protected void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.variableTypes = processDefinition.getVariableTypes();
  }

  /** must be called before memberfield processDefinition is used. 
   * can be used by subclasses to provide processDefinition member field initialization. */
  protected void ensureProcessDefinitionInitialized() {
  }
  
  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public ExecutionImpl getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }
  
  /** for setting the process instance, this setter must be used as subclasses can override */  
  protected void setProcessInstance(ExecutionImpl processInstance) {
    this.processInstance = processInstance;
  }

  /** must be called before memberfield processInstance is used. 
   * can be used by subclasses to provide processInstance member field initialization. */
  protected void ensureProcessInstanceInitialized() {
  }
  
  // activity /////////////////////////////////////////////////////////////////
  
  /** ensures initialization and returns the activity */
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return activity;
  }
  
  /** sets the current activity.  can be overridden by subclasses.  doesn't 
   * require initialization. */
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  /** must be called before the activity member field or getActivity() is called */
  protected void ensureActivityInitialized() {
  }
  
  // current activities ///////////////////////////////////////////////////////

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

  // scopes ///////////////////////////////////////////////////////////////////
  
  public ExecutionImpl createScope() {
    ExecutionImpl scopeExecution = createExecution();
    scopeExecution.setTransition(getTransition());
    setTransition(null);
    setActive(false);
    
    log.fine("create scope: parent scope "+this+" continues as scoped execution "+scopeExecution);

    // TODO: create declared scoped variables

    // create scoped timers
    for (TimerDeclarationImpl timerDeclaration: getActivity().getTimerDeclarations()) {
      scopeExecution.createTimer(timerDeclaration);
    }

    return scopeExecution;
  }

  public void destroyScope() {
    log.fine("destroy scope: scoped "+this+" continues as parent scope "+getParent());
    CommandContext commandContext = CommandContextHolder.getCurrentCommandContext();
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
  
  // timers ///////////////////////////////////////////////////////////////////

  public void createTimer(TimerDeclarationImpl timerDeclaration) {
    TimerImpl timer = new TimerImpl();
    timer.setExecution(this);
    timer.setDuedate( timerDeclaration.getDuedate() );
    timer.setJobHandlerType( timerDeclaration.getJobHandlerType() );
    timer.setJobHandlerConfiguration( timerDeclaration.getJobHandlerConfiguration() );
    timer.setExclusive(timerDeclaration.isExclusive());
    timer.setRepeat(timerDeclaration.getRepeat());
    timer.setRetries(timerDeclaration.getRetries());
    
    CommandContextHolder
      .getCurrentCommandContext()
      .getTimerSession()
      .schedule(timer);
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
        if (log.isLoggable(Level.FINEST)) {
          log.finest("ExeOp: " + currentOperation + " on " + this);
        }
        currentOperation.execute(this);
      }
      isOperating = false;
    }
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
  

  // variables ////////////////////////////////////////////////////////////////

  public Object getVariable(String variableName) {
    ensureVariableMapInitialized();
    
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
    ensureVariableMapInitialized();
    return variableMap.getVariables();
  }
  
  public void setVariables(Map<String, Object> variables) {
    ensureVariableMapInitialized();
    if (variables!=null) {
      for (Map.Entry<String, Object> entry: variables.entrySet()) {
        setVariable(entry.getKey(), entry.getValue());
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    ensureVariableMapInitialized();
    variableMap.setVariable(variableName, value);
  }
  
  public boolean hasVariable(String variableName) {
    ensureVariableMapInitialized();
    return variableMap.hasVariable(variableName);
  }

  protected void ensureVariableMapInitialized() {
    if (variableMap==null) {
      variableMap = new VariableMap();
    }
  }
  
  // toString /////////////////////////////////////////////////////////////////
  
  public String toString() {
    return toString(new StringBuilder()); 
  }
  
  public String toString(StringBuilder text) {
    if (isProcessInstance()) {
      text.append("ProcessInstance");
    } else {
      text.append("Execution");
    }
    text.append("-");
    text.append(getIdForToString());
    return text.toString();
  }

  protected String getIdForToString() {
    return Integer.toString(System.identityHashCode(this));
  }
  
  // customized getters and setters ///////////////////////////////////////////

  protected boolean isProcessInstance() {
    ensureParentInitialized();
    return parent==null;
  }
  
  // getters and setters //////////////////////////////////////////////////////

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
  public Object getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(Object cachedElContext) {
    this.cachedElContext = cachedElContext;
  }
  public VariableTypes getVariableTypes() {
    return variableTypes;
  }
  public boolean isConcurrent() {
    return isConcurrent;
  }
  public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }
  public boolean isScope() {
    return isScope;
  }
  public void setScope(boolean isScope) {
    this.isScope = isScope;
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
  public void setActivity(Activity activity) {
    this.setActivity((ActivityImpl)activity);
  }
  public String getId() {
    return null;
  }
}
