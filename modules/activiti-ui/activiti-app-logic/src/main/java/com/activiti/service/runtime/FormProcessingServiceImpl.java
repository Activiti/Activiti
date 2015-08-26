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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.domain.runtime.SubmittedForm;
import com.activiti.exception.FormValidationException;
import com.activiti.model.component.SimpleContentTypeMapper;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.FormFieldTypes;
import com.activiti.model.idm.LightGroupRepresentation;
import com.activiti.model.idm.LightUserRepresentation;
import com.activiti.model.runtime.ProcessInstanceVariableRepresentation;
import com.activiti.model.runtime.RelatedContentRepresentation;
import com.activiti.repository.runtime.FormRepository;
import com.activiti.repository.runtime.RuntimeAppDeploymentRepository;
import com.activiti.repository.runtime.SubmittedFormRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.GroupHierarchyCache;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * @author Joram Barrez
 */
@Service
@Transactional
public class FormProcessingServiceImpl implements FormProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(FormProcessingServiceImpl.class);

    private static final int CONTENT_FETCH_PAGE_SIZE = 50;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected FormService formService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected FormStoreService formStoreService;

    @Autowired
    protected RelatedContentService relatedContentService;

    @Autowired
    protected PermissionService permissionService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected SubmittedFormRepository submittedFormRepository;

    @Autowired
    protected FormRepository formRepository;

    @Autowired
    protected RuntimeAppDeploymentRepository runtimeAppDeploymentRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected SimpleContentTypeMapper contentTypeMapper;

    @Autowired
    protected UserCache userCache;

    @Autowired
    protected GroupHierarchyCache groupCache;

    @Override
    public Form getTaskForm(String taskId) {

        // TODO: this could be optimized, but needs an Activiti change:
		// better would be if the formkey is directly stored on the Task in the db
        // and returned with the query.

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return null;
        }

        String formKey = formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (formKey == null) {
            return null;
        }
        return formStoreService.getForm(formKey);
    }

    @Override
    public Form getStartForm(String processDefinitionId) {
        Form result = null;
        String formKey = formService.getStartFormKey(processDefinitionId);
        if (formKey != null) {
            result = formStoreService.getForm(formKey);
        }
        return result;
    }

    @Override
	public SubmittedFormVariables getVariablesFromFormSubmission(Form form, FormDefinitionRepresentation definition,
	        Map<String, Object> values, String outcome, ObjectNode submittedFormValuesJson) {

        SubmittedFormVariables result = new SubmittedFormVariables();
	    // When no values are given, use an empty map to ensure validation is performed
        // (eg. for required fields)
        if (values == null) {
            values = Collections.emptyMap();
        }

        // Loop over all form fields and see if a value was provided
        Map<String, FormFieldRepresentation> fieldMap = definition.allFieldsAsMap();
        Map<String, Object> variables = new HashMap<String, Object>();
        ObjectNode valuesNode = objectMapper.createObjectNode();
        submittedFormValuesJson.put("values", valuesNode);
        for (String fieldId : fieldMap.keySet()) {
            Object variableValue = null;
            FormFieldRepresentation formField = fieldMap.get(fieldId);

            if (FormFieldTypes.READONLY_TEXT.equals(formField.getType()) || FormFieldTypes.CONTAINER.equals(formField.getType()) || FormFieldTypes.GROUP.equals(formField.getType())) {
                continue;
            }

            if (FormFieldTypes.READONLY.equals(formField.getType())) {

                boolean displayEditableField = false;
                Object tableEditable = formField.getParam("tableEditable");
                Object documentsEditable = formField.getParam("documentsEditable");

                if (tableEditable != null && Boolean.valueOf(tableEditable.toString())) {
                    displayEditableField = true;
                }

                if (documentsEditable != null && Boolean.valueOf(documentsEditable.toString())) {
                    displayEditableField = true;
                }

                if (displayEditableField == false) {
                    continue;
                }
            }

            if (values.containsKey(fieldId)) {

                variableValue = transformFormFieldValueToVariableValue(formField, values.get(fieldId), result, valuesNode);
                variables.put(formField.getId(), variableValue);
            }

            if (formField.isRequired() && variableValue == null && !FormFieldTypes.UPLOAD.equals(formField.getType())) {
                throw new FormValidationException("Form field " + formField.getId() + " is required, but no value was found");
            }
        }

        // Handle outcomes
        if (outcome != null) {
            String targetVariable = "form" + form.getId() + "outcome";
            if (definition.getOutcomeTarget() != null) {
                targetVariable = definition.getOutcomeTarget();
            }
            variables.put(targetVariable, outcome);
            submittedFormValuesJson.put("outcome", outcome);
        }

        result.setVariables(variables);
        return result;
    }

    @Override
    public List<Form> getAllForms(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        RuntimeAppDeployment appDeployment = runtimeAppDeploymentRepository.findByDeploymentId(processDefinition.getDeploymentId());
        List<Form> forms = formRepository.findByAppDeploymentId(appDeployment.getId());
        return forms;
    }

    @Override
    public FormDefinitionRepresentation getTaskFormDefinition(HistoricTaskInstance task) {
        String formKey = formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (formKey == null) {
            throw new NotFoundException("Task form was not found for task id " + task.getId());
        }
        Form form = formStoreService.getForm(formKey);

        try {
            FormDefinitionRepresentation formDefinition = objectMapper.readValue(form.getDefinition(), FormDefinitionRepresentation.class);
            formDefinition.setId(form.getId());
            formDefinition.setName(form.getName());
            formDefinition.setProcessDefinitionId(task.getProcessDefinitionId());
            formDefinition.setTaskId(task.getId());
            formDefinition.setTaskName(task.getName());
            formDefinition.setTaskDefinitionKey(task.getTaskDefinitionKey());

            List<FormValueExpression> expressions = new ArrayList<FormValueExpression>();

            // If task is completed then all fields should be changed to readonly fields
            if (task.getEndTime() != null) {
                createReadonlyForm(task.getProcessInstanceId(), task.getProcessDefinitionId(), task.getId(), formDefinition);

            } else {
                Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
                
                // Check if any reference field needs the value extracted
                expressions.addAll(extractExpressionFields(formDefinition));

                if (!expressions.isEmpty()) {
                    Set<String> variableNames = new HashSet<String>();

                    // Extract all variables needed in all expresions
                    for (FormValueExpression expression : expressions) {
                        variableNames.addAll(expression.getRequiredFieldIds());
                    }

                    // Get all process-variables to extract values from
                    Map<String, Object> finalVariables = new HashMap<String, Object>();
                    Map<String, String> variableTypes = new HashMap<String, String>();

                    // Enhance variables, based on type
                    List<Form> forms = getAllForms(task.getProcessDefinitionId());
                    Object variableValue = null;
                    for (Form formDef : forms) {
                        FormDefinitionRepresentation readFormDefinition = readFormDefinition(formDef);
                        for (FormFieldRepresentation fieldDef : readFormDefinition.listAllFields()) {
                            if (FormFieldTypes.READONLY.equals(fieldDef.getType()) == false &&
                                    FormFieldTypes.READONLY_TEXT.equals(fieldDef.getType()) == false) {
                                variableValue = variables.get(fieldDef.getId());
                                variableValue = getFormFieldValue(variableValue, fieldDef, task.getProcessInstanceId());
                                if (variableValue != null) {
                                    finalVariables.put(fieldDef.getId(), variableValue);
                                    variableTypes.put(fieldDef.getId(), fieldDef.getType());
                                }
                            }
                        }
                    }

                    for (FormValueExpression expression : expressions) {
                        expression.apply(finalVariables, variableTypes, task.getProcessInstanceId(), relatedContentService, contentTypeMapper, objectMapper);
                    }
                }

                for (FormFieldRepresentation formField : formDefinition.listAllFields()) {
                    if (FormFieldTypes.READONLY.equals(formField.getType()) == false &&
                            FormFieldTypes.READONLY_TEXT.equals(formField.getType()) == false) {

                        Object variableValue = readFieldValue(formField.getId(), formField.getType(), variables);
                        
                        if (variableValue != null || FormFieldTypes.UPLOAD.equals(formField.getType())) {
                            variableValue = getFormFieldValue(variableValue, formField, task.getProcessInstanceId());
                            formField.setValue(variableValue);
                        }
                    }
                }
            }

            return formDefinition;

        } catch (IOException e) {
            throw new InternalServerErrorException("Error parsing form definition", e);
        }
    }

    @Override
    public FormDefinitionRepresentation getStartFormDefinition(String processInstanceId) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new NotFoundException("Process instance was not found with id " + processInstance);
        }

        String formKey = formService.getStartFormKey(processInstance.getProcessDefinitionId());
        if (formKey == null) {
            throw new NotFoundException("Process instance form was not found for process definition id " +
                    processInstance.getProcessDefinitionId());
        }
        Form form = formStoreService.getForm(formKey);

        try {
            FormDefinitionRepresentation formDefinition = objectMapper.readValue(form.getDefinition(), FormDefinitionRepresentation.class);
            createReadonlyForm(processInstanceId, processInstance.getProcessDefinitionId(), null, formDefinition);
            return formDefinition;

        } catch (IOException e) {
            throw new InternalServerErrorException("Error parsing form definition", e);
        }
    }

    @Override
    public void storeSubmittedForm(Form form, String taskId, String processInstanceId, JsonNode valuesNode) {
        SubmittedForm submittedForm = new SubmittedForm();
        submittedForm.setForm(form);
        submittedForm.setTaskId(taskId);
        submittedForm.setProcessId(processInstanceId);
        submittedForm.setSubmitted(new Date());
        submittedForm.setSubmittedBy(SecurityUtils.getCurrentUserObject());
        submittedForm.setFieldsValueDefinition(valuesNode.toString());
        submittedFormRepository.save(submittedForm);
    }
    
    @Override
    public Map<String, ProcessInstanceVariableRepresentation> getProcessInstanceVariables(HistoricTaskInstance task) {
        List<HistoricVariableInstance> historicVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(task.getProcessInstanceId()).list();

        // Get all process-variables to extract values from
        Map<String, ProcessInstanceVariableRepresentation> finalVariables = new HashMap<String, ProcessInstanceVariableRepresentation>();

        for (HistoricVariableInstance historicVariableInstance : historicVariables) {
            ProcessInstanceVariableRepresentation processInstanceVariableRepresentation = new ProcessInstanceVariableRepresentation(
                    historicVariableInstance.getVariableName(), historicVariableInstance.getVariableTypeName(), historicVariableInstance.getValue());
            finalVariables.put(historicVariableInstance.getId(), processInstanceVariableRepresentation);
        }

        return finalVariables;
    }

    @SuppressWarnings("unchecked")
    protected Object getFormFieldValue(Object rawValue, FormFieldRepresentation field, String processInstanceId) {
        Object result = rawValue;
        if (FormFieldTypes.UPLOAD.equals(field.getType())) {
	        // In case the referenced value is an upload, there is no actual variable value
            List<RelatedContent> content = new ArrayList<RelatedContent>();
            Page<RelatedContent> page = null;
            int pageNumber = 0;
            while (page == null || page.hasNext()) {
                page = relatedContentService.getFieldContentForProcessInstance(processInstanceId, field.getId(),
                        CONTENT_FETCH_PAGE_SIZE, pageNumber);
                content.addAll(page.getContent());
                pageNumber++;
            }
            
            List<Long> idList = null;
            if (rawValue != null && rawValue instanceof List<?>) {
                List<Long> tempList = (List<Long>) rawValue;
                if (tempList != null && tempList.size() > 0) {
                    idList = tempList;
                }
            }
            
            List<RelatedContentRepresentation> relatedContentList = new ArrayList<RelatedContentRepresentation>();
            for (RelatedContent related : content) {
                if (idList == null || idList.contains(related.getId())) {
                    relatedContentList.add(new RelatedContentRepresentation(related, contentTypeMapper));
                }
            }

            result = relatedContentList;

        } else if (rawValue instanceof Long && FormFieldTypes.PEOPLE.equals(field.getType())) {
	        // In case user is deleted/inactive, we return null instead of throwing an exception
            CachedUser user = userCache.getUser((Long) rawValue);
            if (user != null) {
                LightUserRepresentation userRep = new LightUserRepresentation(user.getUser());
                result = userRep;
            }

        } else if (rawValue instanceof Long && FormFieldTypes.FUNCTIONAL_GROUP.equals(field.getType())) {
            Group group = groupCache.getGroup((Long) rawValue);
            if (group != null) {
                LightGroupRepresentation groupRep = new LightGroupRepresentation(group);
                result = groupRep;
            }

        } else if (FormFieldTypes.DATE.equals(field.getType()) && rawValue != null && rawValue instanceof Date) {
            result = ISO8601Utils.format((Date) rawValue);

        }

        return result;
    }
    
    protected List<FormValueExpression> extractExpressionFields(FormDefinitionRepresentation rep) {
        List<FormValueExpression> result = new ArrayList<FormValueExpression>();
        List<FormFieldRepresentation> allFields = rep.listAllFields();
        for (FormFieldRepresentation field : allFields) {
            if (FormFieldTypes.READONLY.equals(field.getType())) {
                String referencedValue = (String) field.getParam("field");
                if (StringUtils.isNotEmpty(referencedValue)) {
                	result.add(FormValueExpression.parse(referencedValue, field));
                }
            } else if (FormFieldTypes.READONLY_TEXT.equals(field.getType()) && field.getValue() != null) {
                result.add(FormValueExpression.parse(field.getValue().toString(), field));
            }
        }
        return result;
    }

    protected List<FormValueExpression> createReadonlyForm(String processInstanceId, String processDefinitionId,
            String taskId, FormDefinitionRepresentation rep) {

        List<FormValueExpression> result = new ArrayList<FormValueExpression>();
        List<FormFieldRepresentation> allFields = rep.listAllFields();
        if (allFields != null) {

            Map<String, JsonNode> submittedFormMap = new HashMap<String, JsonNode>();
            Map<String, JsonNode> contentFormMap = new HashMap<String, JsonNode>();
            List<SubmittedForm> submittedForms = submittedFormRepository.findByProcessIdOrderByIdDesc(processInstanceId);
            if (CollectionUtils.isNotEmpty(submittedForms)) {

                for (SubmittedForm submittedForm : submittedForms) {
                    try {
                        JsonNode submittedNode = objectMapper.readTree(submittedForm.getFieldsValueDefinition());
                        if (submittedNode != null && submittedNode.get("values") != null) {
                            JsonNode valuesNode = submittedNode.get("values");
                            Iterator<String> fieldIdIterator = valuesNode.fieldNames();
                            while (fieldIdIterator.hasNext()) {
                                String fieldId = fieldIdIterator.next();
                                if (submittedFormMap.containsKey(fieldId) == false || (taskId == null && submittedForm.getTaskId() == null) ||
                                        (taskId != null && taskId.equals(submittedForm.getTaskId()))) {

                                    submittedFormMap.put(fieldId, valuesNode.get(fieldId));
                                }
                            }
                        }

                        if (submittedNode != null && submittedNode.get("content") != null) {
                            JsonNode contentNode = submittedNode.get("content");
                            Iterator<String> fieldIdIterator = contentNode.fieldNames();
                            while (fieldIdIterator.hasNext()) {
                                String fieldId = fieldIdIterator.next();
                                if (contentFormMap.containsKey(fieldId) == false || (taskId == null && submittedForm.getTaskId() == null) ||
                                        (taskId != null && taskId.equals(submittedForm.getTaskId()))) {

                                    contentFormMap.put(fieldId, contentNode.get(fieldId));
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing submitted form " + submittedForm.getId());
                    }
                }
            }

            Map<String, Map<Long, RelatedContent>> relatedContentMap = new HashMap<String, Map<Long, RelatedContent>>();
            Page<RelatedContent> contentPageList = relatedContentService.getAllFieldContentForProcessInstance(
                    processInstanceId, 1000, 0);

            for (RelatedContent relatedContent : contentPageList) {
                if (StringUtils.isNotEmpty(relatedContent.getField())) {
                    Map<Long, RelatedContent> contentFieldMap = null;
                    if (relatedContentMap.get(relatedContent.getField()) != null) {
                        contentFieldMap = relatedContentMap.get(relatedContent.getField());
                    } else {
                        contentFieldMap = new HashMap<Long, RelatedContent>();
                    }
                    contentFieldMap.put(relatedContent.getId(), relatedContent);
                    relatedContentMap.put(relatedContent.getField(), contentFieldMap);
                }
            }

            // get historic process instance variables (for response variable values)
            HistoricVariableInstanceQueryImpl historicVariableInstanceQuery = (HistoricVariableInstanceQueryImpl) historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId);
            List<HistoricVariableInstance> historicVariableInstances = historicVariableInstanceQuery.list();

            Map<String, Object> historicVariableInstancesMap = new HashMap<String, Object>();
            for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
                historicVariableInstancesMap.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
            }

            List<Form> forms = null;

            for (FormFieldRepresentation field : allFields) {

                // the submitedFormMap contains all the submitted values for this processInstance
                // (with the fieldId's in the moment of the form submit)
                // We have to detect the fieldValue by the fieldId of the current field if it's a regular field
                // or the id of the targeted field in case of "Display Value" (readonly) field
                // by default the id of the display value field is the same as the id of the targeted field
                // but it could be changed and the lookup by fieldId is not working. https://github.com/Alfresco/activiti-bpm-suite/issues/896
                JsonNode fieldValueNode = getSubmitedValue(submittedFormMap, field);

                if (fieldValueNode != null) {

                    if (fieldValueNode.isNull() == false) {

                        String fieldType = field.getType();
                        String fieldId = field.getId();
                        String fieldValue = fieldValueNode.asText();

                        if (FormFieldTypes.DATE.equals(fieldType)) {
                            try {
                                Date value = ISO8601Utils.parse(fieldValue);
                                field.setValue(value);
                            } catch (Exception e) {
                                logger.error("Error parsing form date value for process instance " +
                                        processInstanceId + " with value " + fieldValue, e);
                            }

                        } else if (FormFieldTypes.PEOPLE.equals(fieldType)) {
                            if (NumberUtils.isNumber(fieldValue)) {
                                Long personId = Long.valueOf(fieldValue);
                                CachedUser user = userCache.getUser(personId);
                                if (user != null && user.getUser() != null) {
                                    LightUserRepresentation userResult = new LightUserRepresentation(user.getUser());
                                    field.setValue(userResult);
                                }
                            }

                        } else if (FormFieldTypes.FUNCTIONAL_GROUP.equals(fieldType)) {
                            if (NumberUtils.isNumber(fieldValue)) {
                                Long groupId = Long.valueOf(fieldValue);
                                Group group = groupCache.getGroup(groupId);
                                if (group != null) {
                                    LightGroupRepresentation groupResult = new LightGroupRepresentation(group);
                                    field.setValue(groupResult);
                                }
                            }

                        } else if (FormFieldTypes.UPLOAD.equals(fieldType)) {
                            if (contentFormMap.containsKey(fieldId)) {
                                JsonNode contentNodes = contentFormMap.get(fieldId);
                                if (contentNodes != null) {

                                    Map<Long, RelatedContent> fieldContentMap = relatedContentMap.get(fieldId);
                                    if (fieldContentMap != null && fieldContentMap.size() > 0) {
                                        List<RelatedContentRepresentation> relatedContentList = new ArrayList<RelatedContentRepresentation>();
                                        if (contentNodes.isArray()) {
                                            for (JsonNode contentNode : contentNodes) {
                                                addContentItemToList(contentNode, fieldContentMap, relatedContentList);
                                            }
                                        } else {
                                            addContentItemToList(contentNodes, fieldContentMap, relatedContentList);
                                        }

                                        field.setValue(relatedContentList);

                                        if (field.getParam("documentsEditable") != null) {
                                            field.getParams().put("documentsEditable", false);
                                        }
                                    }
                                }
                            }

                        } else {
                            field.setValue(fieldValue);
                        }
                    }
                }

                if (FormFieldTypes.READONLY_TEXT.equals(field.getType()) == false &&
                        FormFieldTypes.READONLY.equals(field.getType()) == false &&
                        FormFieldTypes.GROUP.equals(field.getType()) == false &&
                        FormFieldTypes.CONTAINER.equals(field.getType()) == false) {

                    FormFieldRepresentation paramsField = new FormFieldRepresentation();
                    paramsField.setId(field.getId());
                    paramsField.setName(field.getName());
                    paramsField.setType(field.getType());
                    field.setType(FormFieldTypes.READONLY);
                    Map<String, Object> paramMap = field.getParams();
                    if (paramMap == null) {
                        paramMap = new HashMap<String, Object>();
                        field.setParams(paramMap);
                    }
                    field.getParams().put("field", paramsField);

                } else if (FormFieldTypes.READONLY_TEXT.equals(field.getType()) || FormFieldTypes.READONLY.equals(field.getType())) {

                	String readonlyFieldValue = null;
                	if (FormFieldTypes.READONLY_TEXT.equals(field.getType())) {
                		readonlyFieldValue = field.getValue().toString();
                	} else {
                		readonlyFieldValue = (String) field.getParams().get("field");
                	}
                	
                    FormValueExpression expression = FormValueExpression.parse(readonlyFieldValue, field);
                    if (forms == null) {
                        forms = getAllForms(processDefinitionId);
                    }

                    Map<String, String> fieldTypeMap = new HashMap<String, String>();
                    for (Form form : forms) {
                        FormDefinitionRepresentation formRepresentation = readFormDefinition(form);
                        List<FormFieldRepresentation> allFormFields = formRepresentation.listAllFields();
                        for (FormFieldRepresentation formField : allFormFields) {
                            if (fieldTypeMap.containsKey(formField.getId()) == false) {
                                fieldTypeMap.put(formField.getId(), formField.getType());
                            }
                        }
                    }

                    Map<String, Object> finalVariables = new HashMap<String, Object>();
                    Map<String, String> variableTypes = new HashMap<String, String>();

                    for (String fieldId : expression.getRequiredFieldIds()) {
                        if (contentFormMap.containsKey(fieldId)) {
                            JsonNode contentNodes = contentFormMap.get(fieldId);
                            if (contentNodes != null) {

                                Map<Long, RelatedContent> fieldContentMap = relatedContentMap.get(fieldId);
                                if (fieldContentMap != null && fieldContentMap.size() > 0) {
                                    List<RelatedContentRepresentation> relatedContentList = new ArrayList<RelatedContentRepresentation>();
                                    if (contentNodes.isArray()) {
                                        for (JsonNode contentNode : contentNodes) {
                                            addContentItemToList(contentNode, fieldContentMap, relatedContentList);
                                        }
                                    } else {
                                        addContentItemToList(contentNodes, fieldContentMap, relatedContentList);
                                    }

                                    finalVariables.put(fieldId, relatedContentList);
                                    variableTypes.put(fieldId, FormFieldTypes.UPLOAD);
                                }
                            }

                        } else if (submittedFormMap.containsKey(fieldId)) {
                            String fieldValue = submittedFormMap.get(fieldId).asText();
                            if (StringUtils.isEmpty(fieldValue)) continue;

                            if (FormFieldTypes.PEOPLE.equals(fieldTypeMap.get(fieldId))) {

                                if (NumberUtils.isNumber(fieldValue)) {
                                    Long personId = Long.valueOf(fieldValue);
                                    CachedUser user = userCache.getUser(personId);
                                    if (user != null && user.getUser() != null) {
                                        LightUserRepresentation userResult = new LightUserRepresentation(user.getUser());
                                        finalVariables.put(fieldId, userResult);
                                        variableTypes.put(fieldId, FormFieldTypes.PEOPLE);
                                    }
                                }

                            } else if (FormFieldTypes.FUNCTIONAL_GROUP.equals(fieldTypeMap.get(fieldId))) {

                                if (NumberUtils.isNumber(fieldValue)) {
                                    Long groupId = Long.valueOf(fieldValue);
                                    Group group = groupCache.getGroup(groupId);
                                    if (group != null) {
                                        LightGroupRepresentation groupResult = new LightGroupRepresentation(group);
                                        finalVariables.put(fieldId, groupResult);
                                        variableTypes.put(fieldId, FormFieldTypes.FUNCTIONAL_GROUP);
                                    }
                                }

                            } else if (FormFieldTypes.DATE.equals(fieldTypeMap.get(fieldId))) {
                                try {
                                    Date value = ISO8601Utils.parse(fieldValue);
                                    finalVariables.put(fieldId, value);
                                    variableTypes.put(fieldId, FormFieldTypes.DATE);

                                } catch (Exception e) {
                                    logger.error("Error parsing form date value for process instance " +
                                            processInstanceId + " with value " + fieldValue, e);
                                }

                            } else {
                                finalVariables.put(fieldId, fieldValue);
                            }
                        }
                    }

                    expression.apply(finalVariables, variableTypes, processInstanceId, relatedContentService,
                            contentTypeMapper, objectMapper);
                }
            }
        }
        return result;
    }

    private JsonNode getSubmitedValue(Map<String, JsonNode> submittedFormMap, FormFieldRepresentation field) {
        String fieldId = field.getId();

        // if this is a readonly field should try to find the submitted value related to the targetField.
        if (FormFieldTypes.READONLY.equals(field.getType())) {
            if (field.getParams().containsKey("field")) {
                fieldId = (String) field.getParams().get("field");
            }
        }
        return submittedFormMap.get(fieldId);
    }

    protected void addContentItemToList(JsonNode contentNode, Map<Long, RelatedContent> fieldContentMap,
            List<RelatedContentRepresentation> relatedContentList) {

        Long contentId = contentNode.asLong();
        if (contentId != null && fieldContentMap.containsKey(contentId)) {
            RelatedContent content = fieldContentMap.get(contentId);
            relatedContentList.add(new RelatedContentRepresentation(content, contentTypeMapper));
        }
    }

    @SuppressWarnings("unchecked")
    protected Object transformFormFieldValueToVariableValue(FormFieldRepresentation formField, Object formFieldValue,
            SubmittedFormVariables variables, ObjectNode valuesJson) {

        Object result = formFieldValue;
        if (formField.getType().equals(FormFieldTypes.DATE)) {
            if (StringUtils.isNotEmpty((String) formFieldValue)) {
                try {
                    result = ISO8601Utils.parse((String) formFieldValue);
                    valuesJson.put(formField.getId(), (String) formFieldValue);
                } catch (IllegalArgumentException e) {
                    result = null;
                }
            }

        } else if (formField.getType().equals(FormFieldTypes.INTEGER) && formFieldValue instanceof String) {
            String strFieldValue = (String) formFieldValue;
            if (StringUtils.isNotEmpty(strFieldValue) && NumberUtils.isNumber(strFieldValue)) {
                result = Long.valueOf(strFieldValue);
                valuesJson.put(formField.getId(), (Long) result);
            } else {
                result = (Long) null;
            }

        } else if (formField.getType().equals(FormFieldTypes.AMOUNT) && formFieldValue instanceof String) {
            try {
                result = Double.parseDouble((String) formFieldValue);
                valuesJson.put(formField.getId(), (Double) result);
            } catch (NumberFormatException e) {
                result = null;
            }
        } else if (formField.getType().equals(FormFieldTypes.DROPDOWN)) {
            if (formFieldValue != null && formFieldValue instanceof Map<?, ?>) {
                result = ((Map<?, ?>) formFieldValue).get("id");
                if (result == null) {
                    // fallback to name for manual config options
                    result = ((Map<?, ?>) formFieldValue).get("name");
                }

                if (result != null) {
                    valuesJson.put(formField.getId(), result.toString());
                }
            }

        } else if (formField.getType().equals(FormFieldTypes.RADIO_BUTTONS)) {
            if (formFieldValue != null) {
                valuesJson.put(formField.getId(), formFieldValue.toString());
            }

        } else if (formField.getType().equals(FormFieldTypes.UPLOAD)) {
            processUploadFieldValue(formField, formFieldValue, variables, valuesJson);

		    // We don't store the variable, the field-name will be referenced by the created related content entries
            result = null;

        } else if (formField.getType().equals(FormFieldTypes.PEOPLE) || formField.getType().equals(FormFieldTypes.FUNCTIONAL_GROUP)) {
            // Instead of storing the person as serializable, extract the ID
            if (formFieldValue != null && formFieldValue instanceof Map<?, ?>) {
                Map<String, Object> value = (Map<String, Object>) formFieldValue;
                Object id = value.get("id");
                if (id instanceof Number) {
                    result = ((Number) id).longValue();
                    valuesJson.put(formField.getId(), result.toString());
                } else {
                    // Wrong type, ignore
                    result = null;
                }
            } else {
                // Incorrect or empty map, ignore
                result = null;
            }

        } else if (formField.getType().equals(FormFieldTypes.READONLY)) {
            if (formField.getParam("field") != null && ((Map<String, Object>) formField.getParam("field")).get("type") != null) {

                String readonlyType = (String) ((Map<String, Object>) formField.getParam("field")).get("type");

                if (FormFieldTypes.UPLOAD.equals(readonlyType)) {
                    processUploadFieldValue(formField, formFieldValue, variables, valuesJson);

		            // We don't store the variable, the field-name will be referenced by the created related content
                    // entries
                    result = null;
                }
            }

		} else if (formField.getType().equals(FormFieldTypes.READONLY) == false &&
		        formField.getType().equals(FormFieldTypes.READONLY_TEXT) == false) {
            
		    if (formFieldValue != null) {
                valuesJson.put(formField.getId(), formFieldValue.toString());
            }
        }

        // default empty value to the valuesJson.
        if (result == null && !formField.getType().equals(FormFieldTypes.UPLOAD)) {
            valuesJson.put(formField.getId(), "");
        }

        // Default: no processing needs to be done, can be stored as-is
        return result;
    }
    
    protected Object readFieldValue(String fieldId, String fieldType, Map<String, Object> variables) {
        Object variableValue = null;
        
        if (variables.containsKey(fieldId)) {
            variableValue = variables.get(fieldId);
        }
        return variableValue;
    }

    protected void processUploadFieldValue(FormFieldRepresentation formField, Object formFieldValue,
            SubmittedFormVariables variables, ObjectNode valuesJson) {

        if (formFieldValue != null) {
            if (!(formFieldValue instanceof String)) {
                throw new FormValidationException("Expecting a string value for upload field, but was: " + formFieldValue.getClass().getName());
            }
            String valueString = (String) formFieldValue;
            String[] parts = StringUtils.split(valueString, ",");

            User user = SecurityUtils.getCurrentUserObject();
            for (String idString : parts) {
                Long id = null;
                try {
                    id = Long.parseLong(idString);
                } catch (Exception e) {
                    throw new FormValidationException("Illegal related content id: " + idString);
                }

                RelatedContent content = relatedContentService.getRelatedContent(id, false);
                if (!permissionService.hasWritePermissionOnRelatedContent(user, content)) {
                    throw new FormValidationException("No permissions to add content with id: " + id);
                }

                if (content != null) {
                    variables.addContent(formField.getId(), content);
                }
            }
            // empty content list if there is no file uploaded
            if (parts.length==0){
                variables.addContent(formField.getId(), null);
            }

            valuesJson.put(formField.getId(), (String) formFieldValue);
        }
    }

    protected FormDefinitionRepresentation readFormDefinition(Form form) {
        try {
            return objectMapper.readValue(form.getDefinition(), FormDefinitionRepresentation.class);
        } catch (IOException e) {
            logger.error("Error reading form " + form.getId(), e);
            throw new FormValidationException("Could not read form definition" + e.getMessage());
        }
    }
}
