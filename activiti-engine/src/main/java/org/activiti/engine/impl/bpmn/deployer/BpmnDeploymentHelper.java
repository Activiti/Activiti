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
package org.activiti.engine.impl.bpmn.deployer;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.lang3.StringUtils;

/**
 * Methods for working with deployments.  Much of the actual work of {@link BpmnDeployer} is
 * done by orchestrating the different pieces of work this class does; by having them here,
 * we allow other deployers to make use of them.   
 */
public class BpmnDeploymentHelper  {
  
  protected TimerManager timerManager;
  protected EventSubscriptionManager eventSubscriptionManager;
  
  /**
   * Verifies that no two process definitions share the same key, to prevent database unique
   * index violation.
   * 
   * @throws ActivitiException if any two processes have the same key
   */
  public void verifyProcessDefinitionsDoNotShareKeys(
      Collection<ProcessDefinitionEntity> processDefinitions) {
    Set<String> keySet = new LinkedHashSet<String>();
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (keySet.contains(processDefinition.getKey())) {
        throw new ActivitiException(
            "The deployment contains process definitions with the same key (process id attribute), this is not allowed");
      }
      keySet.add(processDefinition.getKey());
    }
  }

  /**
   * Updates all the process definition entities to match the deployment's values for tenant,
   * engine version, and deployment id.
   */
  public void copyDeploymentValuesToProcessDefinitions(DeploymentEntity deployment, 
      List<ProcessDefinitionEntity> processDefinitions) {
    String engineVersion = deployment.getEngineVersion();
    String tenantId = deployment.getTenantId();
    String deploymentId = deployment.getId();

    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      
      // Backwards compatibility
      if (engineVersion != null) {
        processDefinition.setEngineVersion(engineVersion);
      }

      // process definition inherits the tenant id
      if (tenantId != null) {
        processDefinition.setTenantId(tenantId); 
      }

      processDefinition.setDeploymentId(deploymentId);
    }
  }

  /**
   * Updates all the process definition entities to have the correct resource names.
   */
  public void setResourceNamesOnProcessDefinitions(ParsedDeployment parsedDeployment) {
    for (ProcessDefinitionEntity processDefinition : parsedDeployment.getAllProcessDefinitions()) {
      String resourceName = parsedDeployment.getResourceForProcessDefinition(processDefinition).getName();
      processDefinition.setResourceName(resourceName);
    }
  }

  /**
   * Gets the most recent persisted process definition that matches this one for tenant and key.
   * If none is found, returns null.  This method assumes that the tenant and key are properly
   * set on the process definition entity.
   */
  public ProcessDefinitionEntity getMostRecentVersionOfProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String key = processDefinition.getKey();
    String tenantId = processDefinition.getTenantId();
    ProcessDefinitionEntityManager processDefinitionManager 
      = Context.getCommandContext().getProcessEngineConfiguration().getProcessDefinitionEntityManager();

    ProcessDefinitionEntity existingDefinition = null;

    if (tenantId != null && !tenantId.equals(ProcessEngineConfiguration.NO_TENANT_ID)) {
      existingDefinition = processDefinitionManager.findLatestProcessDefinitionByKeyAndTenantId(key, tenantId);
    } else {
      existingDefinition = processDefinitionManager.findLatestProcessDefinitionByKey(key);
    }

    return existingDefinition;
  }

  /**
   * Gets the persisted version of the already-deployed process definition.  Note that this is
   * different from {@link #getMostRecentVersionOfProcessDefinition} as it looks specifically for
   * a process definition that is already persisted and attached to a particular deployment,
   * rather than the latest version across all deployments.
   */
  public ProcessDefinitionEntity getPersistedInstanceOfProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String deploymentId = processDefinition.getDeploymentId();
    if (StringUtils.isEmpty(processDefinition.getDeploymentId())) {
      throw new IllegalStateException("Provided process definition must have a deployment id.");
    }

    ProcessDefinitionEntityManager processDefinitionManager 
      = Context.getCommandContext().getProcessEngineConfiguration().getProcessDefinitionEntityManager();
    ProcessDefinitionEntity persistedProcessDefinition = null;
    if (processDefinition.getTenantId() == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
      persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKey(deploymentId, processDefinition.getKey());
    } else {
      persistedProcessDefinition = processDefinitionManager.findProcessDefinitionByDeploymentAndKeyAndTenantId(deploymentId, processDefinition.getKey(), processDefinition.getTenantId());
    }

    return persistedProcessDefinition;
  }

  /**
   * Updates all timers and events for the process definition.  This removes obsolete message and signal
   * subscriptions and timers, and adds new ones.
   */
  public void updateTimersAndEvents(ProcessDefinitionEntity processDefinition, 
      ProcessDefinitionEntity previousProcessDefinition, ParsedDeployment parsedDeployment) {
    
    Process process = parsedDeployment.getProcessModelForProcessDefinition(processDefinition);
    BpmnModel bpmnModel = parsedDeployment.getBpmnModelForProcessDefinition(processDefinition);

    eventSubscriptionManager.removeObsoleteMessageEventSubscriptions(previousProcessDefinition);
    eventSubscriptionManager.addMessageEventSubscriptions(processDefinition, process, bpmnModel);

    eventSubscriptionManager.removeObsoleteSignalEventSubScription(previousProcessDefinition);
    eventSubscriptionManager.addSignalEventSubscriptions(Context.getCommandContext(), processDefinition, process, bpmnModel);

    timerManager.removeObsoleteTimers(processDefinition);
    timerManager.scheduleTimers(processDefinition, process);
  }

  enum ExpressionType {
    USER, GROUP
  }

  /**
   * @param processDefinition
   */
  public void addAuthorizationsForNewProcessDefinition(Process process, ProcessDefinitionEntity processDefinition) {
    CommandContext commandContext = Context.getCommandContext();
    
    addAuthorizationsFromIterator(commandContext, process.getCandidateStarterUsers(), processDefinition, ExpressionType.USER);
    addAuthorizationsFromIterator(commandContext, process.getCandidateStarterGroups(), processDefinition, ExpressionType.GROUP);
  }
  
  protected void addAuthorizationsFromIterator(CommandContext commandContext, List<String> expressions, 
      ProcessDefinitionEntity processDefinition, ExpressionType expressionType) {
    
    if (expressions != null) {
      Iterator<String> iterator = expressions.iterator();
      while (iterator.hasNext()) {
        @SuppressWarnings("cast")
        String expression = iterator.next();
        IdentityLinkEntity identityLink = commandContext.getIdentityLinkEntityManager().create();
        identityLink.setProcessDef(processDefinition);
        if (expressionType.equals(ExpressionType.USER)) {
          identityLink.setUserId(expression);
        } else if (expressionType.equals(ExpressionType.GROUP)) {
          identityLink.setGroupId(expression);
        }
        identityLink.setType(IdentityLinkType.CANDIDATE);
        commandContext.getIdentityLinkEntityManager().insert(identityLink);
      }
    }
    
  }

  public TimerManager getTimerManager() {
    return timerManager;
  }

  public void setTimerManager(TimerManager timerManager) {
    this.timerManager = timerManager;
  }

  public EventSubscriptionManager getEventSubscriptionManager() {
    return eventSubscriptionManager;
  }

  public void setEventSubscriptionManager(EventSubscriptionManager eventSubscriptionManager) {
    this.eventSubscriptionManager = eventSubscriptionManager;
  }
}

