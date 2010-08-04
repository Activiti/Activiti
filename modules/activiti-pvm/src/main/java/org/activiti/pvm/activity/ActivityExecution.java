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
package org.activiti.pvm.activity;

import java.util.List;

import org.activiti.pvm.process.PvmActivity;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.process.PvmTransition;
import org.activiti.pvm.runtime.PvmProcessInstance;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ActivityExecution {
  
  /* Process instance/activity/transition retrieval */

  /**
   * returns the current {@link Activity} of the execution.
   */
  PvmActivity getActivity();
  
  /**
   * leaves the current activity by taking the given transition.
   */
  void take(PvmTransition transition);
  
  
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
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition);
  
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

  void inactivate();

  List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity);

  void takeAll(List<PvmTransition> outgoingTransitions, List<ActivityExecution> joinedExecutions);

  void executeActivity(PvmActivity startActivity);
}
