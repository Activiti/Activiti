package org.activiti.validation.validator;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;

/**
 * @author jbarrez
 */
public abstract class ProcessLevelValidator extends ValidatorImpl {
	
	@Override
	public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
		for (Process process : bpmnModel.getProcesses()) {
			executeValidation(bpmnModel, process, errors);
		}
	}
	
	protected abstract void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors);
	
}
