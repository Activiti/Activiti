package org.activiti.validation.validator;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.validation.ValidationError;

/**
 * @author jbarrez
 */
public interface Validator {
	
	void validate(BpmnModel bpmnModel, List<ValidationError> errors);

}
