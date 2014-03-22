package org.activiti.validation;

import org.activiti.validation.validator.ValidatorSetFactory;

/**
 * @author jbarrez
 */
public class ProcessValidatorFactory {
	
	public ProcessValidator createDefaultProcessValidator() {
		ProcessValidatorImpl processValidator = new ProcessValidatorImpl();
		processValidator.addValidatorSet(new ValidatorSetFactory().createActivitiExecutableProcessValidatorSet());
		return processValidator;
	}

}
