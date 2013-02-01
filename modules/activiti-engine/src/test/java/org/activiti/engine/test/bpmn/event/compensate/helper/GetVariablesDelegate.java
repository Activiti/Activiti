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

package org.activiti.engine.test.bpmn.event.compensate.helper;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


/**
 * @author Daniel Meyer
 */
public class GetVariablesDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    Object nrOfCompletedInstances = execution.getVariable("nrOfCompletedInstances");    
    Integer variable = SetVariablesDelegate.variablesMap.get(nrOfCompletedInstances);
    Object variableLocal = execution.getVariable("variable");
    if(!variableLocal.equals(variable)) {
      throw new ActivitiIllegalArgumentException("wrong variable passed in to compensation handler");
    }
  }

}
