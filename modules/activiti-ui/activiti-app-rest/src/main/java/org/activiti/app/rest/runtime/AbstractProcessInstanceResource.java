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
package org.activiti.app.rest.runtime;

import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.runtime.ProcessInstanceRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.app.service.runtime.ProcessInstanceService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.api.FormService;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractProcessInstanceResource {

  private static final Logger logger = LoggerFactory.getLogger(AbstractProcessInstanceResource.class);

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected HistoryService historyService;

  @Autowired
  protected RuntimeService runtimeService;
  
  @Autowired
  protected FormRepositoryService formRepositoryService;
  
  @Autowired
  protected FormService formService;

  @Autowired
  protected PermissionService permissionService;

  @Autowired
  protected ProcessInstanceService processInstanceService;

  @Autowired
  protected UserCache userCache;

  public ProcessInstanceRepresentation getProcessInstance(String processInstanceId, HttpServletResponse response) {

    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

    if (!permissionService.hasReadPermissionOnProcessInstance(SecurityUtils.getCurrentUserObject(), processInstance, processInstanceId)) {
      throw new NotFoundException("Process with id: " + processInstanceId + " does not exist or is not available for this user");
    }

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

    User userRep = null;
    if (processInstance.getStartUserId() != null) {
      CachedUser user = userCache.getUser(processInstance.getStartUserId());
      if (user != null && user.getUser() != null) {
        userRep = user.getUser();
      }
    }

    ProcessInstanceRepresentation processInstanceResult = new ProcessInstanceRepresentation(processInstance, processDefinition, processDefinition.isGraphicalNotationDefined(), userRep);

    FormDefinition formDefinition = getStartFormDefinition(processInstance.getProcessDefinitionId(), processDefinition, processInstance.getId());
    if (formDefinition != null) {
      processInstanceResult.setStartFormDefined(true);
    }

    return processInstanceResult;
  }

  public FormDefinition getProcessInstanceStartForm(String processInstanceId, HttpServletResponse response) {

    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

    if (!permissionService.hasReadPermissionOnProcessInstance(SecurityUtils.getCurrentUserObject(), processInstance, processInstanceId)) {
      throw new NotFoundException("Process with id: " + processInstanceId + " does not exist or is not available for this user");
    }
    
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

    return getStartFormDefinition(processInstance.getProcessDefinitionId(), processDefinition, processInstance.getId());
  }

  public void deleteProcessInstance(String processInstanceId) {

    User currentUser = SecurityUtils.getCurrentUserObject();

    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .startedBy(String.valueOf(currentUser.getId())) // Permission
        .singleResult();

    if (processInstance == null) {
      throw new NotFoundException("Process with id: " + processInstanceId + " does not exist or is not started by this user");
    }

    if (processInstance.getEndTime() != null) {
      // Check if a hard delete of process instance is allowed
      if (!permissionService.canDeleteProcessInstance(currentUser, processInstance)) {
        throw new NotFoundException("Process with id: " + processInstanceId + " is already completed and can't be deleted");
      }

      // Delete cascade behavior in separate service to share a single transaction for all nested service-calls
      processInstanceService.deleteProcessInstance(processInstanceId);

    } else {
      runtimeService.deleteProcessInstance(processInstanceId, "Cancelled by " + SecurityUtils.getCurrentUserId());
    }
  }
  
  protected FormDefinition getStartFormDefinition(String processDefinitionId, ProcessDefinitionEntity processDefinition, String processInstanceId) {
    FormDefinition formDefinition = null;
    BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
    Process process = bpmnModel.getProcessById(processDefinition.getKey());
    FlowElement startElement = process.getInitialFlowElement();
    if (startElement instanceof StartEvent) {
      StartEvent startEvent = (StartEvent) startElement;
      if (StringUtils.isNotEmpty(startEvent.getFormKey())) {
        formDefinition = formService.getCompletedTaskFormDefinitionByKeyAndParentDeploymentId(startEvent.getFormKey(), 
            processDefinition.getDeploymentId(), null, processInstanceId, null, processDefinition.getTenantId());
      }
    }
    
    return formDefinition;
  }

}
