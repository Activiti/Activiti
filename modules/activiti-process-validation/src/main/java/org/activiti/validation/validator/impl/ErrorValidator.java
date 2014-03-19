package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;

/**
 * @author jbarrez
 */
public class ErrorValidator extends ValidatorImpl {

	@Override
	public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
		if (bpmnModel.getErrors() != null) {
			for (String errorRef : bpmnModel.getErrors().keySet()) {
				String errorCode = bpmnModel.getErrors().get(errorRef);
				if ("".equals(errorCode)) {
					addError(errors, Problems.ERROR_MISSING_ERROR_CODE, null, errorRef, "Invalid error code: empty errorCode");
				}
			}
		}
	}

}
