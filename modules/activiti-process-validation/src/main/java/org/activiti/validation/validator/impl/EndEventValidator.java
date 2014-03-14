package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.Transaction;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class EndEventValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<EndEvent> endEvents = process.findFlowElementsOfType(EndEvent.class);
		for (EndEvent endEvent : endEvents) {
			if (endEvent.getEventDefinitions() != null && endEvent.getEventDefinitions().size() > 0) {
				
				EventDefinition eventDefinition = endEvent.getEventDefinitions().get(0);
				
				// Error end event
				if (eventDefinition instanceof CancelEventDefinition) {
					
					FlowElementsContainer parent = process.findParent(endEvent);
					if (!(parent instanceof Transaction)) {
						addError(errors, Problems.END_EVENT_CANCEL_ONLY_INSIDE_TRANSACTION, process, endEvent, "end event with cancelEventDefinition only supported inside transaction subprocess");
					}
					
				}
				
			}
		}
	}
	
}
