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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.ConcurrencyController;
import org.activiti.pvm.EventActivityBehavior;
import org.activiti.pvm.Transition;


/**
 * superclass for all 'connectable' BPMN 2.0 process elements.
 * This means that any subclass can be the source or target of a sequenceflow.
 * 
 *  Corresponds with the notion of the 'flownode' in BPMN 2.0.
 * 
 * @author Joram Barrez
 */
public abstract class FlownodeActivity implements EventActivityBehavior {
  
  private static final Logger LOG = Logger.getLogger(FlownodeActivity.class.getName());
  
  /**
   * Default behaviour: just leave the activity with no extra functionality.
   */
  public void execute(ActivityExecution execution) throws Exception {
    leave(execution);
  }

  /**
   * In BPMN 2.0, every outgoing sequence flow (which has no expression or has
   * an expression evaluating to true) is taken when leaving an activity.
   */
  protected void leave(ActivityExecution execution) {
   
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Leaving activity '" + execution.getActivity().getId() + "'");
    }
    
    List<Transition> outgoingSequenceFlow = execution.getOutgoingTransitions();
    if (outgoingSequenceFlow.size() == 1) {
      execution.take(outgoingSequenceFlow.get(0));
    } else if (outgoingSequenceFlow.size() > 1) {
      ConcurrencyController concurrencyController = execution.getConcurrencyController();
      for (Transition transition: outgoingSequenceFlow) {
        if (transition.getCondition() == null || transition.getCondition().evaluate(execution)) {
          ActivityExecution concurrentExecution = concurrencyController.createExecution();
          concurrentExecution.take(transition);
        }
      }
      execution.setActive(false);
      execution.setActivity(null);
    } else {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("No outgoing sequence flow found for " + execution.getActivity().getId() + ". Ending execution.");
      }
      execution.end();
    }
    
  }
  
  public void event(ActivityExecution execution, Object event) throws Exception {
  }

}
