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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implementation of the Parallel Gateway/AND gateway as definined in the BPMN
 * 2.0 specification.
 * 
 * The Parallel Gateway can be used for splitting a path of execution into
 * multiple paths of executions (AND-split/fork behavior), one for every
 * outgoing sequence flow.
 * 
 * The Parallel Gateway can also be used for merging or joining paths of
 * execution (AND-join). In this case, on every incoming sequence flow an
 * execution needs to arrive, before leaving the Parallel Gateway (and
 * potentially then doing the fork behavior in case of multiple outgoing
 * sequence flow).
 * 
 * Note that there is a slight difference to spec (p. 436): "The parallel
 * gateway is activated if there is at least one Token on each incoming sequence
 * flow." We only check the number of incoming tokens to the number of sequenceflow.
 * So if two tokens would arrive through the same sequence flow, our implementation
 * would activate the gateway.
 * 
 * Note that a Parallel Gateway having one incoming and multiple ougoing
 * sequence flow, is the same as having multiple outgoing sequence flow on a
 * given activity. However, a parallel gateway does NOT check conditions on the
 * outgoing sequence flow.
 * 
 * @author Joram Barrez
 * @author Tom Baeyens
 */
public class ParallelGatewayActivityBehavior extends GatewayActivityBehavior {
  
  private static Logger log = Logger.getLogger(ParallelGatewayActivityBehavior.class.getName());

  public void execute(ActivityExecution execution) throws Exception { 
    
    // Join
    PvmActivity activity = execution.getActivity();
    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    
    execution.inactivate();
    lockConcurrentRoot(execution);
    
    List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(activity);
    int nbrOfExecutionsToJoin = execution.getActivity().getIncomingTransitions().size();
    int nbrOfExecutionsJoined = joinedExecutions.size();
    
    if (nbrOfExecutionsJoined==nbrOfExecutionsToJoin) {
      
      // Fork
      log.fine("parallel gateway '"+activity.getId()+"' activates: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
      execution.takeAll(outgoingTransitions, joinedExecutions);
      
    } else if (log.isLoggable(Level.FINE)){
      log.fine("parallel gateway '"+activity.getId()+"' does not activate: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
    }
  }

}
