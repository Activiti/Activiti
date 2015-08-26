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
