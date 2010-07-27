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
package org.activiti.test.pvm.activities;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.impl.execution.ConcurrencyController;
import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityBehavior;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.Transition;

/**
 * @author Tom Baeyens
 */
public class ParallelGateway implements ActivityBehavior {
  
  private static Logger log = Logger.getLogger(ParallelGateway.class.getName());

  public void execute(ActivityExecution execution) {
    Activity activity = execution.getActivity();

    List<Transition> outgoingTransitions = execution.getOutgoingTransitions();
    
    ConcurrencyController concurrencyController = new ConcurrencyController(execution);
    concurrencyController.inactivate();
    
    List<ActivityExecution> joinedExecutions = concurrencyController.findInactiveConcurrentExecutions(activity);
    
    int nbrOfExecutionsToJoin = execution.getIncomingTransitions().size();
    int nbrOfExecutionsJoined = joinedExecutions.size();
    
    if (nbrOfExecutionsJoined==nbrOfExecutionsToJoin) {
      log.fine("parallel gateway '"+activity.getId()+"' activates: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
      concurrencyController.takeAll(outgoingTransitions, joinedExecutions);
      
    } else if (log.isLoggable(Level.FINE)){
      log.fine("parallel gateway '"+activity.getId()+"' does not activate: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
    }
  }
}
