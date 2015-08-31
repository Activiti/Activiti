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

package org.activiti5.engine.test.pvm.activities;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti5.engine.impl.pvm.PvmTransition;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Tom Baeyens
 */
public class While implements ActivityBehavior {
  
  String variableName;
  int from;
  int to;
  
  public While(String variableName, int from, int to) {
    this.variableName = variableName;
    this.from = from;
    this.to = to;
  }

  public void execute(DelegateExecution execution) {
    ActivityExecution activityExecution = (ActivityExecution) execution;
    PvmTransition more = activityExecution.getActivity().findOutgoingTransition("more");
    PvmTransition done = activityExecution.getActivity().findOutgoingTransition("done");
    
    Integer value = (Integer) execution.getVariable(variableName);

    if (value==null) {
      execution.setVariable(variableName, from);
      activityExecution.take(more);
      
    } else {
      value = value+1;
      
      if (value<to) {
        execution.setVariable(variableName, value);
        activityExecution.take(more);
        
      } else {
        activityExecution.take(done);
      }
    }
  }

}
