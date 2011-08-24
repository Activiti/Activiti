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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Helper class for implementing BPMN 2.0 activities, offering convenience
 * methods specific to BPMN 2.0.
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
   * true (or which doesn't have a condition), is selected for continuation of
   * the process instance. If multiple sequencer flow are selected, multiple,
   * parallel paths of executions are created.
   */
  public void performDefaultOutgoingBehavior(ActivityExecution activityExceution) {
    performOutgoingBehavior(activityExceution, true);
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
  public void performIgnoreConditionsOutgoingBehavior(ActivityExecution activityExecution) {
    performOutgoingBehavior(activityExecution, false);
  }

  /**
   * Actual implementation of leaving an activity.
   */
  protected void performOutgoingBehavior(ActivityExecution execution, boolean checkConditions) {
    performOutgoingBehavior(execution, checkConditions, false);
  }

  /**
   * Actual implementation of leaving an activity.
   * 
   * @param execution
   *          The current execution context
   * @param checkConditions
   *          Whether or not to check conditions before determining whether or
   *          not to take a transition.
   * @param failWithNoTransition
   *          If <code>true</code> Activiti will fail with an exception when no
   *          outgoing path can be found. If <code>false</code>, Activiti will
   *          simply log a FINE message and end the execution (
   *          <code>false</code> was default behavior originally).
   */
  protected void performOutgoingBehavior(ActivityExecution execution, boolean checkConditions, boolean failWithNoTransition) {

    if (log.isLoggable(Level.FINE)) {
      log.fine("Leaving activity '" + execution.getActivity().getId() + "'");
    }

    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
    List<PvmTransition> transitionsToTake = new ArrayList<PvmTransition>();

    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    for (PvmTransition outgoingTransition : outgoingTransitions) {
      if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
        Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        if (condition == null || !checkConditions || condition.evaluate(execution)) {
          transitionsToTake.add(outgoingTransition);
        }
      }
    }

    if (transitionsToTake.size() == 1) {
      execution.take(transitionsToTake.get(0));

    } else if (transitionsToTake.size() >= 1) {
      execution.inactivate();

      List<ActivityExecution> joinedExecutions = new ArrayList<ActivityExecution>();
      joinedExecutions.add(execution);

      execution.takeAll(transitionsToTake, joinedExecutions);

    } else {

      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          execution.take(defaultTransition);
        } else {
          throw new ActivitiException("Default sequence flow '" + defaultSequenceFlow + "' could not be not found");
        }
      } else {
        if (failWithNoTransition) {
          // No sequence flow could be found, not even a default one
          throw new ActivitiException("No outgoing sequence flow of the inclusive gateway '" + execution.getActivity().getId()
                  + "' could be selected for continuing the process");
        } else if (log.isLoggable(Level.FINE)) {
          log.fine("No outgoing sequence flow found for " + execution.getActivity().getId() + ". Ending execution.");
        }
        execution.end();
      }
    }
  }
}
