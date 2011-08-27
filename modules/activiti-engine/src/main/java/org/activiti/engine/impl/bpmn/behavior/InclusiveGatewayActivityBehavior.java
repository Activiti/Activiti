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
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implementation of the Inclusive Gateway/OR gateway/inclusive data-based
 * gateway as defined in the BPMN specification.
 * 
 * @author Tijs Rademakers
 * @author Tom Van Buskirk
 */
public class InclusiveGatewayActivityBehavior extends GatewayActivityBehavior {
  
  private static Logger log = Logger.getLogger(InclusiveGatewayActivityBehavior.class.getName());
  
  public void execute(ActivityExecution execution) throws Exception { 
    PvmActivity activity = execution.getActivity();
    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    
    execution.inactivate();
    lockConcurrentRoot(execution);
    
    boolean activeExecutionFound = execution.activeConcurrentExecutions(activity);
    
    if (activeExecutionFound == false) {
      if (log.isLoggable(Level.FINE)) {
        log.fine("inclusive gateway '"+activity.getId()+"' activates");
      }
      
      List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(activity);
      
      String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
      List<PvmTransition> transitionsToTake = new ArrayList<PvmTransition>();

      for (PvmTransition outgoingTransition : outgoingTransitions) {
        if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
          Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
          if (condition == null || condition.evaluate(execution)) {
            transitionsToTake.add(outgoingTransition);
          }
        }
      }

      if (transitionsToTake.size() == 1) {
        execution.take(transitionsToTake.get(0));

      } else if (transitionsToTake.size() >= 1) {
        execution.inactivate();

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
          // No sequence flow could be found, not even a default one
          throw new ActivitiException("No outgoing sequence flow of the inclusive gateway '" + execution.getActivity().getId()
                  + "' could be selected for continuing the process");
        }
      }
      
    } else {
      if (log.isLoggable(Level.FINE)) {
        log.fine("inclusive gateway '"+activity.getId()+"' does not activate");
      }
    }
  }

  protected void lockConcurrentRoot(ActivityExecution execution) {
    ActivityExecution concurrentRoot = null; 
    if (execution.isConcurrent()) {
      concurrentRoot = execution.getParent();
    } else {
      concurrentRoot = execution;
    }
    ((ExecutionEntity)concurrentRoot).forceUpdate();
  }
}
