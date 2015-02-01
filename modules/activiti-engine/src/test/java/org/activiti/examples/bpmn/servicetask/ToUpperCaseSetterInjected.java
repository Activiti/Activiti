
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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;


/**
 * @author Frederik Heremans
 */
public class ToUpperCaseSetterInjected implements JavaDelegate {
  
  private Expression text;
  private boolean setterInvoked = false;
  
  public void execute(DelegateExecution execution) {
    
    if(!setterInvoked) {
      throw new RuntimeException("Setter was not invoked");
    }
    execution.setVariable("setterVar", ((String)text.getValue(execution)).toUpperCase());
  }
  
  public void setText(Expression text) {
    setterInvoked = true;
    this.text = text;
  }
  
}
