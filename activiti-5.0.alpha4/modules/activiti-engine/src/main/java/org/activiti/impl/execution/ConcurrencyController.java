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

package org.activiti.impl.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.pvm.Activity;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.Transition;


/**
 * @author Tom Baeyens
 */
public class ConcurrencyController {

  private static Logger log = Logger.getLogger(ConcurrencyController.class.getName());
  
  ActivityExecution execution;
  Activity activity;

  public ConcurrencyController(ActivityExecution execution) {
    this.execution = execution;
    this.activity = execution.getActivity();
  }

  public void inactivate() {
    execution.setActive(false);
  }

  public List<ActivityExecution> findInactiveConcurrentExecutions(Activity activity) {
    List<ActivityExecution> inactiveConcurrentExecutionsInActivity = new ArrayList<ActivityExecution>();
    List<ActivityExecution> otherConcurrentExecutions = new ArrayList<ActivityExecution>();
    if (execution.isConcurrent()) {
      List< ? extends ActivityExecution> concurrentExecutions = execution.getParent().getExecutions();
      for (ActivityExecution concurrentExecution: concurrentExecutions) {
        if (concurrentExecution.getActivity()==activity) {
          if (concurrentExecution.isActive()) {
            throw new ActivitiException("didn't expect active execution in "+activity+". bug?");
          }
          inactiveConcurrentExecutionsInActivity.add(concurrentExecution);
        } else {
          otherConcurrentExecutions.add(concurrentExecution);
        }
      }
    } else {
      if (!execution.isActive()) {
        inactiveConcurrentExecutionsInActivity.add(execution);
      } else {
        otherConcurrentExecutions.add(execution);
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("inactive concurrent executions in '"+activity+"': "+inactiveConcurrentExecutionsInActivity);
      log.fine("other concurrent executions: "+otherConcurrentExecutions);
    }
    return inactiveConcurrentExecutionsInActivity;
  }

  public void takeAll(List<Transition> transitions, List<ActivityExecution> joinedExecutions) {
    transitions = new ArrayList<Transition>(transitions);
    joinedExecutions = new ArrayList<ActivityExecution>(joinedExecutions);
    
    ActivityExecution concurrentRoot = (execution.isConcurrent() ? execution.getParent() : execution);
    List< ? extends ActivityExecution> concurrentExecutions = concurrentRoot.getExecutions();

    if (log.isLoggable(Level.FINE)) {
      log.fine("transitions to take concurrent: " + transitions);
      log.fine("existing concurrent executions: " + concurrentExecutions);
    }

    if ( (transitions.size()==1)
         && (joinedExecutions.size()==concurrentExecutions.size())
       ) {

      for (ActivityExecution prunedExecution: joinedExecutions) {
        log.info("pruning execution "+prunedExecution);
        prunedExecution.end();
      }

      log.info("activating the concurrent root execution as the single path of execution going forward");
      concurrentRoot.setActive(true);
      concurrentRoot.setActivity(activity);
      concurrentRoot.setConcurrent(false);
      concurrentRoot.take(transitions.get(0));

    } else {
      
      List<OutgoingExecution> outgoingExecutions = new ArrayList<OutgoingExecution>();
      
      joinedExecutions.remove(concurrentRoot);
      log.fine("joined executions to be reused: " + joinedExecutions);
      
      // first create the concurrent executions
      while (!transitions.isEmpty()) {
        Transition outgoingTransition = transitions.remove(0);

        if (joinedExecutions.isEmpty()) {
          ActivityExecution outgoingExecution = concurrentRoot.createExecution();
          outgoingExecutions.add(new OutgoingExecution(outgoingExecution, outgoingTransition, true));
          log.fine("new "+outgoingExecution+" created to take transition "+outgoingTransition);
        } else {
          ActivityExecution outgoingExecution = joinedExecutions.remove(0);
          outgoingExecutions.add(new OutgoingExecution(outgoingExecution, outgoingTransition, true));
          log.fine("recycled "+outgoingExecution+" to take transition "+outgoingTransition);
        }
      }

      // prune the executions that are not recycled 
      for (ActivityExecution prunedExecution: joinedExecutions) {
        log.info("pruning execution "+prunedExecution);
        prunedExecution.end();
      }

      // then launch all the concurrent executions
      for (OutgoingExecution outgoingExecution: outgoingExecutions) {
        outgoingExecution.take();
      }
    }
  }
  
  private class OutgoingExecution {
    ActivityExecution outgoingExecution;
    Transition outgoingTransition;
    boolean isNew;

    public OutgoingExecution(ActivityExecution outgoingExecution, Transition outgoingTransition, boolean isNew) {
      this.outgoingExecution = outgoingExecution;
      this.outgoingTransition = outgoingTransition;
      this.isNew = isNew;
    }
    
    public void take() {
      outgoingExecution.setActive(true);
      outgoingExecution.setConcurrent(true);
      outgoingExecution.take(outgoingTransition);
    }
  }
}
