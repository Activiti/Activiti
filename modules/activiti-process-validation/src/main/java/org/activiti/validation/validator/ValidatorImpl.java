package org.activiti.validation.validator;

import java.util.List;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;

/**
 * @author jbarrez
 */
public abstract class ValidatorImpl implements Validator {
	
	public void addError(List<ValidationError> validationErrors, ValidationError error) {
		validationErrors.add(error);
	}
	
	protected void addError(List<ValidationError> validationErrors, String problem, BaseElement baseElement, String description) {
		addError(validationErrors, problem, null, baseElement, description);
	}
	
	protected void addError(List<ValidationError> validationErrors, String problem, Process process, BaseElement baseElement, String description) {
		addError(validationErrors, problem, process, baseElement, description, false);
	}
	
	protected void addWarning(List<ValidationError> validationErrors, String problem, Process process, BaseElement baseElement, String description) {
		addError(validationErrors, problem, process, baseElement, description, true);
	}
	
	protected void addError(List<ValidationError> validationErrors, String problem, Process process, BaseElement baseElement, String description, boolean isWarning) {
		ValidationError error = new ValidationError();
		error.setWarning(isWarning);
		
		if (process != null) {
			error.setProcessDefinitionId(process.getId());
			error.setProcessDefinitionName(process.getName());
		}
		
		if (baseElement != null) {
			error.setXmlLineNumber(baseElement.getXmlRowNumber());
			error.setXmlColumnNumber(baseElement.getXmlColumnNumber());
		}
		error.setProblem(problem);
		error.setDefaultDescription(description);
		
		if (baseElement instanceof FlowElement) {
			FlowElement flowElement = (FlowElement) baseElement;
			error.setActivityId(flowElement.getId());
			error.setActivityName(flowElement.getName());
		}
		
		addError(validationErrors, error);
	}
	
	protected void addError(List<ValidationError> validationErrors, String problem, Process process, String id, String description) {
		ValidationError error = new ValidationError();
		
		if (process != null) {
			error.setProcessDefinitionId(process.getId());
			error.setProcessDefinitionName(process.getName());
		}
		
		error.setProblem(problem);
		error.setDefaultDescription(description);
		error.setActivityId(id);
		
		addError(validationErrors, error);
	}
	
}
