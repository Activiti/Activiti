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



/**
 * Execution used in {@link JavaDelegate}s and {@link ExecutionListener}s.
 * 
 * @author Tom Baeyens
 */
public interface DelegateExecution extends VariableScope {

  /** Unique id of this path of execution that can be used as a handle to provide external signals back into the engine after wait states. */
  String getId();

  /** Reference to the overall process instance */
  String getProcessInstanceId();

  /** The {@link ExecutionListener#EVENTNAME_START event name} in case this execution is passed in for an {@link ExecutionListener}  */
  String getEventName();
  
  /** The business key that was associated with the process this execution is part of. */
  String getBusinessKey();
}
