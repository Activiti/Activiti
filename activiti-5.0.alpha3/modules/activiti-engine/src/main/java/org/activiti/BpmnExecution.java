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

package org.activiti;

/**
 * <p>
 * provides access to operations on the execution in BPMN 2.0 Java service
 * delegations.
 * </p>
 * 
 * <p>
 * While the process is executed, a tree of execution is built (where the root
 * of the tree is the actual process instance). When a Java service delegation
 * is reached, the delegation class has access to that specific execution that
 * reaches the Java service delegation through the operations here defined (eg.
 * manipulation of variables).
 * </p>
 * 
 * <p>
 * 
 * @see BpmnActivityBehavior#execute(BpmnExecution) </p>
 * 
 * @author Joram Barrez
 */
public interface BpmnExecution {

  Object getVariable(String variableName);
  void setVariable(String variableName, Object value);

}
