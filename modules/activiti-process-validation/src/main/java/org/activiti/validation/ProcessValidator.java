package org.activiti.validation;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;

/**
 * Validates a process definition against the rules of the Activiti engine to be executable 
 * 
 * @author jbarrez
 */
public interface ProcessValidator {

	List<ValidationError> validate(BpmnModel bpmnModel);
	
}
