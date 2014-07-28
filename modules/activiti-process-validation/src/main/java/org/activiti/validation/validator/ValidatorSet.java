package org.activiti.validation.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jbarrez
 */
public class ValidatorSet {
	
	protected String name;

	protected Map<Class<? extends Validator>, Validator> validators;
	
	public ValidatorSet(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Validator> getValidators() {
		return validators.values();
	}

	public void setValidators(Collection<? extends Validator> validators) {
		for (Validator validator : validators) {
			addValidator(validator);
		}
	}
	
	public void removeValidator(Class<Validator> validatorClass) {
		validators.remove(validatorClass);
	}

	public void addValidator(Validator validator) {
		if (validators == null) {
			validators = new HashMap<Class<? extends Validator>, Validator>();
		}
		validators.put(validator.getClass(), validator);
	}

}
