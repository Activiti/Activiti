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
package org.activiti.examples.pojo;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.pvm.ActivityBehavior;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.Condition;
import org.activiti.pvm.Transition;


/**
 * @author Tom Baeyens
 */
public class Decision implements ActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    Transition transition = findOutgoingTransition(execution);
    execution.take(transition);
  }

  private Transition findOutgoingTransition(ActivityExecution execution) {
    List<Transition> outgoingTransitions = execution.getOutgoingTransitions();
    for (Transition transition: outgoingTransitions) {
      Condition condition = transition.getCondition();
      if ( (condition==null)
           || (condition.evaluate(execution))
         ) {
        return transition;
      }
    }
    throw new ActivitiException("no transition for which the condition resolved to true: "+outgoingTransitions);
  }
}
