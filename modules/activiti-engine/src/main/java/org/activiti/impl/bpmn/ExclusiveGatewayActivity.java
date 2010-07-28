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

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.Condition;
import org.activiti.pvm.Transition;


/**
 * implementation of the Exclusive Gateway/XOR gateway/exclusive data=based gateway
 * as defined in the BPMN specification.
 * 
 * @author Joram Barrez
 */
public class ExclusiveGatewayActivity extends GatewayActivity {
  
  private static Logger log = Logger.getLogger(ExclusiveGatewayActivity.class.getName());
  
  /**
   * The default behaviour of BPMN, taking every outgoing sequence flow
   * (where the condition evaluates to true), is not valid for an exclusive
   * gateway. 
   * 
   * Hence, this behaviour is overriden and replaced by the correct behavior:
   * selecting the first sequence flow which condition evaluates to true
   * (or which hasn't got a condition) and leaving the activity through that
   * sequence flow. 
   */
  @Override
  protected void leave(ActivityExecution execution) {
    
    if (log.isLoggable(Level.FINE)) {
      log.fine("Leaving activity '" + execution.getActivity().getId() + "'");
    }
    
    Transition outgoingSeqFlow = null;
    Iterator<Transition> seqFlowIterator = execution.getOutgoingTransitions().iterator();
    while (outgoingSeqFlow == null && seqFlowIterator.hasNext()) {
      Transition seqFlow = seqFlowIterator.next();
      Condition condition = seqFlow.getCondition();
      if ( condition==null || condition.evaluate(execution) ) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Sequence flow '" + seqFlow.getId() + " '"
                  + "selected as outgoing sequence flow.");
        }
        outgoingSeqFlow = seqFlow;
      }
    }
    
    if (outgoingSeqFlow != null) {
      execution.take(outgoingSeqFlow);
    } else {
      //No sequence flow could be found
      throw new ActivitiException("No outgoing sequence flow of the exclusive gateway '"
            + execution.getActivity().getId() + "' could be selected for continuing the process");
    }
  }

}
