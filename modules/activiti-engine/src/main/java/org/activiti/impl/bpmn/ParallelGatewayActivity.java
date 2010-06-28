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

package org.activiti.impl.bpmn;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.impl.interceptor.CommandContext;
import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityExecution;

/**
 * Implementation of the Parallel Gateway/AND gateway as definined in the BPMN
 * 2.0 specification.
 * 
 * The Parallel Gateway can be used for splitting a path of execution into
 * multiple paths of executions (AND-split/fork behavior), one for every
 * outgoing sequence flow.
 * 
 * The Parallel Gateway can also be used for merging or joinging paths of
 * execution (AND-join). In this case, on every incoming sequence flow an
 * execution needs to arrive, before leaving the Parallel Gateway (and
 * potentially then doing the fork behavior in case of multiple outgoing
 * sequence flow).
 * 
 * Note that a Parallel Gateway having one incoming and multiple ougoing
 * sequence flow, is the same as having multiple outgoing sequence flow on a
 * given activity. However, a parallel gateway does NOT check conditions on the
 * outgoing sequence flow.
 * 
 * @author Joram Barrez
 */
public class ParallelGatewayActivity extends GatewayActivity {
  
  private static Logger log = Logger.getLogger(ParallelGatewayActivity.class.getName());

  public void execute(ActivityExecution execution) throws Exception { 
    
    // If there is only one incoming sequence flow, we can directly execute the fork behavior
    int nbrOfExecutionsToJoin = execution.getIncomingTransitions().size();
    if (nbrOfExecutionsToJoin == 1) {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Only one incoming sequence flow found for parallel gateway " 
                + execution.getActivity().getId());
      }
      fork(execution);
    } else {
    
      Activity joinActivity = execution.getActivity();
      List<ActivityExecution> joinedExecutions = new ArrayList<ActivityExecution>();
      
      List<? extends ActivityExecution> concurrentExecutions = execution.getExecutionController().getExecutions();
      for (ActivityExecution concurrentExecution: concurrentExecutions) {
        if (concurrentExecution.getActivity().equals(joinActivity)) {
          joinedExecutions.add(concurrentExecution);
        }
      }
      
      int nbrOfExecutionsJoined = joinedExecutions.size();
      if (log.isLoggable(Level.FINE)) {
        log.fine(nbrOfExecutionsJoined + " of " + nbrOfExecutionsToJoin 
                + " joined in " + execution.getActivity().getId());
      }
          
      if (nbrOfExecutionsJoined == nbrOfExecutionsToJoin) {
        ActivityExecution outgoingExecution = join(execution, joinedExecutions);
        // After all incoming executions are joined, potentially there can be multiple
        // outgoing sequence flow, requiring fork behavior.
        fork(outgoingExecution);
      } else {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Not all executions arrived in parallel gateway '" + execution.getActivity().getId() + "'");
        }
      }
      
    }
  }
  
  protected void fork(ActivityExecution execution) {
    leaveIgnoreConditions(execution); // a parallel gateway does NOT evaluate conditions 
  }
  
  protected ActivityExecution join(ActivityExecution execution, List<ActivityExecution> joinedExecutions) {
    
    // Child executions must be ended before selecting the ougoing sequence flowm
    // since the children endings have an influence on the reusal of the parent execution
    for (ActivityExecution joinedExecution: joinedExecutions) {
      joinedExecution.getExecutionController().end();
    }

    //HACKHACKHACKHACKHACKHACKHACK
    CommandContext.getCurrent().getPersistenceSession().flush();
    // HACKHACKHACKHACKHACKHACKHACK
    
    ActivityExecution outgoingExecution = execution.getExecutionController().createExecution();
    outgoingExecution.getExecutionController().setActivity(execution.getActivity());
    
    return outgoingExecution;
  }

}
