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
package org.activiti.engine.impl.pvm.delegate;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.PvmTransition;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Falko Menge
 */
public interface ActivityExecution extends DelegateExecution {
  
  /* Process instance/activity/transition retrieval */

  /**
   * returns the current {@link PvmActivity} of the execution.
   */
  PvmActivity getActivity();
  
  /**
   * leaves the current activity by taking the given transition.
   */
  void take(PvmTransition transition);
  
  
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
   * @param processDefinition The {@link PvmProcessDefinition} of the subprocess.
   */
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition);
  
  /**
   * returns the parent of this execution, or null if there no parent.
   */
  ActivityExecution getParent();
  
  ActivityExecution getProcessInstance();
  
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
   * makes this execution active or inactive.
   */
  void setActive(boolean isActive);
  
  /**
   * returns whether this execution is currently active.
   */
  boolean isActive();
  
  /**
   * returns whether this execution has ended or not.
   */
  boolean isEnded();
  
  /**
   * Sets whether this execution is ended or not.
   * Note that this won't trigger any deletion or such, it simply sets the boolean.
   * Use {@link #end()} to set the boolean and execution removal methods. 
   */
  void setEnded(boolean ended);
  
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
  boolean isProcessInstanceType();

  /**
   * Inactivates this execution.
   * This is useful for example in a join: the execution
   * still exists, but it is not longer active.
   */
  void inactivate();
  
  /**
   * Returns whether this execution is a scope.
   */
  boolean isScope();
  
  /**
   * Changes whether this execution is a scope or not
   */
  void setScope(boolean isScope);

  /**
   * Retrieves all executions which are concurrent and inactive at the given activity.
   */
  List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity);
  
  /**
   * Takes the given outgoing transitions, and potentially reusing
   * the given list of executions that were previously joined.
   */
  void takeAll(List<PvmTransition> outgoingTransitions, List<ActivityExecution> joinedExecutions);

  /**
   * Executes the {@link ActivityBehavior} associated with the given activity.
   */
  void executeActivity(PvmActivity activity);

  /**
   * Called when an execution is interrupted. 
   * 
   * Performs destroy scope behavior: all child executions and sub-process instances and other related
   * resources are removed. The execution itself can continue execution. 
   */
  void destroyScope(String reason);
}
