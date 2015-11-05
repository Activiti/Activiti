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

import java.util.Iterator;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * implementation of the Exclusive Gateway/XOR gateway/exclusive data-based gateway
 * as defined in the BPMN specification.
 * 
 * @author Joram Barrez
 */
public class ExclusiveGatewayActivityBehavior extends GatewayActivityBehavior {
  
  private static final long serialVersionUID = 1L;
  
  private static Logger log = LoggerFactory.getLogger(ExclusiveGatewayActivityBehavior.class);
  
  /**
   * The default behaviour of BPMN, taking every outgoing sequence flow
   * (where the condition evaluates to true), is not valid for an exclusive
   * gateway. 
   * 
   * Hence, this behaviour is overriden and replaced by the correct behavior:
   * selecting the first sequence flow which condition evaluates to true
   * (or which hasn't got a condition) and leaving the activity through that
   * sequence flow. 
   * 
   * If no sequence flow is selected (ie all conditions evaluate to false),
   * then the default sequence flow is taken (if defined).
   */
  @Override
  protected void leave(ActivityExecution execution) {
    
    if (log.isDebugEnabled()) {
      log.debug("Leaving activity '{}'", execution.getActivity().getId());
    }
    
    PvmTransition outgoingSeqFlow = null;
    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
    Iterator<PvmTransition> transitionIterator = execution.getActivity().getOutgoingTransitions().iterator();
    while (outgoingSeqFlow == null && transitionIterator.hasNext()) {
      PvmTransition seqFlow = transitionIterator.next();
      Expression skipExpression = seqFlow.getSkipExpression();
      
      if (!SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpression)) {
        Condition condition = (Condition) seqFlow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        if ( (condition == null && (defaultSequenceFlow == null || !defaultSequenceFlow.equals(seqFlow.getId())) ) 
                || (condition != null && condition.evaluate(seqFlow.getId(), execution)) ) {
          if (log.isDebugEnabled()) {
            log.debug("Sequence flow '{}'selected as outgoing sequence flow.", seqFlow.getId());
          }
          outgoingSeqFlow = seqFlow;
        }
      }
      else if (SkipExpressionUtil.shouldSkipFlowElement(execution, skipExpression)){
        outgoingSeqFlow = seqFlow;
      }
    }
    
    if (outgoingSeqFlow != null) {
      execution.take(outgoingSeqFlow);
    } else {
      
      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          execution.take(defaultTransition);
        } else {
          throw new ActivitiException("Default sequence flow '" + defaultSequenceFlow + "' not found");
        }
      } else {
        //No sequence flow could be found, not even a default one
        throw new ActivitiException("No outgoing sequence flow of the exclusive gateway '"
              + execution.getActivity().getId() + "' could be selected for continuing the process");
      }
    }
  }

}
