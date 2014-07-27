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
package org.activiti.examples.pvm;

import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Tom Baeyens
 */
public class Decision implements ActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    PvmTransition transition;
    String creditRating = (String) execution.getVariable("creditRating");
    if (creditRating.equals("AAA+")) {
      transition = execution.getActivity().findOutgoingTransition("wow");
    } else if (creditRating.equals("Aaa-")) {
      transition = execution.getActivity().findOutgoingTransition("nice");
    } else {
      transition = execution.getActivity().findOutgoingTransition("default");
    }

    execution.take(transition);
  }
}
