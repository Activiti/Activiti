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
package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ValidatorImpl;

/**
 * @author jbarrez
 */
public class BpmnModelValidator extends ValidatorImpl {
	
	@Override
	public void validate(BpmnModel bpmnModel, List<ValidationError> errors) {
		
		// If all process definitions of this bpmnModel are not executable, raise an error
		boolean isAtLeastOneExecutable = validateAtLeastOneExecutable(bpmnModel, errors);
		
		// If at least one process definition is executable, show a warning for each of the none-executables
		if (isAtLeastOneExecutable) {
			for (Process process : bpmnModel.getProcesses()) {
				if (!process.isExecutable()) {
					addWarning(errors, Problems.PROCESS_DEFINITION_NOT_EXECUTABLE, process, process, 
							"Process definition is not executable. Please verify that this is intentional.");
				}
			}
		}
	}

	/**
	 * Returns 'true' if at least one process definition in the {@link BpmnModel} is executable.
	 */
	private boolean validateAtLeastOneExecutable(BpmnModel bpmnModel, List<ValidationError> errors) {
	  int nrOfExecutableDefinitions = 0;
		for (Process process : bpmnModel.getProcesses()) {
			if (process.isExecutable()) {
				nrOfExecutableDefinitions++;
			}
		}
		
		if (nrOfExecutableDefinitions == 0) {
			addError(errors, Problems.ALL_PROCESS_DEFINITIONS_NOT_EXECUTABLE,
					"All process definition are set to be non-executable (property 'isExecutable' on process). This is not allowed.");
		}
		
		return nrOfExecutableDefinitions > 0;
  }

}
