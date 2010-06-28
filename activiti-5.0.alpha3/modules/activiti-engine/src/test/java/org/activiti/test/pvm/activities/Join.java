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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityBehavior;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.ConcurrencyController;

/**
 * @author Tom Baeyens
 */
public class Join implements ActivityBehavior {
  
  private static Logger log = Logger.getLogger(Join.class.getName());

  public void execute(ActivityExecution execution) {
    execution.setActive(false);
    
    Activity joinActivity = execution.getActivity();
    List<ActivityExecution> joinedExecutions = new ArrayList<ActivityExecution>();
    ConcurrencyController scopeInstance = execution.getConcurrencyController();
    List<? extends ActivityExecution> concurrentExecutions = scopeInstance.getExecutions();
    for (ActivityExecution concurrentExecution: concurrentExecutions) {
      if (concurrentExecution.getActivity()==joinActivity) {
        joinedExecutions.add(concurrentExecution);
      }
    }
    
    int nbrOfExecutionsToJoin = execution.getIncomingTransitions().size();
    int nbrOfExecutionsJoined = joinedExecutions.size();
    
    if (nbrOfExecutionsJoined==nbrOfExecutionsToJoin) {
      log.fine("join '"+joinActivity.getId()+"' activates: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
      activate(scopeInstance, joinActivity, joinedExecutions);
    } else if (log.isLoggable(Level.FINE)){
      log.fine("join '"+joinActivity.getId()+"' does not activate: "+nbrOfExecutionsJoined+" of "+nbrOfExecutionsToJoin+" joined");
    }
  }

  protected void activate(ConcurrencyController scopeInstance, Activity joinActivity, List<ActivityExecution> joinedExecutions) {
    for (ActivityExecution joinedExecution: joinedExecutions) {
      joinedExecution.end();
    }
    
    ActivityExecution outgoingExecution = scopeInstance.createExecution();
    outgoingExecution.setActivity(joinActivity);
    outgoingExecution.takeDefaultOutgoingTransition();
  }
}
