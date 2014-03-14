package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class IntermediateCatchEventValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<IntermediateCatchEvent> intermediateCatchEvents = process.findFlowElementsOfType(IntermediateCatchEvent.class);
		for (IntermediateCatchEvent intermediateCatchEvent : intermediateCatchEvents) {
			 EventDefinition eventDefinition = null;
		    if (intermediateCatchEvent.getEventDefinitions().size() > 0) {
		      eventDefinition = intermediateCatchEvent.getEventDefinitions().get(0);
		    }
		   
		    if (eventDefinition == null) {
		    	addError(errors, Problems.INTERMEDIATE_CATCH_EVENT_NO_EVENTDEFINITION, process, intermediateCatchEvent, 
		    			"No event definition for intermediate catch event ");
		    } else {
		    	if (!(eventDefinition instanceof TimerEventDefinition)
              && !(eventDefinition instanceof SignalEventDefinition)
              && !(eventDefinition instanceof MessageEventDefinition)) {
		    		addError(errors, Problems.INTERMEDIATE_CATCH_EVENT_INVALID_EVENTDEFINITION, process, intermediateCatchEvent, 
		    				"Unsupported intermediate catch event type");
		    	}
		    }
		}
	}

}
