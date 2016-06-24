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
package com.activiti.model.editor;

import org.activiti.validation.ValidationError;



/**
 * Representation of validation errors of a bpmn model.
 * 
 * @author Tijs Rademakers
 */
public class BpmnValidationErrorRepresentation extends ValidationErrorRepresentation {
	
	protected String processDefinitionId;
	
	protected String processDefinitionName;
	
	public BpmnValidationErrorRepresentation(ValidationError error) {
	    this.id = error.getActivityId();
	    this.name = error.getActivityName();
	    this.processDefinitionId = error.getProcessDefinitionId();
	    this.processDefinitionName = error.getProcessDefinitionName();
	    this.validatorSetName = error.getValidatorSetName();
	    this.problem = error.getProblem();
	    this.defaultDescription = error.getDefaultDescription();
	    this.isWarning = error.isWarning();
	}
	
	public BpmnValidationErrorRepresentation() {
	    super();
	}

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }
}
