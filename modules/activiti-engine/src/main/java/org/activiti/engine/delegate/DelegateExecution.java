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

package org.activiti.engine.delegate;

import org.activiti.engine.EngineServices;

/**
 * Execution used in {@link JavaDelegate}s and {@link ExecutionListener}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DelegateExecution extends VariableScope {

  /**
   * Unique id of this path of execution that can be used as a handle to provide external signals back into the engine after wait states.
   */
  String getId();

  /** Reference to the overall process instance */
  String getProcessInstanceId();
  
  String getRootProcessInstanceId();

  /**
   * The {@link ExecutionListener#EVENTNAME_START event name} in case this execution is passed in for an {@link ExecutionListener}
   */
  String getEventName();

  /**
   * The business key for the process instance this execution is associated with.
   */
  String getProcessInstanceBusinessKey();

  /**
   * The process definition key for the process instance this execution is associated with.
   */
  String getProcessDefinitionId();

  /**
   * Gets the id of the parent of this execution. If null, the execution represents a process-instance.
   */
  String getParentId();

  /**
   * Gets the id of the calling execution. If not null, the execution is part of a subprocess.
   */
  String getSuperExecutionId();

  /**
   * Gets the id of the current activity.
   */
  String getCurrentActivityId();

  /**
   * Returns the tenant id, if any is set before on the process definition or process instance.
   */
  String getTenantId();

  /**
   * All Activiti services can be accessed through this interface.
   */
  EngineServices getEngineServices();

}
