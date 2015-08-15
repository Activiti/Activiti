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

import java.util.HashMap;
import java.util.Map;

import org.activiti.compatibility.wrapper.Activiti5DeploymentWrapper;
import org.activiti.compatibility.wrapper.Activiti5ProcessDefinitionWrapper;
import org.activiti.compatibility.wrapper.Activiti5ProcessInstanceWrapper;
import org.activiti.engine.ActivitiClassLoadingException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.cmd.AddIdentityLinkCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.repository.DeploymentBuilderImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.delegate.event.ActivitiEventListener;
import org.activiti5.engine.impl.asyncexecutor.AsyncJobUtil;
import org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti5.engine.impl.cmd.AddEventListenerCommand;
import org.activiti5.engine.impl.cmd.RemoveEventListenerCommand;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti5.engine.repository.DeploymentBuilder;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DefaultActiviti5CompatibilityHandler implements Activiti5CompatibilityHandler {

  protected DefaultProcessEngineFactory processEngineFactory;
  protected ProcessEngine processEngine;
  
  public ProcessDefinition getProcessDefinition(final String processDefinitionId) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    ProcessDefinitionEntity processDefinitionEntity = processEngineConfig.getCommandExecutor().execute(new Command<ProcessDefinitionEntity>() {

      @Override
      public ProcessDefinitionEntity execute(CommandContext commandContext) {
        return processEngineConfig.getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);
      }
    });
    
    Activiti5ProcessDefinitionWrapper wrapper = null;
    if (processDefinitionEntity != null) {
      wrapper = new Activiti5ProcessDefinitionWrapper(processDefinitionEntity);
    }
    return wrapper;
  }
  
  public ProcessDefinition getProcessDefinitionByKey(final String processDefinitionKey) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    ProcessDefinitionEntity processDefinitionEntity = processEngineConfig.getCommandExecutor().execute(new Command<ProcessDefinitionEntity>() {

      @Override
      public ProcessDefinitionEntity execute(CommandContext commandContext) {
        return processEngineConfig.getDeploymentManager().findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
      }
    });
    
    Activiti5ProcessDefinitionWrapper wrapper = null;
    if (processDefinitionEntity != null) {
      wrapper = new Activiti5ProcessDefinitionWrapper(processDefinitionEntity);
    }
    return wrapper;
  }
  
  public Deployment deploy(DeploymentBuilderImpl activiti6DeploymentBuilder) {
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
  }
  
  public void deleteDeployment(String deploymentId, boolean cascade) {
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    processEngineConfig.getRepositoryService().deleteDeployment(deploymentId, cascade);
  }
  
  public ProcessInstance startProcessInstance(String processDefinitionKey, String processDefinitionId, 
      Map<String, Object> variables, String businessKey, String tenantId, String processInstanceName) {
    
    if (Authentication.getAuthenticatedUserId() != null) {
      org.activiti5.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    }
    
    try {
      org.activiti5.engine.runtime.ProcessInstance activiti5ProcessInstance 
          = getProcessEngine().getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
      return new Activiti5ProcessInstanceWrapper(activiti5ProcessInstance);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    getProcessEngine().getRuntimeService().deleteProcessInstance(processInstanceId, deleteReason);
  }
  
  public void completeTask(TaskEntity taskEntity, Map<String, Object> variables, boolean localScope) {
    if (Authentication.getAuthenticatedUserId() != null) {
      org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    }
    try {
      getProcessEngine().getTaskService().complete(taskEntity.getId(), variables, localScope);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public ProcessInstance submitStartFormData(String processDefinitionId, String businessKey, Map<String, String> properties) {
    if (Authentication.getAuthenticatedUserId() != null) {
      org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    }
    try {
      return new Activiti5ProcessInstanceWrapper(getProcessEngine().getFormService().submitStartFormData(processDefinitionId, businessKey, properties));
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
      return null;
    }
  }
  
  public void submitTaskFormData(String taskId, Map<String, String> properties) {
    if (Authentication.getAuthenticatedUserId() != null) {
      org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(Authentication.getAuthenticatedUserId());
    }
    try {
      getProcessEngine().getFormService().submitTaskFormData(taskId, properties);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void saveTask(TaskEntity task) {
    org.activiti5.engine.impl.persistence.entity.TaskEntity activiti5Task = convertToActiviti5TaskEntity(task);
    getProcessEngine().getTaskService().saveTask(activiti5Task);
  }
  
  public void addIdentityLink(String taskId, String identityId, int identityIdType, String identityType) {
    if (identityIdType == AddIdentityLinkCmd.IDENTITY_USER) {
      getProcessEngine().getTaskService().addUserIdentityLink(taskId, identityId, identityType);
    } else if (identityIdType == AddIdentityLinkCmd.IDENTITY_GROUP) {
      getProcessEngine().getTaskService().addGroupIdentityLink(taskId, identityId, identityType);
    }
  }
  
  public void trigger(String executionId, Map<String, Object> processVariables) {
    try {
      getProcessEngine().getRuntimeService().signal(executionId, processVariables);
    } catch (org.activiti5.engine.ActivitiException e) {
      handleActivitiException(e);
    }
  }
  
  public void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables) {
    try {
      getProcessEngine().getRuntimeService().signalEventReceived(signalName, executionId, processVariables);
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
    final org.activiti5.engine.impl.persistence.entity.JobEntity activity5Job = convertToActiviti5JobEntity((JobEntity) job);
    processEngineConfig.getCommandExecutor().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        activity5Job.execute(commandContext);
        return null;
      }
    });
  }
  
  public void executeJobWithLockAndRetry(JobEntity job) {
    if (job == null) return;
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    final org.activiti5.engine.impl.persistence.entity.JobEntity activity5Job = convertToActiviti5JobEntity((JobEntity) job);
    AsyncJobUtil.executeJob(activity5Job, processEngineConfig.getCommandExecutor());
  }
  
  public void addEventListener(Object listener) {
    if (listener instanceof ActivitiEventListener == false) {
      throw new ActivitiException("listener does not implement org.activiti5.engine.delegate.event.ActivitiEventListener interface");
    }
    
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    processEngineConfig.getCommandExecutor().execute(new AddEventListenerCommand((ActivitiEventListener) listener));
  }
  
  public void removeEventListener(Object listener) {
    if (listener instanceof ActivitiEventListener == false) {
      throw new ActivitiException("listener does not implement org.activiti5.engine.delegate.event.ActivitiEventListener interface");
    }
    
    final ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    processEngineConfig.getCommandExecutor().execute(new RemoveEventListenerCommand((ActivitiEventListener) listener));
  }
  
  public Object getRawProcessConfiguration() {
    return getProcessEngine().getProcessEngineConfiguration();
  }
  
  public Object getRawCommandExecutor() {
    ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) getProcessEngine().getProcessEngineConfiguration();
    return processEngineConfig.getCommandExecutor();
  }
  
  public Object getRawClock() {
    return getProcessEngine().getProcessEngineConfiguration().getClock();
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
    if (task.getInitialAssignee() != null) {
      activiti5Task.setInitialAssignee(task.getInitialAssignee());
    }
    activiti5Task.setCategoryWithoutCascade(task.getCategory());
    activiti5Task.setCreateTime(task.getCreateTime());
    activiti5Task.setDelegationStateString(task.getDelegationStateString());
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
  
  protected org.activiti5.engine.impl.persistence.entity.JobEntity convertToActiviti5JobEntity(JobEntity job) {
    org.activiti5.engine.impl.persistence.entity.JobEntity activity5Job = null;
    if (job instanceof TimerEntity) {
      TimerEntity timer = (TimerEntity) job;
      org.activiti5.engine.impl.persistence.entity.TimerEntity tempTimer = new org.activiti5.engine.impl.persistence.entity.TimerEntity();
      tempTimer.setEndDate(timer.getEndDate());
      tempTimer.setRepeat(timer.getRepeat());
      activity5Job = tempTimer;
      
    } else if (job instanceof MessageEntity) {
      org.activiti5.engine.impl.persistence.entity.MessageEntity tempTimer = new org.activiti5.engine.impl.persistence.entity.MessageEntity();
      activity5Job = tempTimer;
    }
    
    activity5Job.setDuedate(job.getDuedate());
    activity5Job.setExclusive(job.isExclusive());
    activity5Job.setExecutionId(job.getExecutionId());
    activity5Job.setId(job.getId());
    activity5Job.setJobHandlerConfiguration(job.getJobHandlerConfiguration());
    activity5Job.setJobHandlerType(job.getJobHandlerType());
    activity5Job.setJobType(job.getJobType());
    activity5Job.setLockExpirationTime(job.getLockExpirationTime());
    activity5Job.setLockOwner(job.getLockOwner());
    activity5Job.setProcessDefinitionId(job.getProcessDefinitionId());
    activity5Job.setProcessInstanceId(job.getProcessInstanceId());
    activity5Job.setRetries(job.getRetries());
    activity5Job.setRevision(job.getRevision());
    activity5Job.setTenantId(job.getTenantId());
    
    return activity5Job;
  }
  
  protected void handleActivitiException(org.activiti5.engine.ActivitiException e) {
    if (e instanceof org.activiti5.engine.delegate.BpmnError) {
      org.activiti5.engine.delegate.BpmnError activiti5BpmnError = (org.activiti5.engine.delegate.BpmnError) e;
      throw new BpmnError(activiti5BpmnError.getErrorCode(), activiti5BpmnError.getMessage());
    } else if (e instanceof org.activiti5.engine.ActivitiClassLoadingException) {
      throw new ActivitiClassLoadingException(e.getMessage(), e.getCause());
    } else {
      if (e.getCause() instanceof org.activiti5.engine.ActivitiClassLoadingException) {
        throw new ActivitiException(e.getMessage(), new ActivitiClassLoadingException(e.getCause().getMessage(), e.getCause().getCause()));
      } else if (e.getCause() instanceof org.activiti5.engine.impl.javax.el.PropertyNotFoundException) {
        throw new ActivitiException(e.getMessage(), new PropertyNotFoundException(e.getCause().getMessage(), e.getCause().getCause()));
      } else {
        throw new ActivitiException(e.getMessage(), e.getCause());
      }
    }
  }
   
}
