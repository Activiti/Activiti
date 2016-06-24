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
package com.activiti.rest.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;
import com.activiti.domain.runtime.RelatedContent;
import com.activiti.exception.FormValidationException;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormFieldRepresentation;
import com.activiti.model.editor.form.FormFieldTypes;
import com.activiti.model.runtime.CompleteFormRepresentation;
import com.activiti.model.runtime.ProcessInstanceVariableRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.FormProcessingService;
import com.activiti.service.runtime.PermissionService;
import com.activiti.service.runtime.RelatedContentService;
import com.activiti.service.runtime.SubmittedFormVariables;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 */
public abstract class AbstractTaskFormResource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTaskFormResource.class);

    @Inject
    protected TaskService taskService;

    @Inject
    protected FormProcessingService formProcessingService;

    @Inject
    protected PermissionService permissionService;

    @Inject
    protected RelatedContentService relatedContentService;

    @Inject
    protected ObjectMapper objectMapper;

    public FormDefinitionRepresentation getTaskForm(String taskId) {
        HistoricTaskInstance task = permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
        FormDefinitionRepresentation form = formProcessingService.getTaskFormDefinition(task);

        // If form does not exists, we don't want to leak out this info to just anyone
        if (form == null) {
            throw new NotFoundException();
        }

        ProcessDefinition processDefinition = permissionService.getProcessDefinitionById(task.getProcessDefinitionId());
        form.setProcessDefinitionName(processDefinition.getName());
        form.setProcessDefinitionKey(processDefinition.getKey());

        return form;
    }

    public void completeTaskForm(String taskId, CompleteFormRepresentation completeTaskFormRepresentation) {

        // Get the form definition
        Form form = formProcessingService.getTaskForm(taskId);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        if (task == null) {
            throw new NotFoundException("Task not found with id: " + taskId);
        }

        User currentUser = SecurityUtils.getCurrentUserObject();

        if (!permissionService.isTaskOwnerOrAssignee(currentUser, taskId)) {
            if (!permissionService.validateIfUserIsInitiatorAndCanCompleteTask(currentUser, task)) {
                throw new NotPermittedException();
            }
        }

        FormDefinitionRepresentation formDefinitionRepresentation = null;
        try {
            formDefinitionRepresentation = objectMapper.readValue(form.getDefinition(), FormDefinitionRepresentation.class);
        } catch (Exception e) {
            throw new InternalServerErrorException("Could not deserialize form definition");
        }

        try {
            ObjectNode submittedFormValuesJson = objectMapper.createObjectNode();
            // Extract raw variables and complete the task
            SubmittedFormVariables formSubmission = formProcessingService.getVariablesFromFormSubmission(form, formDefinitionRepresentation,
                    completeTaskFormRepresentation.getValues(), completeTaskFormRepresentation.getOutcome(), submittedFormValuesJson);

            Map<String, Object> variables = null;
            if (formSubmission != null) {
                variables = formSubmission.getVariables();
                
                // get all upload fields
                List<String> uploadFieldIds = new ArrayList<String>();
                List<FormFieldRepresentation> fields = formDefinitionRepresentation.listAllFields();
                for (FormFieldRepresentation formField : fields) {
                    if (FormFieldTypes.UPLOAD.equals(formField.getType())) {
                        uploadFieldIds.add(formField.getId());
                    }
                }
                
                Map<Long, RelatedContent> existingContentIds = new HashMap<Long, RelatedContent>();
                Page<RelatedContent> storedContent = relatedContentService.getAllFieldContentForProcessInstance(task.getProcessInstanceId(), 1000, 0);
                if (storedContent.getContent() != null) {
                    for (RelatedContent content : storedContent.getContent()) {
                        if (content.getField() != null && uploadFieldIds.contains(content.getField())) {
                            existingContentIds.put(content.getId(), content);
                        }
                    }
                }
                
                if (formSubmission.hasContent()) {
                    // Mark any content created as part of the form-submission connected to the task and field
                    ObjectNode contentNode = objectMapper.createObjectNode();
                    submittedFormValuesJson.put("content", contentNode);
                    for (Entry<String, List<RelatedContent>> entry : formSubmission.getVariableContent().entrySet()) {
                        ArrayNode contentArray = objectMapper.createArrayNode();
                        for (RelatedContent content : entry.getValue()) {
                            relatedContentService.setContentField(content.getId(), entry.getKey(), task.getProcessInstanceId(), task.getId());
                            existingContentIds.remove(content.getId());
                            contentArray.add(content.getId());
                        }
                        contentNode.put(entry.getKey(), contentArray);
                    }
                }
                
                for (RelatedContent toDeleteContent : existingContentIds.values()) {
                    relatedContentService.deleteRelatedContent(toDeleteContent);
                }
                
                formProcessingService.storeSubmittedForm(form, task.getId(), task.getProcessInstanceId(), submittedFormValuesJson);
            }
            taskService.complete(taskId, variables);

        } catch (FormValidationException fve) {
            throw new BadRequestException("Validation of form failed: " + fve.getMessage());
        }
    }

    public List<ProcessInstanceVariableRepresentation> getProcessInstanceVariables(String taskId) {
        HistoricTaskInstance task = permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
        Map<String, ProcessInstanceVariableRepresentation> processInstanceVariables = formProcessingService.getProcessInstanceVariables(task);
        List<ProcessInstanceVariableRepresentation> processInstanceVariableRepresenations = new ArrayList<ProcessInstanceVariableRepresentation>(
                processInstanceVariables.values());
        return processInstanceVariableRepresenations;
    }

    protected FormFieldRepresentation getFormFieldFromTaskForm(String taskId, String field) {
        HistoricTaskInstance task = permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
        FormDefinitionRepresentation form = formProcessingService.getTaskFormDefinition(task);

        // If form does not exists, we don't want to leak out this info to just
        // anyone
        if (form == null) {
            throw new NotFoundException();
        }

        FormFieldRepresentation selectedField = null;
        for (FormFieldRepresentation formFieldRepresentation : form.listAllFields()) {
            if (formFieldRepresentation.getId().equalsIgnoreCase(field)) {
                selectedField = formFieldRepresentation;
            }
        }

        if (selectedField == null) {
            throw new NotFoundException("Field could not be found in form definition " + field);
        }

        return selectedField;
    }
}
