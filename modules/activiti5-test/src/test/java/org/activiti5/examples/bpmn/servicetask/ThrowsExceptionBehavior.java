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

package org.activiti5.examples.bpmn.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti5.engine.impl.pvm.PvmTransition;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Joram Barrez
 */
public class ThrowsExceptionBehavior implements ActivityBehavior {

  public void execute(DelegateExecution execution) {
    ActivityExecution activityExecution = (ActivityExecution) execution;
    String var = (String) execution.getVariable("var");

    PvmTransition transition;
    try {
      executeLogic(var);
      transition = activityExecution.getActivity().findOutgoingTransition("no-exception");
    } catch (Exception e) {
      transition = activityExecution.getActivity().findOutgoingTransition("exception");
    }
    activityExecution.take(transition);
  }
  
  protected void executeLogic(String value) {
    if (value.equals("throw-exception")) {
      throw new RuntimeException();
    }
  }
  
}
