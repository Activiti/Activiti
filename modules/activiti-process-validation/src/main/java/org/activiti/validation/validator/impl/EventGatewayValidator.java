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
 * @author jbarrez
 */
public class EventGatewayValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<EventGateway> eventGateways = process.findFlowElementsOfType(EventGateway.class);
		for (EventGateway eventGateway : eventGateways) {
	    for (SequenceFlow sequenceFlow : eventGateway.getOutgoingFlows()) {
	      FlowElement flowElement = process.getFlowElementRecursive(sequenceFlow.getTargetRef());
	      if (flowElement != null && flowElement instanceof IntermediateCatchEvent == false) {
	      	addError(errors, Problems.EVENT_GATEWAY_ONLY_CONNECTED_TO_INTERMEDIATE_EVENTS, process, eventGateway, 
	      			"Event based gateway can only be connected to elements of type intermediateCatchEvent");
	      }
	    }
		}
	}

}
