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

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Specialization of the Start Event for Event Sub-Processes.
 * 
 * @author Falko Menge
 */
public class EventSubProcessStartEventActivityBehavior extends NoneStartEventActivityBehavior {
  
  // TODO: non-interrupting [no destroyScope, setConcurrent(true)]
//  public void execute(ActivityExecution execution) throws Exception {
//    if(!interrupting) {
//      ActivityExecution executionForEventSubProcess = execution.createExecution();
//      executionForEventSubProcess.setScope(true);
//      executionForEventSubProcess.setConcurrent(true);
//      executionForEventSubProcess.setActive(true);
//      leave(executionForEventSubProcess);
//    }
//  }

}
