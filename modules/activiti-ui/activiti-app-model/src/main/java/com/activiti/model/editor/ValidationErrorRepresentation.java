/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
