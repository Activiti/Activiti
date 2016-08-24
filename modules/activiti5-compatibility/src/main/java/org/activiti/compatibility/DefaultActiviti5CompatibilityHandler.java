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

package org.activiti.compatibility;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.PropertyNotFoundException;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.MapExceptionEntry;
import org.activiti.compatibility.wrapper.Activiti5AttachmentWrapper;
import org.activiti.compatibility.wrapper.Activiti5CommentWrapper;
import org.activiti.compatibility.wrapper.Activiti5DeploymentWrapper;
import org.activiti.compatibility.wrapper.Activiti5ProcessInstanceWrapper;
import org.activiti.engine.ActivitiClassLoadingException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.cmd.AddIdentityLinkCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.ProcessEngineConfiguration;
import org.activiti5.engine.impl.asyncexecutor.AsyncJobUtil;
import org.activiti5.engine.impl.bpmn.behavior.BpmnActivityBehavior;
import org.activiti5.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti5.engine.impl.bpmn.helper.ErrorThrowingEventListener;
import org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti5.engine.impl.cmd.ExecuteJobsCmd;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti5.engine.impl.scripting.ScriptingEngines;
import org.activiti5.engine.repository.DeploymentBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DefaultActiviti5CompatibilityHandler implements Activiti5CompatibilityHandler {

  protected DefaultProcessEngineFactory processEngineFactory;
  protected ProcessEngine processEngine;
  
  public ProcessDefinition getProcessDefinition(final String processDefinitionId) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    ProcessDefinition processDefinitionEntity = processEngineConfig.getCommandExecutor().execute(new Command<ProcessDefinition>() {

      @Override
      public ProcessDefinition execute(CommandContext commandContext) {
        return processEngineConfig.getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);
      }
    });
    
    return processDefinitionEntity;
  }
  
  public ProcessDefinition getProcessDefinitionByKey(final String processDefinitionKey) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    ProcessDefinition processDefinition = processEngineConfig.getCommandExecutor().execute(new Command<ProcessDefinition>() {

      @Override
      public ProcessDefinition execute(CommandContext commandContext) {
        return processEngineConfig.getDeploymentManager().findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      }
    });
    
    return processDefinition;
  }
  
  public org.activiti.bpmn.model.Process getProcessDefinitionProcessObject(final String processDefinitionId) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    org.activiti.bpmn.model.Process process = processEngineConfig.getCommandExecutor().execute(new Command<org.activiti.bpmn.model.Process>() {

      @Override
      public org.activiti.bpmn.model.Process execute(CommandContext commandContext) {
        org.activiti.bpmn.model.Process process = null;
        DeploymentManager deploymentManager = processEngineConfig.getDeploymentManager();
        ProcessDefinition processDefinition = deploymentManager.findDeployedProcessDefinitionById(processDefinitionId);
        if (processDefinition != null) {
          BpmnModel bpmnModel = deploymentManager.getBpmnModelById(processDefinitionId);
          if (bpmnModel != null) {
            process = bpmnModel.getProcessById(processDefinition.getKey());
          }
        }
        return process;
      }
    });
    
    return process;
  }
  
  public BpmnModel getProcessDefinitionBpmnModel(final String processDefinitionId) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    return processEngineConfig.getDeploymentManager().getBpmnModelById(processDefinitionId);
  }
  
  public void addCandidateStarter(String processDefinitionId, String userId, String groupId) {
    try {
      if (userId != null) {
        getProcessEngine().getRepositoryService().addCandidateStarterUser(processDefinitionId, userId);
      } else {
        getProcessEngine().getRepositoryService().addCandidateStarterGroup(processDefinitionId, groupId);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public ObjectNode getProcessDefinitionInfo(String processDefinitionId) {
    try {
      return getProcessEngine().getDynamicBpmnService().getProcessDefinitionInfo(processDefinitionId);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public ProcessDefinitionCacheEntry resolveProcessDefinition(final ProcessDefinition processDefinition) {
    try {
      final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
      ProcessDefinitionCacheEntry cacheEntry = processEngineConfig.getCommandExecutor().execute(new Command<ProcessDefinitionCacheEntry>() {

        @Override
        public ProcessDefinitionCacheEntry execute(CommandContext commandContext) {
          return commandContext.getProcessEngineConfiguration().getDeploymentManager().resolveProcessDefinition(processDefinition);
        }
      });
      
      return cacheEntry;
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public boolean isProcessDefinitionSuspended(String processDefinitionId) {
    try {
      return getProcessEngine().getRepositoryService().isProcessDefinitionSuspended(processDefinitionId);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return false;
    }
  }
  
  public void deleteCandidateStarter(String processDefinitionId, String userId, String groupId) {
    try {
      if (userId != null) {
        getProcessEngine().getRepositoryService().deleteCandidateStarterUser(processDefinitionId, userId);
      } else {
        getProcessEngine().getRepositoryService().deleteCandidateStarterGroup(processDefinitionId, groupId);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void suspendProcessDefinition(String processDefinitionId, String processDefinitionKey, boolean suspendProcessInstances, Date suspensionDate, String tenantId) {
    try {
      if (processDefinitionId != null) {
        getProcessEngine().getRepositoryService().suspendProcessDefinitionById(processDefinitionId, suspendProcessInstances, suspensionDate);
      } else {
        getProcessEngine().getRepositoryService().suspendProcessDefinitionByKey(processDefinitionKey, suspendProcessInstances, suspensionDate, tenantId);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void activateProcessDefinition(String processDefinitionId, String processDefinitionKey, boolean activateProcessInstances, Date activationDate, String tenantId) {
    try {
      if (processDefinitionId != null) {
        getProcessEngine().getRepositoryService().activateProcessDefinitionById(processDefinitionId, activateProcessInstances, activationDate);
      } else {
        getProcessEngine().getRepositoryService().activateProcessDefinitionByKey(processDefinitionKey, activateProcessInstances, activationDate, tenantId);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void setProcessDefinitionCategory(String processDefinitionId, String category) {
    try {
      getProcessEngine().getRepositoryService().setProcessDefinitionCategory(processDefinitionId, category);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public Deployment deploy(DeploymentBuilderImpl activiti6DeploymentBuilder) {
    try {
      DeploymentBuilder deploymentBuilder = getProcessEngine().getRepositoryService().createDeployment();
      
      // Copy settings 
      
      deploymentBuilder.name(activiti6DeploymentBuilder.getDeployment().getName());
      deploymentBuilder.category(activiti6DeploymentBuilder.getDeployment().getCategory());
      deploymentBuilder.tenantId(activiti6DeploymentBuilder.getDeployment().getTenantId());
      
      // Copy flags 
      
      if (!activiti6DeploymentBuilder.isBpmn20XsdValidationEnabled()) {
        deploymentBuilder.disableSchemaValidation();
      }
      
      if (!activiti6DeploymentBuilder.isProcessValidationEnabled()) {
        deploymentBuilder.disableBpmnValidation();
      }
      
      if (activiti6DeploymentBuilder.isDuplicateFilterEnabled()) {
        deploymentBuilder.enableDuplicateFiltering();
      }
      
      if (activiti6DeploymentBuilder.getProcessDefinitionsActivationDate() != null) {
        deploymentBuilder.activateProcessDefinitionsOn(activiti6DeploymentBuilder.getProcessDefinitionsActivationDate());
      }
  
      // Copy resources
      DeploymentEntity activiti6DeploymentEntity = activiti6DeploymentBuilder.getDeployment();
      Map<String, org.activiti5.engine.impl.persistence.entity.ResourceEntity> activiti5Resources = new HashMap<String, org.activiti5.engine.impl.persistence.entity.ResourceEntity>();
      for (String resourceKey : activiti6DeploymentEntity.getResources().keySet()) {
        ResourceEntity activiti6ResourceEntity = activiti6DeploymentEntity.getResources().get(resourceKey);
        
        org.activiti5.engine.impl.persistence.entity.ResourceEntity activiti5ResourceEntity = new org.activiti5.engine.impl.persistence.entity.ResourceEntity();
        activiti5ResourceEntity.setName(activiti6ResourceEntity.getName());
        activiti5ResourceEntity.setBytes(activiti6ResourceEntity.getBytes());
        activiti5Resources.put(resourceKey, activiti5ResourceEntity);
      }
  
      org.activiti5.engine.impl.persistence.entity.DeploymentEntity activiti5DeploymentEntity 
        = ((org.activiti5.engine.impl.repository.DeploymentBuilderImpl) deploymentBuilder).getDeployment();
      activiti5DeploymentEntity.setResources(activiti5Resources);
      
      return new Activiti5DeploymentWrapper(deploymentBuilder.deploy());
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void setDeploymentCategory(String deploymentId, String category) {
    try {
      getProcessEngine().getRepositoryService().setDeploymentCategory(deploymentId, category);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void changeDeploymentTenantId(String deploymentId, String newTenantId) {
    try {
      getProcessEngine().getRepositoryService().changeDeploymentTenantId(deploymentId, newTenantId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteDeployment(String deploymentId, boolean cascade) {
    try {
      final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
      processEngineConfig.getRepositoryService().deleteDeployment(deploymentId, cascade);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public ProcessInstance startProcessInstance(String processDefinitionKey, String processDefinitionId, 
      Map<String, Object> variables, String businessKey, String tenantId, String processInstanceName) {
    
    org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    
    try {
      org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance = null;
      if (tenantId != null) { 
        activiti5ProcessInstance = getProcessEngine().getRuntimeService()
            .startProcessInstanceByKeyAndTenantId(processDefinitionKey, businessKey, variables, tenantId);
      } else {
        activiti5ProcessInstance = getProcessEngine().getRuntimeService()
            .startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
      }
      
      if (processInstanceName != null) {
        getProcessEngine().getRuntimeService().setProcessInstanceName(activiti5ProcessInstance.getId(), processInstanceName);
        ((ExecutionEntity) activiti5ProcessInstance).setName(processInstanceName);
      }
      
      return new Activiti5ProcessInstanceWrapper(activiti5ProcessInstance);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> variables, String businessKey, String tenantId) {
    
    try {
      org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance = null;
      if (tenantId != null) { 
        activiti5ProcessInstance = getProcessEngine().getRuntimeService()
            .startProcessInstanceByMessageAndTenantId(messageName, businessKey, variables, tenantId);
      } else {
        activiti5ProcessInstance = getProcessEngine().getRuntimeService()
            .startProcessInstanceByMessage(messageName, businessKey, variables);
      }
      
      return new Activiti5ProcessInstanceWrapper(activiti5ProcessInstance);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public Object getExecutionVariable(String executionId, String variableName, boolean isLocal) {
    try {
      if (isLocal) {
        return getProcessEngine().getRuntimeService().getVariableLocal(executionId, variableName);
      } else {
        return getProcessEngine().getRuntimeService().getVariable(executionId, variableName);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public VariableInstance getExecutionVariableInstance(String executionId, String variableName, boolean isLocal) {
    try {
      if (isLocal) {
        return getProcessEngine().getRuntimeService().getVariableInstanceLocal(executionId, variableName);
      } else {
        return getProcessEngine().getRuntimeService().getVariableInstance(executionId, variableName);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public Map<String, Object> getExecutionVariables(String executionId, Collection<String> variableNames, boolean isLocal) {
    try {
      if (isLocal) {
        return getProcessEngine().getRuntimeService().getVariablesLocal(executionId, variableNames);
      } else {
        return getProcessEngine().getRuntimeService().getVariables(executionId, variableNames);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public Map<String, VariableInstance> getExecutionVariableInstances(String executionId, Collection<String> variableNames, boolean isLocal) {
    try {
      if (isLocal) {
        return getProcessEngine().getRuntimeService().getVariableInstancesLocal(executionId, variableNames);
      } else {
        return getProcessEngine().getRuntimeService().getVariableInstances(executionId, variableNames);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void setExecutionVariables(String executionId, Map<String, ? extends Object> variables, boolean isLocal) {
    try {
      if (isLocal) {
        getProcessEngine().getRuntimeService().setVariablesLocal(executionId, variables);
      } else {
        getProcessEngine().getRuntimeService().setVariables(executionId, variables);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void removeExecutionVariables(String executionId, Collection<String> variableNames, boolean isLocal) {
    try {
      if (isLocal) {
        getProcessEngine().getRuntimeService().removeVariablesLocal(executionId, variableNames);
      } else {
        getProcessEngine().getRuntimeService().removeVariables(executionId, variableNames);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void updateBusinessKey(String processInstanceId, String businessKey) {
    try {
      getProcessEngine().getRuntimeService().updateBusinessKey(processInstanceId, businessKey);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void suspendProcessInstance(String processInstanceId) {
    try {
      getProcessEngine().getRuntimeService().suspendProcessInstanceById(processInstanceId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void activateProcessInstance(String processInstanceId) {
    try {
      getProcessEngine().getRuntimeService().activateProcessInstanceById(processInstanceId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    try {
      getProcessEngine().getRuntimeService().deleteProcessInstance(processInstanceId, deleteReason);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteHistoricProcessInstance(String processInstanceId) {
    try {
      getProcessEngine().getHistoryService().deleteHistoricProcessInstance(processInstanceId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void addIdentityLinkForProcessInstance(String processInstanceId, String userId, String groupId, String identityLinkType) {
    try {
      if (userId != null) {
        getProcessEngine().getRuntimeService().addUserIdentityLink(processInstanceId, userId, identityLinkType);
      } else {
        getProcessEngine().getRuntimeService().addGroupIdentityLink(processInstanceId, groupId, identityLinkType);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteIdentityLinkForProcessInstance(String processInstanceId, String userId, String groupId, String identityLinkType) {
    try {
      if (userId != null) {
        getProcessEngine().getRuntimeService().deleteUserIdentityLink(processInstanceId, userId, identityLinkType);
      } else {
        getProcessEngine().getRuntimeService().deleteGroupIdentityLink(processInstanceId, groupId, identityLinkType);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void completeTask(TaskEntity taskEntity, Map<String, Object> variables, boolean localScope) {
    org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    try {
      getProcessEngine().getTaskService().complete(taskEntity.getId(), variables, localScope);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void claimTask(String taskId, String userId) {
    org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    try {
      getProcessEngine().getTaskService().claim(taskId, userId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void setTaskVariables(String taskId, Map<String, ? extends Object> variables, boolean isLocal) {
    try {
      if (isLocal) {
        getProcessEngine().getTaskService().setVariablesLocal(taskId, variables);
      } else {
        getProcessEngine().getTaskService().setVariables(taskId, variables);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void removeTaskVariables(String taskId, Collection<String> variableNames, boolean isLocal) {
    try {
      if (isLocal) {
        getProcessEngine().getTaskService().removeVariablesLocal(taskId, variableNames);
      } else {
        getProcessEngine().getTaskService().removeVariables(taskId, variableNames);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void setTaskDueDate(String taskId, Date dueDate) {
    try {
      getProcessEngine().getTaskService().setDueDate(taskId, dueDate);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void setTaskPriority(String taskId, int priority) {
    try {
      getProcessEngine().getTaskService().setPriority(taskId, priority);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteTask(String taskId, String deleteReason, boolean cascade) {
    try {
      if (deleteReason != null) {
        getProcessEngine().getTaskService().deleteTask(taskId, deleteReason);
      } else {
        getProcessEngine().getTaskService().deleteTask(taskId, cascade);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteHistoricTask(String taskId) {
    try {
      getProcessEngine().getHistoryService().deleteHistoricTaskInstance(taskId);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public StartFormData getStartFormData(String processDefinitionId) {
    try {
      return getProcessEngine().getFormService().getStartFormData(processDefinitionId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public String getFormKey(String processDefinitionId, String taskDefinitionKey) {
    try {
      if (taskDefinitionKey != null) {
        return getProcessEngine().getFormService().getTaskFormKey(processDefinitionId, taskDefinitionKey);
      } else {
        return getProcessEngine().getFormService().getStartFormKey(processDefinitionId);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public Object getRenderedStartForm(String processDefinitionId, String formEngineName) {
    try {
      return getProcessEngine().getFormService().getRenderedStartForm(processDefinitionId, formEngineName);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public ProcessInstance submitStartFormData(String processDefinitionId, String businessKey, Map<String, String> properties) {
    org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    try {
      return new Activiti5ProcessInstanceWrapper(getProcessEngine().getFormService().submitStartFormData(processDefinitionId, businessKey, properties));
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void submitTaskFormData(String taskId, Map<String, String> properties, boolean completeTask) {
    org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    try {
      if (completeTask) {
        getProcessEngine().getFormService().submitTaskFormData(taskId, properties);
      } else {
        getProcessEngine().getFormService().saveFormData(taskId, properties);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void saveTask(TaskEntity task) {
    try {
      org.activiti5.engine.impl.persistence.entity.TaskEntity activiti5Task = convertToActiviti5TaskEntity(task);
      getProcessEngine().getTaskService().saveTask(activiti5Task);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void addIdentityLink(String taskId, String identityId, int identityIdType, String identityType) {
    if (identityIdType == AddIdentityLinkCmd.IDENTITY_USER) {
      getProcessEngine().getTaskService().addUserIdentityLink(taskId, identityId, identityType);
    } else if (identityIdType == AddIdentityLinkCmd.IDENTITY_GROUP) {
      getProcessEngine().getTaskService().addGroupIdentityLink(taskId, identityId, identityType);
    }
  }
  
  public void deleteIdentityLink(String taskId, String userId, String groupId, String identityLinkType) {
    if (userId != null) {
      getProcessEngine().getTaskService().deleteUserIdentityLink(taskId, userId, identityLinkType);
    } else {
      getProcessEngine().getTaskService().deleteGroupIdentityLink(taskId, groupId, identityLinkType);
    }
  }
  
  public Comment addComment(String taskId, String processInstanceId, String type, String message) {
    try {
      return new Activiti5CommentWrapper(getProcessEngine().getTaskService().addComment(taskId, processInstanceId, type, message));
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void deleteComment(String commentId, String taskId, String processInstanceId) {
    try {
      if (commentId != null) {
        getProcessEngine().getTaskService().deleteComment(commentId);
      } else {
        getProcessEngine().getTaskService().deleteComments(taskId, processInstanceId);
      }
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content, String url) {
    try {
      org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
      if (content != null) {
        return new Activiti5AttachmentWrapper(getProcessEngine().getTaskService().
            createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, content));
      } else {
        return new Activiti5AttachmentWrapper(getProcessEngine().getTaskService().
            createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, url));
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void saveAttachment(Attachment attachment) {
    try {
      org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
      org.activiti5.engine.task.Attachment activiti5Attachment = getProcessEngine().getTaskService().getAttachment(attachment.getId());
      activiti5Attachment.setName(attachment.getName());
      activiti5Attachment.setDescription(attachment.getDescription());
      activiti5Attachment.setTime(attachment.getTime());
      getProcessEngine().getTaskService().saveAttachment(activiti5Attachment);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void deleteAttachment(String attachmentId) {
    try {
      org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
      getProcessEngine().getTaskService().deleteAttachment(attachmentId);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void trigger(String executionId, Map<String, Object> processVariables) {
    try {
      getProcessEngine().getRuntimeService().signal(executionId, processVariables);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables, boolean async) {
    try {
      if (async == false) {
        getProcessEngine().getRuntimeService().messageEventReceived(messageName, executionId, processVariables);
      } else {
        getProcessEngine().getRuntimeService().messageEventReceivedAsync(messageName, executionId);
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables, boolean async, String tenantId) {
    try {
      if (tenantId != null) {
        if (async) {
          getProcessEngine().getRuntimeService().signalEventReceivedAsyncWithTenantId(signalName, tenantId);
        } else {
          getProcessEngine().getRuntimeService().signalEventReceivedWithTenantId(signalName, processVariables, tenantId);
        }
      } else {
        if (async) {
          getProcessEngine().getRuntimeService().signalEventReceivedAsync(signalName, executionId);
        } else {
          getProcessEngine().getRuntimeService().signalEventReceived(signalName, executionId, processVariables);
        }
      }
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void signalEventReceived(final SignalEventSubscriptionEntity signalEventSubscriptionEntity, final Object payload, final boolean async) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    processEngineConfig.getCommandExecutor().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        org.activiti5.engine.impl.persistence.entity.SignalEventSubscriptionEntity activiti5SignalEvent = new org.activiti5.engine.impl.persistence.entity.SignalEventSubscriptionEntity();
        activiti5SignalEvent.setId(signalEventSubscriptionEntity.getId());
        activiti5SignalEvent.setExecutionId(signalEventSubscriptionEntity.getExecutionId());
        activiti5SignalEvent.setActivityId(signalEventSubscriptionEntity.getActivityId());
        activiti5SignalEvent.setEventName(signalEventSubscriptionEntity.getEventName());
        activiti5SignalEvent.setEventType(signalEventSubscriptionEntity.getEventType());
        activiti5SignalEvent.setConfiguration(signalEventSubscriptionEntity.getConfiguration());
        activiti5SignalEvent.setProcessDefinitionId(signalEventSubscriptionEntity.getProcessDefinitionId());
        activiti5SignalEvent.setProcessInstanceId(signalEventSubscriptionEntity.getProcessInstanceId());
        activiti5SignalEvent.setTenantId(signalEventSubscriptionEntity.getTenantId());
        activiti5SignalEvent.setRevision(signalEventSubscriptionEntity.getRevision());
        activiti5SignalEvent.eventReceived(payload, async);
        return null;
      }
    });
    
  }
  
  public void executeJob(Job job) {
    if (job == null) return;
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    final org.activiti5.engine.impl.persistence.entity.JobEntity activiti5Job = convertToActiviti5JobEntity((JobEntity) job, processEngineConfig);
    processEngineConfig.getCommandExecutor().execute(new ExecuteJobsCmd(activiti5Job));
  }
  
  public void executeJobWithLockAndRetry(Job job) {
    if (job == null) return;
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    org.activiti5.engine.impl.persistence.entity.JobEntity activity5Job = null;
    if (job instanceof org.activiti5.engine.impl.persistence.entity.JobEntity) {
      activity5Job = (org.activiti5.engine.impl.persistence.entity.JobEntity) job;
    } else {
      activity5Job = convertToActiviti5JobEntity((JobEntity) job, processEngineConfig);
    }
    AsyncJobUtil.executeJob(activity5Job, processEngineConfig.getCommandExecutor());
  }
  
  public void handleFailedJob(Job job, Throwable exception) {
    if (job == null) return;
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    final org.activiti5.engine.impl.persistence.entity.JobEntity activity5Job = convertToActiviti5JobEntity((JobEntity) job, processEngineConfig);
    AsyncJobUtil.handleFailedJob(activity5Job, exception, processEngineConfig.getCommandExecutor());
  }
  
  public void deleteJob(String jobId) {
    try {
      getProcessEngine().getManagementService().deleteJob(jobId);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void leaveExecution(DelegateExecution execution) {
    try {
      BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();
      bpmnActivityBehavior.performDefaultOutgoingBehavior((ActivityExecution) execution);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void propagateError(BpmnError bpmnError, DelegateExecution execution) {
    try {
      org.activiti5.engine.delegate.BpmnError activiti5BpmnError = new org.activiti5.engine.delegate.BpmnError(bpmnError.getErrorCode(), bpmnError.getMessage());
      ErrorPropagation.propagateError(activiti5BpmnError, (ActivityExecution) execution);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public boolean mapException(Exception camelException, DelegateExecution execution, List<MapExceptionEntry> mapExceptions) {
    try {
      return ErrorPropagation.mapException(camelException, (ExecutionEntity) execution, mapExceptions);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return false;
    }
  }
  
  public Map<String, Object> getVariables(ProcessInstance processInstance) {
    org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance = ((Activiti5ProcessInstanceWrapper) processInstance).getRawObject();
    return ((ExecutionEntity) activiti5ProcessInstance).getVariables();
  }
  
  public Object getScriptingEngineValue(String payloadExpressionValue, String languageValue, DelegateExecution execution) {
    try {
      final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
      ScriptingEngines scriptingEngines = processEngineConfig.getScriptingEngines();
      return scriptingEngines.evaluate(payloadExpressionValue, languageValue, execution);
      
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void throwErrorEvent(ActivitiEvent event) {
    ErrorThrowingEventListener eventListener = new ErrorThrowingEventListener();
    eventListener.onEvent(event);
  }
  
  public void setClock(Clock clock) {
    ProcessEngineConfiguration processEngineConfig = getProcessEngine().getProcessEngineConfiguration();
    if (processEngineConfig.getClock() == null) {
      getProcessEngine().getProcessEngineConfiguration().setClock(clock);
    } else {
      Clock activiti5Clock = processEngineConfig.getClock();
      activiti5Clock.setCurrentCalendar(clock.getCurrentCalendar());
    }
  }
  
  public void resetClock() {
    ProcessEngineConfiguration processEngineConfig = getProcessEngine().getProcessEngineConfiguration();
    if (processEngineConfig.getClock() != null) {
      processEngineConfig.getClock().reset();
    }
  }
  
  public Object getRawProcessEngine() {
    return getProcessEngine();
  }
  
  public Object getRawProcessConfiguration() {
    return getProcessEngine().getProcessEngineConfiguration();
  }
  
  public Object getRawCommandExecutor() {
    ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    return processEngineConfig.getCommandExecutor();
  }
  
  public Object getCamelContextObject(String camelContextValue) {
    throw new ActivitiException("Getting the Camel context is not support in this engine configuration");
  }
  
  protected ProcessEngine getProcessEngine() {
    if (processEngine == null) {
      synchronized (this) {
        if (processEngine == null) {
          processEngine = getProcessEngineFactory().buildProcessEngine(Context.getProcessEngineConfiguration());
        }
      }
    }
    return processEngine;
  }

  public DefaultProcessEngineFactory getProcessEngineFactory() {
    if (processEngineFactory == null) {
      processEngineFactory = new DefaultProcessEngineFactory();
    }
    return processEngineFactory;
  }

  public void setProcessEngineFactory(DefaultProcessEngineFactory processEngineFactory) {
    this.processEngineFactory = processEngineFactory;
  }
  
  protected org.activiti5.engine.impl.persistence.entity.TaskEntity convertToActiviti5TaskEntity(TaskEntity task) {
    org.activiti5.engine.impl.persistence.entity.TaskEntity activiti5Task = new org.activiti5.engine.impl.persistence.entity.TaskEntity();
    activiti5Task.setAssigneeWithoutCascade(task.getAssignee());
    activiti5Task.setInitialAssignee( ((TaskEntityImpl) task).getOriginalAssignee());
    activiti5Task.setCategoryWithoutCascade(task.getCategory());
    activiti5Task.setCreateTime(task.getCreateTime());
    activiti5Task.setDelegationStateString(((TaskEntityImpl)task).getDelegationStateString());
    activiti5Task.setDescriptionWithoutCascade(task.getDescription());
    activiti5Task.setDueDateWithoutCascade(task.getDueDate());
    activiti5Task.setExecutionId(task.getExecutionId());
    activiti5Task.setFormKeyWithoutCascade(task.getFormKey());
    activiti5Task.setId(task.getId());
    activiti5Task.setNameWithoutCascade(task.getName());
    activiti5Task.setOwnerWithoutCascade(task.getOwner());
    activiti5Task.setParentTaskIdWithoutCascade(task.getParentTaskId());
    activiti5Task.setPriorityWithoutCascade(task.getPriority());
    activiti5Task.setProcessDefinitionId(task.getProcessDefinitionId());
    activiti5Task.setProcessInstanceId(task.getProcessInstanceId());
    activiti5Task.setRevision(task.getRevision());
    activiti5Task.setTaskDefinitionKeyWithoutCascade(task.getTaskDefinitionKey());
    activiti5Task.setTenantId(task.getTenantId());
    return activiti5Task;
  }
  
  protected org.activiti5.engine.impl.persistence.entity.JobEntity convertToActiviti5JobEntity(final JobEntity job, final ProcessEngineConfigurationImpl processEngineConfiguration) {
    org.activiti5.engine.impl.persistence.entity.JobEntity activity5Job = new org.activiti5.engine.impl.persistence.entity.JobEntity();
    activity5Job.setJobType(job.getJobType());
    activity5Job.setDuedate(job.getDuedate());
    activity5Job.setExclusive(job.isExclusive());
    activity5Job.setExecutionId(job.getExecutionId());
    activity5Job.setId(job.getId());
    activity5Job.setJobHandlerConfiguration(job.getJobHandlerConfiguration());
    activity5Job.setJobHandlerType(job.getJobHandlerType());
    activity5Job.setEndDate(job.getEndDate());
    activity5Job.setRepeat(job.getRepeat());
    activity5Job.setProcessDefinitionId(job.getProcessDefinitionId());
    activity5Job.setProcessInstanceId(job.getProcessInstanceId());
    activity5Job.setRetries(job.getRetries());
    activity5Job.setRevision(job.getRevision());
    activity5Job.setTenantId(job.getTenantId());
    activity5Job.setExceptionMessage(job.getExceptionMessage());
    return activity5Job;
  }
  
  protected void handleActivitiException(org.activiti5.engine.ActivitiException e) {
    if (e instanceof org.activiti5.engine.delegate.BpmnError) {
      org.activiti5.engine.delegate.BpmnError activiti5BpmnError = (org.activiti5.engine.delegate.BpmnError) e;
      throw new BpmnError(activiti5BpmnError.getErrorCode(), activiti5BpmnError.getMessage());
      
    } else if (e instanceof org.activiti5.engine.ActivitiClassLoadingException) {
      throw new ActivitiClassLoadingException(e.getMessage(), e.getCause());
      
    } else if (e instanceof org.activiti5.engine.ActivitiObjectNotFoundException) {
      org.activiti5.engine.ActivitiObjectNotFoundException activiti5ObjectNotFoundException = (org.activiti5.engine.ActivitiObjectNotFoundException) e;
      throw new ActivitiObjectNotFoundException(activiti5ObjectNotFoundException.getMessage(), 
          activiti5ObjectNotFoundException.getObjectClass(), activiti5ObjectNotFoundException.getCause());
      
    } else if (e instanceof org.activiti5.engine.ActivitiOptimisticLockingException) {
      throw new ActivitiOptimisticLockingException(e.getMessage());
      
    } else if (e instanceof org.activiti5.engine.ActivitiIllegalArgumentException) {
      throw new ActivitiIllegalArgumentException(e.getMessage(), e.getCause());
      
    } else {
      if (e.getCause() instanceof org.activiti5.engine.ActivitiClassLoadingException) {
        throw new ActivitiException(e.getMessage(), new ActivitiClassLoadingException(e.getCause().getMessage(), e.getCause().getCause()));
      } else if (e.getCause() instanceof org.activiti5.engine.impl.javax.el.PropertyNotFoundException) {
        throw new ActivitiException(e.getMessage(), new PropertyNotFoundException(e.getCause().getMessage(), e.getCause().getCause()));
      } else if (e.getCause() instanceof org.activiti5.engine.ActivitiException) {
        throw new ActivitiException(e.getMessage(), new ActivitiException(e.getCause().getMessage(), e.getCause().getCause()));
      } else {
        throw new ActivitiException(e.getMessage(), e.getCause());
      }
    }
  }
   
}
