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
package org.activiti.pvm;

import java.util.List;

import org.activiti.ProcessDefinition;
import org.activiti.ProcessInstance;
import org.activiti.pvm.event.ProcessEvent;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ActivityExecution {
  
  /* Process instance/activity/transition retrieval */

  /**
   * returns the current {@link ProcessInstance}.
   */
  ProcessInstance getProcessInstance();
  
  /**
   * returns the current {@link Activity} of the execution.
   */
  Activity getActivity();
  
  /**
   * returns a list of all outgoing transitions of the current activity.
   */
  List<Transition> getOutgoingTransitions();
  
  /**
   * returns a list of all incoming transitions of the current activity.
   */
  List<Transition> getIncomingTransitions();
  
  /**
   * returns the default transition for the current activity.
   */
  Transition getDefaultOutgoingTransition();
  
  /**
   * returns a list of all nested activities within the current activity.
   */
  List<Activity> getActivities();
  
  
  /* Transition taking */

  /**
   * leaves the current activity by taking the default transition.
   */
  void takeDefaultOutgoingTransition();
  
  /**
   * leaves the current activity by taking the given transition.
   */
  void take(Transition transition);
  
  /**
   * leaves the current activity by taking the given transition, referenced by its unique id.
   */
  void take(String transitionId);
  
  
  /* Variables */
  
  /**
   * returns the value of the given variable.
   */
  Object getVariable(String variableName);
  
  /**
   * sets or changes the value of the given variable.
   */
  void setVariable(String variableName, Object value);
  
  
  /* Execution management */
  
  /**
   * creates a new execution. This execution will be the parent of the newly created execution.
   * properties processDefinition, processInstance and activity will be initialized.
   */
  ActivityExecution createExecution();
  
  /**
   * creates a new sub process instance.
   * The current execution will be the super execution of the created execution.
   * 
   * @param processDefinition The {@link ProcessDefinition} of the subprocess.
   */
  ObjectProcessInstance createSubProcessInstance(ProcessDefinition processDefinition);
  
  /**
   * returns the parent of this execution, or null if there no parent.
   */
  ActivityExecution getParent();
  
  /**
   * returns the list of execution of which this execution the parent of.
   */
  List<? extends ActivityExecution> getExecutions();
  
  /**
   * ends this execution.
   */
  void end();
  
  
  /* State management */
  
  /**
   * changes the current activity to the given one.
   */
  void setActivity(Activity activity);

  /**
   * makes this execution active or inactive.
   */
  void setActive(boolean isActive);
  
  /**
   * returns whether this execution is currently active.
   */
  boolean isActive();
  
  /**
   * changes the concurrent indicator on this execution.
   */
  void setConcurrent(boolean isConcurrent);
  
  /**
   * returns whether this execution is concurrent or not.
   */
  boolean isConcurrent();
  
  /**
   * returns whether this execution is a process instance or not.
   */
  boolean isProcessInstance();
  
  
  
  /* Events */

  /**
   * fires the given event to the process event bus.
   */
  void fireEvent(ProcessEvent<?> event);
  
}
