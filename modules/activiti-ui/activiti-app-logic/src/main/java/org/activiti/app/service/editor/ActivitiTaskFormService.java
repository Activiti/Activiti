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
package org.activiti.app.service.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.app.model.runtime.CompleteFormRepresentation;
import org.activiti.app.model.runtime.ProcessInstanceVariableRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.api.FormService;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
@Service
public class ActivitiTaskFormService {

  private static final Logger logger = LoggerFactory.getLogger(ActivitiTaskFormService.class);

  @Autowired
  protected TaskService taskService;
  
  @Autowired
  protected RepositoryService repositoryService;
  
  @Autowired
  protected HistoryService historyService;

  @Autowired
  protected FormRepositoryService formRepositoryService;
  
  @Autowired
  protected FormService formService;

  @Autowired
  protected PermissionService permissionService;

  @Autowired
  protected ObjectMapper objectMapper;

  public FormDefinition getTaskForm(String taskId) {
    HistoricTaskInstance task = permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
    
    Map<String, Object> variables = new HashMap<String, Object>();
    if (task.getProcessInstanceId() != null) {
      List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery()
          .processInstanceId(task.getProcessInstanceId())
          .list();
      
      for (HistoricVariableInstance historicVariableInstance : variableInstances) {
        variables.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
      }
    }
    
    String parentDeploymentId = null;
    if (StringUtils.isNotEmpty(task.getProcessDefinitionId())) {
      try {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        parentDeploymentId = processDefinition.getDeploymentId();
        
      } catch (ActivitiException e) {
        logger.error("Error getting process definition " + task.getProcessDefinitionId(), e);
      }
    }
    
    FormDefinition formDefinition = null;
    if (task.getEndTime() != null) {
      formDefinition = formService.getCompletedTaskFormDefinitionByKeyAndParentDeploymentId(task.getFormKey(), parentDeploymentId, 
          taskId, task.getProcessInstanceId(), variables, task.getTenantId());
      
    } else {
      formDefinition = formService.getTaskFormDefinitionByKeyAndParentDeploymentId(task.getFormKey(), parentDeploymentId, 
          task.getProcessInstanceId(), variables, task.getTenantId());
    }

    // If form does not exists, we don't want to leak out this info to just anyone
    if (formDefinition == null) {
      throw new NotFoundException("Form definition for task " + task.getTaskDefinitionKey() + " cannot be found for form key " + task.getFormKey());
    }

    return formDefinition;
  }
  
  public void completeTaskForm(String taskId, CompleteFormRepresentation completeTaskFormRepresentation) {

    // Get the form definition
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task not found with id: " + taskId);
    }
    
    FormDefinition formDefinition = formRepositoryService.getFormDefinitionById(completeTaskFormRepresentation.getFormId());

    User currentUser = SecurityUtils.getCurrentUserObject();

    if (!permissionService.isTaskOwnerOrAssignee(currentUser, taskId)) {
      if (!permissionService.validateIfUserIsInitiatorAndCanCompleteTask(currentUser, task)) {
        throw new NotPermittedException();
      }
    }

    // Extract raw variables and complete the task
    Map<String, Object> variables = formService.getVariablesFromFormSubmission(formDefinition, completeTaskFormRepresentation.getValues(),
        completeTaskFormRepresentation.getOutcome());

    formService.storeSubmittedForm(variables, formDefinition, task.getId(), task.getProcessInstanceId());
    
    taskService.complete(taskId, variables);
  }
  
  public List<ProcessInstanceVariableRepresentation> getProcessInstanceVariables(String taskId) {
    HistoricTaskInstance task = permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
    List<HistoricVariableInstance> historicVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(task.getProcessInstanceId()).list();

    // Get all process-variables to extract values from
    Map<String, ProcessInstanceVariableRepresentation> processInstanceVariables = new HashMap<String, ProcessInstanceVariableRepresentation>();

    for (HistoricVariableInstance historicVariableInstance : historicVariables) {
        ProcessInstanceVariableRepresentation processInstanceVariableRepresentation = new ProcessInstanceVariableRepresentation(
                historicVariableInstance.getVariableName(), historicVariableInstance.getVariableTypeName(), historicVariableInstance.getValue());
        processInstanceVariables.put(historicVariableInstance.getId(), processInstanceVariableRepresentation);
    }

    List<ProcessInstanceVariableRepresentation> processInstanceVariableRepresenations = 
        new ArrayList<ProcessInstanceVariableRepresentation>(processInstanceVariables.values());
    return processInstanceVariableRepresenations;
  }
}
