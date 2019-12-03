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

import java.util.HashMap;
import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**

 */
public class BoundaryEventValidator extends ProcessLevelValidator {

  @Override
  protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
    List<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class);

    // Only one boundary event of type 'cancel' can be attached to the same
    // element, so we store the count temporarily here
    HashMap<String, Integer> cancelBoundaryEventsCounts = new HashMap<String, Integer>();

    // Only one boundary event of type 'compensate' can be attached to the
    // same element, so we store the count temporarily here
    HashMap<String, Integer> compensateBoundaryEventsCounts = new HashMap<String, Integer>();

    for (int i = 0; i < boundaryEvents.size(); i++) {

      BoundaryEvent boundaryEvent = boundaryEvents.get(i);

      if (boundaryEvent.getEventDefinitions() != null && !boundaryEvent.getEventDefinitions().isEmpty()) {

        EventDefinition eventDefinition = boundaryEvent.getEventDefinitions().get(0);
        if (!(eventDefinition instanceof TimerEventDefinition) && !(eventDefinition instanceof ErrorEventDefinition) && !(eventDefinition instanceof SignalEventDefinition)
            && !(eventDefinition instanceof CancelEventDefinition) && !(eventDefinition instanceof MessageEventDefinition) && !(eventDefinition instanceof CompensateEventDefinition)) {

          addError(errors, Problems.BOUNDARY_EVENT_INVALID_EVENT_DEFINITION, process, boundaryEvent, "Invalid or unsupported event definition");

        }

        if (eventDefinition instanceof CancelEventDefinition) {

          FlowElement attachedToFlowElement = bpmnModel.getFlowElement(boundaryEvent.getAttachedToRefId());
          if (!(attachedToFlowElement instanceof Transaction)) {
            addError(errors, Problems.BOUNDARY_EVENT_CANCEL_ONLY_ON_TRANSACTION, process, boundaryEvent, "boundary event with cancelEventDefinition only supported on transaction subprocesses");
          } else {
            if (!cancelBoundaryEventsCounts.containsKey(attachedToFlowElement.getId())) {
              cancelBoundaryEventsCounts.put(attachedToFlowElement.getId(), new Integer(0));
            }
            cancelBoundaryEventsCounts.put(attachedToFlowElement.getId(), new Integer(cancelBoundaryEventsCounts.get(attachedToFlowElement.getId()) + 1));
          }

        } else if (eventDefinition instanceof CompensateEventDefinition) {

          if (!compensateBoundaryEventsCounts.containsKey(boundaryEvent.getAttachedToRefId())) {
            compensateBoundaryEventsCounts.put(boundaryEvent.getAttachedToRefId(), new Integer(0));
          }
          compensateBoundaryEventsCounts.put(boundaryEvent.getAttachedToRefId(), compensateBoundaryEventsCounts.get(boundaryEvent.getAttachedToRefId()) + 1);

        } else if (eventDefinition instanceof MessageEventDefinition) {

          // Check if other message boundary events with same message
          // id
          for (int j = 0; j < boundaryEvents.size(); j++) {
            if (j != i) {
              BoundaryEvent otherBoundaryEvent = boundaryEvents.get(j);
              if (otherBoundaryEvent.getAttachedToRefId() != null && otherBoundaryEvent.getAttachedToRefId().equals(boundaryEvent.getAttachedToRefId())) {
                if (otherBoundaryEvent.getEventDefinitions() != null && !otherBoundaryEvent.getEventDefinitions().isEmpty()) {
                  EventDefinition otherEventDefinition = otherBoundaryEvent.getEventDefinitions().get(0);
                  if (otherEventDefinition instanceof MessageEventDefinition) {
                    MessageEventDefinition currentMessageEventDefinition = (MessageEventDefinition) eventDefinition;
                    MessageEventDefinition otherMessageEventDefinition = (MessageEventDefinition) otherEventDefinition;
                    if (otherMessageEventDefinition.getMessageRef() != null && otherMessageEventDefinition.getMessageRef().equals(currentMessageEventDefinition.getMessageRef())) {
                      addError(errors, Problems.MESSAGE_EVENT_MULTIPLE_ON_BOUNDARY_SAME_MESSAGE_ID, process, boundaryEvent, "Multiple message events with same message id not supported");
                    }
                  }
                }
              }
            }

          }

        }

      } else {

        addError(errors, Problems.BOUNDARY_EVENT_NO_EVENT_DEFINITION, process, boundaryEvent, "Event definition is missing from boundary event");

      }
    }

    for (String elementId : cancelBoundaryEventsCounts.keySet()) {
      if (cancelBoundaryEventsCounts.get(elementId) > 1) {
        addError(errors, Problems.BOUNDARY_EVENT_MULTIPLE_CANCEL_ON_TRANSACTION, process, bpmnModel.getFlowElement(elementId),
            "multiple boundary events with cancelEventDefinition not supported on same transaction subprocess.");
      }
    }

    for (String elementId : compensateBoundaryEventsCounts.keySet()) {
      if (compensateBoundaryEventsCounts.get(elementId) > 1) {
        addError(errors, Problems.COMPENSATE_EVENT_MULTIPLE_ON_BOUNDARY, process, bpmnModel.getFlowElement(elementId), "Multiple boundary events of type 'compensate' is invalid");
      }
    }

  }
}
