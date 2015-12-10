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

import java.net.URLDecoder;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activiti.domain.runtime.Form;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.runtime.FormProcessingService;
import com.activiti.service.runtime.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractProcessDefinitionResource {

    private final Logger logger = LoggerFactory.getLogger(AbstractProcessDefinitionResource.class);

    @Inject
    protected RepositoryService repositoryService;

    @Inject
    protected FormProcessingService formProcessingSerice;

    @Inject
    protected PermissionService permissionService;

    @Inject
    protected ObjectMapper objectMapper;

    public FormDefinitionRepresentation getProcessDefinitionStartForm(HttpServletRequest request) {

        String[] requestInfoArray = parseRequest(request);
        String processDefinitionId = getProcessDefinitionId(requestInfoArray, requestInfoArray.length - 2);

        ProcessDefinition processDefinition = permissionService.getProcessDefinitionById(processDefinitionId);

        try {
            return getStartForm(processDefinition);

        } catch (ActivitiObjectNotFoundException aonfe) {
            // Process definition does not exist
            throw new NotFoundException("No process definition found with the given id: " + processDefinitionId);
        }
    }

    protected FormDefinitionRepresentation getStartForm(ProcessDefinition processDefinition) {
        Form form = formProcessingSerice.getStartForm(processDefinition.getId());
        if (form != null) {
            FormDefinitionRepresentation formDefinitionRepresentation = null;
            try {
                formDefinitionRepresentation = objectMapper.readValue(form.getDefinition(), FormDefinitionRepresentation.class);
                formDefinitionRepresentation.setProcessDefinitionId(processDefinition.getId());
                formDefinitionRepresentation.setProcessDefinitionName(processDefinition.getName());
                formDefinitionRepresentation.setProcessDefinitionKey(processDefinition.getKey());
            } catch (Exception e) {
                throw new InternalServerErrorException("Could not deserialize form definition");
            }
            return formDefinitionRepresentation;
        } else {
            // Definition found, but no form attached
            throw new NotFoundException("Process definition does not have a form defined: " + processDefinition.getId());
        }
    }

    protected ProcessDefinition getProcessDefinitionFromRequest(String[] requestInfoArray, boolean isTableRequest) {
        int paramPosition = requestInfoArray.length - 3;
        if (isTableRequest) {
            paramPosition--;
        }
        String processDefinitionId = getProcessDefinitionId(requestInfoArray, paramPosition);

        ProcessDefinition processDefinition = permissionService.getProcessDefinitionById(processDefinitionId);

        return processDefinition;
    }

    protected FormFieldRepresentation getFormFieldFromRequest(String[] requestInfoArray, ProcessDefinition processDefinition, boolean isTableRequest) {
        FormDefinitionRepresentation form = getStartForm(processDefinition);
        int paramPosition = requestInfoArray.length - 1;
        if (isTableRequest) {
            paramPosition--;
        }
        String fieldVariable = requestInfoArray[paramPosition];

        List<? extends FormFieldRepresentation> allFields = form.listAllFields();
        FormFieldRepresentation selectedField = null;
        if (CollectionUtils.isNotEmpty(allFields)) {
            for (FormFieldRepresentation formFieldRepresentation : allFields) {
                if (formFieldRepresentation.getId().equalsIgnoreCase(fieldVariable)) {
                    selectedField = formFieldRepresentation;
                }
            }
        }

        if (selectedField == null) {
            throw new NotFoundException("Field could not be found in start form definition " + fieldVariable);
        }

        return selectedField;
    }
    
    protected String[] parseRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String[] requestInfoArray = requestURI.split("/");
        if (requestInfoArray.length < 2) {
            throw new BadRequestException("Start form request is not valid " + requestURI);
        }
        return requestInfoArray;
    }

    protected String getProcessDefinitionId(String[] requestInfoArray, int position) {
        String processDefinitionVariable = requestInfoArray[position];
        String processDefinitionId = null;
        try {
            processDefinitionId = URLDecoder.decode(processDefinitionVariable, "UTF-8");
        } catch (Exception e) {
            logger.error("Error decoding process definition " + processDefinitionVariable, e);
            throw new InternalServerErrorException("Error decoding process definition " + processDefinitionVariable);
        }
        return processDefinitionId;
    }
}