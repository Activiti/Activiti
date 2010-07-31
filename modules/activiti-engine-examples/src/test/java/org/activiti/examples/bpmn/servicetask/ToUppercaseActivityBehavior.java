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

import org.activiti.engine.impl.bpmn.BpmnActivityBehavior;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityContext;


/**
 * @author Joram Barrez
 */
public class ToUppercaseActivityBehavior extends BpmnActivityBehavior implements ActivityBehavior {
  
  private static final String VARIABLE_NAME = "input";
  
  public void start(ActivityContext activityContext) throws Exception {
    String var = (String) activityContext.getVariable(VARIABLE_NAME);
    var = var.toUpperCase();
    activityContext.setVariable(VARIABLE_NAME, var);
    
    performDefaultOutgoingBehavior(activityContext);
  }
  
}
