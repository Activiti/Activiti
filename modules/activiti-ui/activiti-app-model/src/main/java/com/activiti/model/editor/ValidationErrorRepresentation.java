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

import com.activiti.model.common.AbstractRepresentation;



/**
 * Representation of validation errors of a model.
 * 
 * @author Tijs Rademakers
 */
public class ValidationErrorRepresentation extends AbstractRepresentation {
	
	protected String id;
	
	protected String name;
	
	protected String validatorSetName;
    
    protected String problem;
    
    // Default description in english. 
    // Other languages can map the validatorSetName/validatorName to the translated version. 
    protected String defaultDescription;
    
    protected boolean isWarning;
	
    
    public ValidationErrorRepresentation(FormValidationError error) {
        this.id = error.getFieldId();
        this.name = error.getFieldName();
        this.validatorSetName = error.getValidatorSetName();
        this.problem = error.getProblem();
        this.defaultDescription = error.getDefaultDescription();
        this.isWarning = error.isWarning();
    }
    
	public ValidationErrorRepresentation() {
	    
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValidatorSetName() {
        return validatorSetName;
    }

    public void setValidatorSetName(String validatorSetName) {
        this.validatorSetName = validatorSetName;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public boolean isWarning() {
        return isWarning;
    }

    public void setWarning(boolean isWarning) {
        this.isWarning = isWarning;
    }
}
