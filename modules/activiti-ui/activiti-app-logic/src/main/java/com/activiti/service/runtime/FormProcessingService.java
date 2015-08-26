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
package com.activiti.service.runtime;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;

import com.activiti.domain.runtime.Form;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.exception.FormValidationException;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.runtime.ProcessInstanceVariableRepresentation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 */
public interface FormProcessingService {

	Form getTaskForm(String taskId);
	
	/**
	 * @return a form definition for the given task. Also values are extracted from the execution, in case
	 * fields require a value to be used.
	 */
	FormDefinitionRepresentation getTaskFormDefinition(HistoricTaskInstance task);
	
	/**
     * @return a form definition for the start form of a process instance id.
     */
    FormDefinitionRepresentation getStartFormDefinition(String processInstanceId);
	
	/**
	 * @return start form for the given process definition. Returns null, if no start
	 * form is defined.
	 * 
	 * @throws ActivitiObjectNotFoundException When no process definition exists with the given id.
	 */
	Form getStartForm(String processDefinitionId);
	
	/**
	 * @return all forms present in the process definition with the given id. Includes all forms in
	 * user tasks as well as the start-form. The order of forms is returned in the order they appear in
	 * the definition.
	 */
	List<Form> getAllForms(String processDefinitionId);
	
	/**
	 * @param definition form definition to use for type-conversion and validation
	 * @param values values submitted by the user
	 * @param outcome outcome selected by the user. If null, no outcome is used and any outcome definitions are ignored.
	 * @param submittedFormValuesJson json node to fill with submitted form field values
	 * 
	 * @return raw variables that can be used in the activiti engine, based on the filled in values and selected outcome. Also contains
	 * all {@link RelatedContent} objects that are added for each variable/field.
	 * @throws FormValidationException when a submitted value is not valid or a required value is missing.
	 */
	SubmittedFormVariables getVariablesFromFormSubmission(Form form, FormDefinitionRepresentation definition, 
	        Map<String, Object> values, String outcome, ObjectNode submittedFormValuesJson);
	
	/**
	 * Store the submitted form values.
	 * 
     * @param form form instance of the submitted form
     * @param taskId task instance id of the completed task
     * @param processInstanceId process instance id of the completed task
     * @param valuesNode json node with the values of the 
     */
	void storeSubmittedForm(Form form, String taskId, String processInstanceId, JsonNode valuesNode);
	
	/**
     * @return map with unique process instance variables with their types
     * and values for the given {@link HistoricTaskInstance} 
     */
	Map<String, ProcessInstanceVariableRepresentation> getProcessInstanceVariables(HistoricTaskInstance task);
}
