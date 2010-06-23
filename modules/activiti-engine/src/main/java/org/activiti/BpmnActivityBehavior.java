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
 * interface to be implemented by Java service delegations.
 * </p>
 * 
 * <p>
 * A java service task in BPMN 2.0 is defined as follows:
 * 
 * <pre>&lt;serviceTask javaClass=&quot;org.activiti.MyBpmnActivityBehavior&quot; ... /&gt;</pre>
 * </p>
 * 
 * <p>
 * The class referenced by the 'javaClass' attribute must implement this interface.
 * At runtime, when process execution reaches the Java service task, the engine
 * will delegate to the implementation class and execute the custom logic.
 * </p>
 * 
 * @author Joram Barrez
 */
public interface BpmnActivityBehavior {
  
  void execute(BpmnExecution bpmnExecution);

}
