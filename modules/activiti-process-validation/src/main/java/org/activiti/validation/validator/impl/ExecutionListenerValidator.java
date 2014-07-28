package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class ExecutionListenerValidator extends ProcessLevelValidator {
	
	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		
		validateListeners(process, process, process.getExecutionListeners(), errors);
		
	  for (FlowElement flowElement : process.getFlowElements()) {
	  	validateListeners(process, flowElement, flowElement.getExecutionListeners(), errors);
	  }
	}
	
	protected void validateListeners(Process process, BaseElement baseElement, List<ActivitiListener> listeners, List<ValidationError> errors) {
		if (listeners != null) {
			for (ActivitiListener listener : listeners) {
				if (listener.getImplementation() == null || listener.getImplementationType() == null) {
					addError(errors, Problems.EXECUTION_LISTENER_IMPLEMENTATION_MISSING, process, baseElement, 
							"Element 'class' or 'expression' is mandatory on executionListener");
				}
			}
		}
	}

}
