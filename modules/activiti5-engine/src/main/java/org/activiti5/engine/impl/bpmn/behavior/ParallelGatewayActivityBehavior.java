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

package org.activiti5.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.pvm.PvmActivity;
import org.activiti5.engine.impl.pvm.PvmTransition;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  private static final long serialVersionUID = 1L;
  
  private static Logger log = LoggerFactory.getLogger(ParallelGatewayActivityBehavior.class);

  public void execute(DelegateExecution execution) { 
    ActivityExecution activityExecution = (ActivityExecution) execution;
    // Join
    PvmActivity activity = activityExecution.getActivity();
    List<PvmTransition> outgoingTransitions = activityExecution.getActivity().getOutgoingTransitions();
    execution.inactivate();
    lockConcurrentRoot(activityExecution);
    
    List<ActivityExecution> joinedExecutions = activityExecution.findInactiveConcurrentExecutions(activity);
    int nbrOfExecutionsToJoin = activityExecution.getActivity().getIncomingTransitions().size();
    int nbrOfExecutionsJoined = joinedExecutions.size();
    Context.getCommandContext().getHistoryManager().recordActivityEnd((ExecutionEntity) execution);
    if (nbrOfExecutionsJoined==nbrOfExecutionsToJoin) {
      
      // Fork
      if(log.isDebugEnabled()) {
        log.debug("parallel gateway '{}' activates: {} of {} joined", activity.getId(), nbrOfExecutionsJoined, nbrOfExecutionsToJoin);
      }
      activityExecution.takeAll(outgoingTransitions, joinedExecutions);
      
    } else if (log.isDebugEnabled()){
      log.debug("parallel gateway '{}' does not activate: {} of {} joined", activity.getId(), nbrOfExecutionsJoined, nbrOfExecutionsToJoin);
    }
  }

}
