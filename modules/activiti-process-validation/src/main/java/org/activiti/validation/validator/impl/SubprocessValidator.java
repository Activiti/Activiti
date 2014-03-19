package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class SubprocessValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<SubProcess> subProcesses = process.findFlowElementsOfType(SubProcess.class);
		for (SubProcess subProcess : subProcesses) {
			
			if (! (subProcess instanceof EventSubProcess) ) {
			
				// Verify start events
				List<StartEvent> startEvents = process.findFlowElementsInSubProcessOfType(subProcess, StartEvent.class, false);
				if (startEvents.size() > 1) {
					addError(errors, Problems.SUBPROCESS_MULTIPLE_START_EVENTS, process, subProcess, "Multiple start events not supported for subprocess");
				}
				
				for (StartEvent startEvent : startEvents) {
					if (startEvent.getEventDefinitions().size() > 0) {
						addError(errors, Problems.SUBPROCESS_START_EVENT_EVENT_DEFINITION_NOT_ALLOWED, process, startEvent, "event definitions only allowed on start event if subprocess is an event subprocess");
					}
				}
				
			}
			
		}
			
	}

}
