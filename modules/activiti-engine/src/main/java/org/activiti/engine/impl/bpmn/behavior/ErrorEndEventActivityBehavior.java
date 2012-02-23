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

import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class ErrorEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  protected String errorCode;
  
  public ErrorEndEventActivityBehavior(String errorCode) {
    this.errorCode = errorCode;
  }
  
  public void execute(ActivityExecution execution) throws Exception {    
    ErrorPropagation.propagateError(errorCode, execution);    
  }

  public String getErrorCode() {
    return errorCode;
  }
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
  
}
