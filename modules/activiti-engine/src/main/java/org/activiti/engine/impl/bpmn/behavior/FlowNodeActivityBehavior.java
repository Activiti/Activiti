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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;


/**
 * Superclass for all 'connectable' BPMN 2.0 process elements: tasks, gateways and events.
 * This means that any subclass can be the source or target of a sequenceflow.
 * 
 * Corresponds with the notion of the 'flownode' in BPMN 2.0.
 * 
 * @author Joram Barrez
 */
public abstract class FlowNodeActivityBehavior implements SignallableActivityBehavior {
  
  protected BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();

  /**
   * Default behaviour: just leave the activity with no extra functionality.
   */
  public void execute(ActivityExecution execution) throws Exception {
    leave(execution);
  }
  
  /**
   * Default way of leaving a BPMN 2.0 activity: evaluate the conditions on the
   * outgoing sequence flow and take those that evaluate to true.
   */
  protected void leave(ActivityExecution execution) {
    bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);
  }
  
  protected void leaveIgnoreConditions(ActivityExecution activityContext) {
    bpmnActivityBehavior.performIgnoreConditionsOutgoingBehavior(activityContext);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    // concrete activity behaviours that do accept signals should override this method;
    throw new ActivitiException("this activity doesn't accept signals");
  }

  /**
   * Register fired signals to handle race conditions within current transaction scope
   */
  protected void registerFiredSignalEvent(String eventName) {
    // register fired signals to handle race conditions within current transaction scope 
    Context.getCommandContext().addAttribute(eventName, true);
  }
  
  /**
   * Check if event has already been fired in the current transaction scope
   */
  protected boolean isSignalEventAlreadyFired(String eventName) {
    return Context.getCommandContext().getAttribute(eventName) != null;
  }
  
  /**
   * Register fired signals to handle race conditions within current transaction scope
   */
  protected void registerFiredSignalEvent(ActivityExecution execution, String eventName) {
    Map<String, List<String>> signalEvents = null;
    CommandContext currentCommandContext = Context.getCommandContext();
    // register fired signals to handle race conditions within current transaction scope 
    if (currentCommandContext.getAttribute(IntermediateThrowSignalEventActivityBehavior.FIRED_SIGNAL_EVENTS) == null ){
      signalEvents = new HashMap<String, List<String>>();
    } else {
      signalEvents = (Map<String, List<String>>) currentCommandContext.getAttribute(IntermediateThrowSignalEventActivityBehavior.FIRED_SIGNAL_EVENTS);
    }
    
    List<String> signalNameList = signalEvents.get(execution.getProcessInstanceId());
    if (signalNameList == null) {
        signalNameList = new ArrayList<String>();
    }
    signalNameList.add(eventName);
    signalEvents.put(execution.getProcessInstanceId(), signalNameList);

    Context.getCommandContext().addAttribute(IntermediateThrowSignalEventActivityBehavior.FIRED_SIGNAL_EVENTS, signalEvents);
  }
  
  /**
   * Check if event has already been fired in the current transaction scope
   */
  protected boolean isSignalEventAlreadyFired(ActivityExecution execution, String eventName) {
    Map<String, List<String>> signalEvents = (Map<String, List<String>>) Context.getCommandContext().getAttribute(IntermediateThrowSignalEventActivityBehavior.FIRED_SIGNAL_EVENTS);
    if (signalEvents != null ){
      List<String> signalNameList = signalEvents.get(execution.getProcessInstanceId());
      return (signalNameList != null) && signalNameList.contains(eventName);
    }
    return false;
  }
}
