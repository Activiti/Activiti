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
package org.activiti.workflow.simple.alfresco.step;

import java.util.HashMap;

import org.activiti.workflow.simple.definition.AbstractNamedStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

/**
 * Step that ends the current execution.
 * 
 * @author Frederik Heremans
 */
public class AlfrescoEndProcessStepDefinition extends AbstractNamedStepDefinition {

	private static final long serialVersionUID = 1L;

	@Override
	public StepDefinition clone() {
		AlfrescoEndProcessStepDefinition clone = new AlfrescoEndProcessStepDefinition();
		clone.setValues(this);
		return clone;
	}

	@Override
	public void setValues(StepDefinition otherDefinition) {
		if (!(otherDefinition instanceof AlfrescoEndProcessStepDefinition)) {
			throw new SimpleWorkflowException("An instance of AlfrescoEndProcessStep is required to set values");
		}
		
		AlfrescoEndProcessStepDefinition def = (AlfrescoEndProcessStepDefinition) otherDefinition;
		
		setName(def.getName());
		setDescription(def.getDescription());
		setId(def.getId());
		if(def.getParameters() != null) {
			setParameters(new HashMap<String, Object>(def.getParameters()));
		}

	}

}
