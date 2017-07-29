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
package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**

 */
public class EventGatewayValidator extends ProcessLevelValidator {

  @Override
  protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
    List<EventGateway> eventGateways = process.findFlowElementsOfType(EventGateway.class);
    for (EventGateway eventGateway : eventGateways) {
      for (SequenceFlow sequenceFlow : eventGateway.getOutgoingFlows()) {
        FlowElement flowElement = process.getFlowElement(sequenceFlow.getTargetRef(), true);
        if (flowElement != null && !(flowElement instanceof IntermediateCatchEvent)) {
          addError(errors, Problems.EVENT_GATEWAY_ONLY_CONNECTED_TO_INTERMEDIATE_EVENTS, process, eventGateway, "Event based gateway can only be connected to elements of type intermediateCatchEvent");
        }
      }
    }
  }

}
