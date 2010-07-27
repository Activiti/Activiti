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

package org.activiti.pvm.impl.runtime;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.pvm.activity.ActivityBehaviour;
import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.activity.SignallableActivityBehaviour;
import org.activiti.pvm.event.Event;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.event.EventContext;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ProcessElementImpl;
import org.activiti.pvm.impl.process.TransitionImpl;
import org.activiti.pvm.process.PvmActivity;
import org.activiti.pvm.process.PvmTransition;


/**
 * @author Tom Baeyens
 */
public class ExecutionContextImpl implements EventContext, ActivityContext {

  private static Logger log = Logger.getLogger(ExecutionContextImpl.class.getName());

  private static final AtomicOperation EVENTLISTENER_INVOKE = new EventListenerInvoke();
  private static final AtomicOperation PROCESS_START = new ProcessStart();
  private static final AtomicOperation ACTIVITY_START = new ActivityStart();
  private static final AtomicOperation TRANSITION_ACTIVITY_END = new TransitionActivityEnd();
  private static final AtomicOperation TRANSITION_TAKE = new TransitionTake();
  private static final AtomicOperation TRANSITION_ACTIVITY_START = new TransitionActivityStart();
  private static final AtomicOperation ACTIVITY_SIGNAL = new ActivitySignal();
  
  protected ProcessInstanceImpl processInstance;
  protected ActivityInstanceImpl activityInstance;
  protected ScopeInstanceImpl scopeInstance;
  
  protected String eventName;
  protected List<EventListener> eventListeners;
  protected int eventListenerIndex;
  protected AtomicOperation eventPostOperation;
  
  protected String signalName;
  protected Object data;
  
  protected TransitionImpl transition;
  
  protected AtomicOperation nextAtomicOperation = null;
  protected boolean isOperating;

  public void startProcessInstance(ProcessInstanceImpl processInstance) {
    this.processInstance = processInstance;
    this.scopeInstance = processInstance;
    fireEvent(processInstance.getProcessDefinition(), Event.PROCESS_START, PROCESS_START);
  }

  public void signal(ActivityInstanceImpl activityInstance, String signalName, Object data) {
    this.activityInstance = activityInstance;
    this.scopeInstance = activityInstance;
    this.signalName = signalName;
    this.data = data;
    
    perform(ACTIVITY_SIGNAL);
  }

  public void take(PvmTransition transition) {
    this.transition = (TransitionImpl) transition;
    fireEvent(activityInstance.activity, Event.ACTIVITY_END, TRANSITION_ACTIVITY_END);
  }
  

  private void fireEvent(ProcessElementImpl eventDispatcher, String event, AtomicOperation eventPostOperation) {
    eventListeners = eventDispatcher.getEventListeners().get(event);
    if ( (eventListeners!=null)
         && (!eventListeners.isEmpty())
       ) {
      this.eventListenerIndex = 0;
      this.eventPostOperation = eventPostOperation;
      perform(EVENTLISTENER_INVOKE);
      
    } else {
      perform(eventPostOperation);
    }
  }
  
  private void perform(AtomicOperation atomicOperation) {
    this.nextAtomicOperation = atomicOperation;
    if (!isOperating) {
      isOperating = true;
      while (nextAtomicOperation!=null) {
        AtomicOperation currentOperation = this.nextAtomicOperation;
        this.nextAtomicOperation = null;
        if (log.isLoggable(Level.FINEST)) {
          log.finest("AtomicOperation: " + currentOperation.getClass().getName());
        }
        currentOperation.perform(this);
      }
      isOperating = false;
    }
  }

  private interface AtomicOperation {
    void perform(ExecutionContextImpl executionContext);
  }
  
  private static class EventListenerInvoke implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      if (executionContext.eventListenerIndex<executionContext.eventListeners.size()) {
        EventListener eventListener = executionContext.eventListeners.get(executionContext.eventListenerIndex);
        eventListener.event(executionContext);
        executionContext.eventListenerIndex++;
        executionContext.perform(EVENTLISTENER_INVOKE);

      } else {
        executionContext.eventListenerIndex = 0;
        executionContext.eventPostOperation = null;
        AtomicOperation operation = executionContext.eventPostOperation;
        executionContext.eventPostOperation = null;
        executionContext.perform(operation);
      }
    }
  }
  
  private static class ProcessStart implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      ProcessInstanceImpl processInstance = executionContext.processInstance;
      ActivityImpl initial = processInstance.getProcessDefinition().getInitial();
      executionContext.activityInstance = processInstance.createActivityInstance(initial);
      executionContext.fireEvent(executionContext.activityInstance.getActivity(), Event.ACTIVITY_START, ACTIVITY_START);
    }
  }

  private static class ActivityStart implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      ActivityInstanceImpl activityInstance = executionContext.activityInstance;
      activityInstance.setExecutionContext(executionContext);
      ActivityBehaviour activityBehaviour = activityInstance.getActivity().getActivityBehaviour();
      activityBehaviour.start(executionContext);
    }
  }

  private static class TransitionActivityEnd implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      ActivityInstanceImpl activityInstance = executionContext.activityInstance;
      TransitionImpl transition = executionContext.transition;
      ScopeInstanceImpl parent = activityInstance.getParent();
      executionContext.scopeInstance = parent;
      parent.removeActivityInstance(activityInstance);
      executionContext.fireEvent(transition, Event.TRANSITION_TAKE, TRANSITION_TAKE);
    }
  }

  private static class TransitionTake implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      executionContext.fireEvent(executionContext.transition, Event.TRANSITION_TAKE, TRANSITION_ACTIVITY_START);
    }
  }

  private static class TransitionActivityStart implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      ActivityImpl destination = executionContext.transition.getDestination();
      executionContext.activityInstance = executionContext.scopeInstance.createActivityInstance(destination);
      executionContext.fireEvent(destination, Event.ACTIVITY_START, ACTIVITY_START);
    }
  }

  private static class ActivitySignal implements AtomicOperation {
    @Override
    public void perform(ExecutionContextImpl executionContext) {
      ActivityInstanceImpl activityInstance = executionContext.activityInstance;
      SignallableActivityBehaviour signallableActivityBehaviour = (SignallableActivityBehaviour) activityInstance.getActivity().getActivityBehaviour();
      signallableActivityBehaviour.signal(executionContext, executionContext.signalName, executionContext.data);
    }
  }

  // execution context methods ////////////////////////////////////////////////
  
  public void setVariable(String variableName, Object value) {
    scopeInstance.setVariable(variableName, value);
  }
  
  public Object getVariable(String variableName) {
    return scopeInstance.getVariable(variableName);
  }

  public Map<String, Object> getVariables() {
    return scopeInstance.getVariables();
  }
  
  // activity execution context methods ///////////////////////////////////////
  
  @SuppressWarnings("unchecked")
  public List<PvmTransition> getOutgoingTransitions() {
    return (List) activityInstance.getActivity().getOutgoingTransitions();
  }

  @SuppressWarnings("unchecked")
  public List<PvmTransition> getIncomingTransitions() {
    return (List) activityInstance.getActivity().getIncomingTransitions();
  }

  @SuppressWarnings("unchecked")
  public List<PvmActivity> getActivities() {
    return (List) activityInstance.getActivity().getActivities();
  }
}
