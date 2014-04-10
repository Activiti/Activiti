package org.activiti.validation;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.validation.validator.Validator;
import org.activiti.validation.validator.ValidatorSet;

/**
 * @author jbarrez
 */
public class ProcessValidatorImpl implements ProcessValidator {
	
	protected List<ValidatorSet> validatorSets;
	
	@Override
	public List<ValidationError> validate(BpmnModel bpmnModel) {
		
		List<ValidationError> allErrors = new ArrayList<ValidationError>();
		
		for (ValidatorSet validatorSet : validatorSets) {
			for (Validator validator : validatorSet.getValidators()) {
			  List<ValidationError> validatorErrors = new ArrayList<ValidationError>();
				validator.validate(bpmnModel, validatorErrors);
				if (validatorErrors.size() > 0) {
					for (ValidationError error : validatorErrors) {
						error.setValidatorSetName(validatorSet.getName());
					}
					allErrors.addAll(validatorErrors);
				}
			}
		}
		return allErrors;
	}

	public List<ValidatorSet> getValidatorSets() {
		return validatorSets;
	}

	public void setValidatorSets(List<ValidatorSet> validatorSets) {
		this.validatorSets = validatorSets;
	}

	public void addValidatorSet(ValidatorSet validatorSet) {
		if (validatorSets == null) {
			validatorSets = new ArrayList<ValidatorSet>();
		}
		validatorSets.add(validatorSet);
	}
	
}
