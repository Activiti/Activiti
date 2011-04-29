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
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;



/**
 * @author Tom Baeyens
 */
public class WaitState implements SignallableActivityBehavior {

  public void execute(ActivityExecution execution) {
    // By default, the execution will not propagate.
    // So if no method like take(Transition) is called on execution
    // then the activity will behave as a wait state.  The execution is currently 
    // pointing to the activity.  The original call to execution.start()
    // or execution.event() will return.  Then the execution object will 
    // remain pointing to the current activity until execution.event(Object) is called.
    // That method will delegate to the method below.  
  }

  public void signal(ActivityExecution execution, String signalName, Object event) {
    PvmTransition transition = findTransition(execution, signalName);
    execution.take(transition);
  }

  protected PvmTransition findTransition(ActivityExecution execution, String signalName) {
    for (PvmTransition transition: execution.getActivity().getOutgoingTransitions()) {
      if (signalName==null) {
        if (transition.getId()==null) {
          return transition;
        }
      } else {
        if (signalName.equals(transition.getId())) {
          return transition;
        }
      }
    }
    throw new RuntimeException("no transition for signalName '"+signalName+"' in WaitState '"+execution.getActivity().getId()+"'");
  }
}
