package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class EventSubprocessValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<EventSubProcess> eventSubprocesses = process.findFlowElementsOfType(EventSubProcess.class);
		for (EventSubProcess eventSubprocess : eventSubprocesses) {
			
			List<StartEvent> startEvents = process.findFlowElementsInSubProcessOfType(eventSubprocess, StartEvent.class);
			for (StartEvent startEvent : startEvents) {
				if (startEvent.getEventDefinitions() != null && startEvent.getEventDefinitions().size() > 0) {
					 EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
					 if (!(eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition) 
		            && !(eventDefinition instanceof MessageEventDefinition)
		            && !(eventDefinition instanceof SignalEventDefinition)) {
						 addError(errors, Problems.EVENT_SUBPROCESS_INVALID_START_EVENT_DEFINITION, process, eventSubprocess, "start event of event subprocess must be of type 'error', 'message' or 'signal'");
					 }
				}
			}
			
		}
	}

}
