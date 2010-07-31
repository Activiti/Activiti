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

package org.activiti.engine.impl.bpmn;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.process.PvmTransition;

/**
 * helper class for implementing BPMN 2.0 activities, offering convience methods
 * specific to BPMN 2.0.
 * 
 * This class can be used by inheritance or aggregation.
 * 
 * @author Joram Barrez
 */
public class BpmnActivityBehavior {
  
  private static Logger log = Logger.getLogger(BpmnActivityBehavior.class.getName());

  /**
   * Performs the default outgoing BPMN 2.0 behavior, which is having parallel
   * paths of executions for the outgoing sequence flow.
   * 
   * More precisely: every sequence flow that has a condition which evaluates to
   * true (or which doesn't have a condition), is selected for continuation
   * of the process instance. If multiple sequencer flow are selected, 
   * multiple, parallel paths of executions are created.
   */
  public void performDefaultOutgoingBehavior(ActivityContext activityContext) {
    performOutgoingBehavior(activityContext, true);
  }
  
  /**
   * Performs the default outgoing BPMN 2.0 behavior (@see
   * {@link #performDefaultOutgoingBehavior(ActivityExecution)}), but without
   * checking the conditions on the outgoing sequence flow.
   * 
   * This means that every outgoing sequence flow is selected for continuing the
   * process instance, regardless of having a condition or not. In case of
   * multiple outgoing sequence flow, multiple parallel paths of executions will
   * be created.
   */
  public void performIgnoreConditionsOutgoingBehavior(ActivityContext activityContext) {
    performOutgoingBehavior(activityContext, false);
  }
  
  /**
   * Actual implementation of leaving an activity.
   */
  protected void performOutgoingBehavior(ActivityContext activityContext, boolean checkConditions) {

    if (log.isLoggable(Level.FINE)) {
        log.fine("Leaving activity '" + activityContext.getActivity().getId() + "'");
      }
      
      List<PvmTransition> outgoingTransitions = activityContext.getOutgoingTransitions();   
      if (outgoingTransitions.size() == 1) {
        activityContext.take(outgoingTransitions.get(0));
        
      } else if (outgoingTransitions.size() > 1) {
// TODO review (also javadocs)
//        ConcurrencyController concurrencyController = new ConcurrencyController(activityContext);
//        concurrencyController.inactivate();
//        
//        List<ActivityExecution> joinedExecutions = new ArrayList<ActivityExecution>();
//        joinedExecutions.add(activityContext);
//        
//        List<Transition> transitionsToTake = new ArrayList<Transition>();
//        
//        for (Transition outgoingTransition: outgoingTransitions) {
//          if (outgoingTransition.getCondition() == null 
//                  || !checkConditions 
//                  || outgoingTransition.getCondition().evaluate(activityContext)) {
//            transitionsToTake.add(outgoingTransition);
//          }
//        }
//        
//        concurrencyController.takeAll(transitionsToTake, joinedExecutions);
        
      } else {
        
        if (log.isLoggable(Level.FINE)) {
          log.fine("No outgoing sequence flow found for " + activityContext.getActivity().getId() 
                  + ". Ending execution.");
        }
        activityContext.end();
        
      }
  }
  
}
