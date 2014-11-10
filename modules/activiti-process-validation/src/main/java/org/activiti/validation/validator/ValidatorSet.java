/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	public void removeValidator(Class<? extends Validator> validatorClass) {
		validators.remove(validatorClass);
	}

	public void addValidator(Validator validator) {
		if (validators == null) {
			validators = new HashMap<Class<? extends Validator>, Validator>();
		}
		validators.put(validator.getClass(), validator);
	}

}
