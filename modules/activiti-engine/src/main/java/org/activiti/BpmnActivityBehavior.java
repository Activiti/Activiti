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

package org.activiti;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.ExecutionController;
import org.activiti.pvm.Transition;

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
  public void performDefaultOutgoingBehavior(ActivityExecution execution) {
    performOutgoingBehavior(execution, true);
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
  public void performIgnoreConditionsOutgoingBehavior(ActivityExecution execution) {
    performOutgoingBehavior(execution, false);
  }
  
  /**
   * Actual implementation of leaving an activity.
   */
  protected void performOutgoingBehavior(ActivityExecution execution, boolean checkConditions) {
      
      if (log.isLoggable(Level.FINE)) {
        log.fine("Leaving activity '" + execution.getActivity().getId() + "'");
      }
      
      List<Transition> outgoingSequenceFlow = execution.getOutgoingTransitions();   
      if (outgoingSequenceFlow.size() == 1) {
        execution.take(outgoingSequenceFlow.get(0));
      } else if (outgoingSequenceFlow.size() > 1) {
        
        execution.getExecutionController().setActive(false);
        ExecutionController concurrencyController = execution.getExecutionController();
        
        for (Transition outSeqFlow: outgoingSequenceFlow) {
        if (outSeqFlow.getCondition() == null 
                || !checkConditions 
                || outSeqFlow.getCondition().evaluate(execution)) {
          ActivityExecution concurrentExecution = concurrencyController.createExecution();
          concurrentExecution.take(outSeqFlow);
        }
      }
        
        // TODO: The following needs to be implemented somehow
        // Current execution is deactived (can be reused)
//        execution.setActive(false);
//        execution.setActivity(null);
        
//        Map<ActivityExecution, Transition> childExecutionMapping 
//            = new LinkedHashMap<ActivityExecution, Transition>(); // Linked? -> order is important
//        ConcurrencyController concurrencyController = execution.getConcurrencyController();
//        
//        for (Transition outSeqFlow: outgoingSequenceFlow) {
//          if (outSeqFlow.getCondition() == null 
//                  || !checkConditions 
//                  || outSeqFlow.getCondition().evaluate(execution)) {
//            ActivityExecution concurrentExecution = concurrencyController.createExecution();
//            childExecutionMapping.put(concurrentExecution, outSeqFlow);
//          }
//        }
//        
//        // Only after all child executions have been created, we start taking the sequence flow
//        // (If the 'take' would be done immediately, this causes some really tricky bugs)
//        for (Map.Entry<ActivityExecution, Transition> entry : childExecutionMapping.entrySet()) {
//          entry.getKey().take(entry.getValue());
//        }
        
      } else {
        
        if (log.isLoggable(Level.FINE)) {
          log.fine("No outgoing sequence flow found for " + execution.getActivity().getId() 
                  + ". Ending execution.");
        }
        execution.getExecutionController().end();
        
      }
  }
  
}
