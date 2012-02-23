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
package org.activiti.examples.bpmn.servicetask;

import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.DelegateExecution;


/**
 * @author Joram Barrez
 */
public class ToUppercase implements JavaDelegate {
  
  private static final String VARIABLE_NAME = "input";
  
  public void execute(DelegateExecution execution) {
    String var = (String) execution.getVariable(VARIABLE_NAME);
    var = var.toUpperCase();
    execution.setVariable(VARIABLE_NAME, var);
  }
  
}
