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

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti5.engine.impl.pvm.PvmActivity;
import org.activiti5.engine.impl.pvm.PvmTransition;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 */
public class ParallelGateway implements ActivityBehavior {
  
  private static final long serialVersionUID = 1L;
  
  private static Logger log = LoggerFactory.getLogger(ParallelGateway.class);
  
  public void execute(DelegateExecution execution) {
    ActivityExecution activityExecution = (ActivityExecution) execution;
    PvmActivity activity = activityExecution.getActivity();

    List<PvmTransition> outgoingTransitions = activityExecution.getActivity().getOutgoingTransitions();
    
    execution.inactivate();
    
    List<ActivityExecution> joinedExecutions = activityExecution.findInactiveConcurrentExecutions(activity);
    
    int nbrOfExecutionsToJoin = activityExecution.getActivity().getIncomingTransitions().size();
    int nbrOfExecutionsJoined = joinedExecutions.size();
    
    if (nbrOfExecutionsJoined==nbrOfExecutionsToJoin) {
      log.debug("parallel gateway '{}' activates: {} of {} joined", activity.getId(), nbrOfExecutionsJoined, nbrOfExecutionsToJoin);
      activityExecution.takeAll(outgoingTransitions, joinedExecutions);
      
    } else if (log.isDebugEnabled()){
      log.debug("parallel gateway '{}' does not activate: {} of {} joined", activity.getId(), nbrOfExecutionsJoined, nbrOfExecutionsToJoin);
    }
  }
}
