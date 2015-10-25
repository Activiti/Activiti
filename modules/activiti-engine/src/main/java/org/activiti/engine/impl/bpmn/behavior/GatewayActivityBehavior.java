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

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;

import java.util.Map;


/**
 * super class for all gateway activity implementations.
 * 
 * @author Joram Barrez
 */
public abstract class GatewayActivityBehavior extends FlowNodeActivityBehavior {
  
  protected void lockConcurrentRoot(ActivityExecution execution) {
    ActivityExecution concurrentRoot = null; 
    if (execution.isConcurrent()) {
      concurrentRoot = execution.getParent();
    } else {
      concurrentRoot = execution;
    }
    ((ExecutionEntity)concurrentRoot).forceUpdate();
  }

  protected static void logExecutionVariables(Logger log, ActivityExecution execution) {
    if (log.isTraceEnabled()) {
      Map<String, Object> variables = execution.getVariables();
      log.trace("Sequence decision based on following variables: ");
      if (!(variables == null || variables.isEmpty())) {
        for (String key : variables.keySet()) {
          Object value = variables.get(key);
          log.trace(
                   "  {} -> '{}' ({})",
                   key,
                   value,
                   value.getClass().getSimpleName()
          );
        }
      }
    }
  }

  protected static void logSequenceSelection(Logger log, PvmTransition transition, boolean selected) {
    if (log.isDebugEnabled()) {
      String reasoning = "";
      if (log.isTraceEnabled()) {
        reasoning = "since expression [" + transition.getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT) + "] evaluated to " + selected;
      }

      if (selected) {
        log.debug("Sequence flow '{}' selected as outgoing sequence flow {}.",
                 transition.getId(),
                 reasoning
        );
      } else {
        log.trace("Sequence flow '{}' not selected as outgoing sequence flow {}.",
                 transition.getId(),
                 reasoning
        );
      }
    }
  }

}
