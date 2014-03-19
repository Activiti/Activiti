package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;

/**
 * @author jbarrez
 */
public class ProcessDefinitionValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		if (!process.isExecutable()) {
			addError(errors, Problems.PROCESS_DEFINITION_IS_NOT_EXECUTABLE,
					process, process, "Process definition should be executable (property 'isExecutable' on process)");
		}
	}

}
