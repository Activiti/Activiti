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

package org.activiti.examples.bpmn.executionlistener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * Simple {@link ExecutionListener} that sets 2 variables on the execution.
 * 
 * @author Frederik Heremans
 */
public class ExampleExecutionListenerTwo implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    execution.setVariable("variableSetInExecutionListener", "secondValue");
    execution.setVariable("eventNameReceived", execution.getEventName());
  }
}
