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
package com.activiti.rest.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.exception.FormValidationException;
import com.activiti.model.component.SimpleContentTypeMapper;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.FormFieldTypes;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.model.runtime.CreateProcessInstanceRepresentation;
import com.activiti.model.runtime.ProcessInstanceRepresentation;
import com.activiti.model.runtime.RelatedContentRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.ActivitiService;
import com.activiti.service.runtime.FormProcessingService;
import com.activiti.service.runtime.PermissionService;
import com.activiti.service.runtime.RelatedContentService;
import com.activiti.service.runtime.SubmittedFormVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractProcessInstancesResource {

    private static final int MAX_CONTENT_PAGE_SIZE = 200;
    
    @Autowired
    protected ActivitiService activitiService;

    @Autowired
    protected RepositoryService repositoryService;
    
    @Autowired
    protected HistoryService historyService;
    
    @Autowired
    protected PermissionService permissionService;
    
    @Autowired
    protected FormProcessingService formProcessingService;
    
    @Autowired
    protected RelatedContentService relatedContentService;
    
    @Autowired
    protected SimpleContentTypeMapper typeMapper;
    
    @Autowired
    protected UserCache userCache;

    @Autowired
    protected ObjectMapper objectMapper;
    
    public ProcessInstanceRepresentation startNewProcessInstance(CreateProcessInstanceRepresentation startRequest) {
        if (StringUtils.isEmpty(startRequest.getProcessDefinitionId())) {
            throw new BadRequestException("Process definition id is required");
        }
        
        ProcessDefinition processDefinition = permissionService.getProcessDefinitionById(startRequest.getProcessDefinitionId());

		Form startForm = null;
		SubmittedFormVariables formSubmission = null;

		ObjectNode submittedFormValuesJson = objectMapper.createObjectNode();
		if (startRequest.getValues() != null || startRequest.getOutcome() != null) {
			// Submitted a form
			startForm = formProcessingService.getStartForm(startRequest.getProcessDefinitionId());
			if (startForm == null) {
				throw new NotFoundException("No start form has been found for process definition: " + startRequest.getProcessDefinitionId());
			}

			// Read form definition
			FormDefinitionRepresentation formDefinitionRepresentation = readFormDefinition(startForm);

			// Validate and extract raw variables
			try {
				formSubmission = formProcessingService.getVariablesFromFormSubmission(startForm, formDefinitionRepresentation,
				        startRequest.getValues(), startRequest.getOutcome(), submittedFormValuesJson);

			} catch (FormValidationException fve) {
				throw new BadRequestException("Validation of form failed: " + fve.getMessage());
			}
		}

		Map<String, Object> variables = null;
		if (formSubmission != null) {
			variables = formSubmission.getVariables();
		}
		ProcessInstance processInstance = activitiService.startProcessInstance(startRequest.getProcessDefinitionId(), variables,
		        startRequest.getName());

		// Mark any content created as part of the form-submission connected to
		// the process instance
		if (formSubmission != null) {
			if (formSubmission.hasContent()) {
				ObjectNode contentNode = objectMapper.createObjectNode();
				submittedFormValuesJson.put("content", contentNode);
				for (Entry<String, List<RelatedContent>> entry : formSubmission.getVariableContent().entrySet()) {
					ArrayNode contentArray = objectMapper.createArrayNode();
					for (RelatedContent content : entry.getValue()) {
						relatedContentService.setContentField(content.getId(), entry.getKey(), processInstance.getId(), null);
						contentArray.add(content.getId());
					}
					contentNode.put(entry.getKey(), contentArray);
				}
			}
			formProcessingService.storeSubmittedForm(startForm, null, processInstance.getId(), submittedFormValuesJson);
		}

		HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
		        .processInstanceId(processInstance.getId()).singleResult();

		LightUserRepresentation userRep = null;
		if (historicProcess.getStartUserId() != null) {
			CachedUser user = userCache.getUser(Long.parseLong(historicProcess.getStartUserId()));
			if (user != null && user.getUser() != null) {
				userRep = new LightUserRepresentation(user.getUser());
			}
		}
		return new ProcessInstanceRepresentation(historicProcess, processDefinition,
		        ((ProcessDefinitionEntity) processDefinition).isGraphicalNotationDefined(), userRep);
            
    }

    protected Map<String, List<RelatedContent>> groupContentByField(Page<RelatedContent> allContent) {
        HashMap<String, List<RelatedContent>> result = new HashMap<String, List<RelatedContent>>();
        List<RelatedContent> list;
        for(RelatedContent content : allContent.getContent()) {
            list = result.get(content.getField());
            if(list == null) {
                list = new ArrayList<RelatedContent>();
                result.put(content.getField(), list);
            }
            list.add(content);
        }
        return result;
    }
    
    protected Map<String, String> getTitlesForFields(Collection<String> fieldIds, HistoricProcessInstance processInstance) {
        Map<String, String> titles = new HashMap<String, String>();
        
        // Fetch all form-definitions used in process and fetch title for the field
        List<Form> forms = formProcessingService.getAllForms(processInstance.getProcessDefinitionId());
        FormDefinitionRepresentation definition = null;
        for(Form form : forms) {
            definition = readFormDefinition(form);
            for(FormFieldRepresentation field : definition.listAllFields()) {
                if(FormFieldTypes.UPLOAD.equals(field.getType())) {
                    // Found an upload type, check if the field title is needed
                    if(fieldIds.contains(field.getId())) {
                        titles.put(field.getId(), field.getName());
                    }
                }
            }
        }
        return titles;
    }
    
    protected FormDefinitionRepresentation readFormDefinition(Form form) {
        try {
            return objectMapper.readValue(form.getDefinition(), FormDefinitionRepresentation.class);
        } catch (Exception e) {
            throw new InternalServerErrorException("Could not deserialize form definition");
        }
    }
    
    protected RelatedContentRepresentation createRelatedContentResponse(RelatedContent relatedContent) {
        RelatedContentRepresentation relatedContentResponse = new RelatedContentRepresentation(relatedContent, typeMapper);
        return relatedContentResponse;
    }
}
