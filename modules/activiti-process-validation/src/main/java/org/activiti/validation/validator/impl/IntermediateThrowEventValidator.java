package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class IntermediateThrowEventValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<ThrowEvent> throwEvents = process.findFlowElementsOfType(ThrowEvent.class);
		for (ThrowEvent throwEvent : throwEvents) {
			 EventDefinition eventDefinition = null;
		   if (throwEvent.getEventDefinitions().size() > 0) {
		     eventDefinition = throwEvent.getEventDefinitions().get(0);
		   }
		   
			 if (eventDefinition != null &&
					 !(eventDefinition instanceof SignalEventDefinition) && 
					 !(eventDefinition instanceof CompensateEventDefinition))  {
				 addError(errors, Problems.THROW_EVENT_INVALID_EVENTDEFINITION, process, throwEvent, "Unsupported intermediate throw event type");
		   }
		}
	}

}
