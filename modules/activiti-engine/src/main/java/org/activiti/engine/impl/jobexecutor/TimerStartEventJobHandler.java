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
package org.activiti.engine.impl.jobexecutor;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimerStartEventJobHandler extends TimerEventHandler implements JobHandler {

  private static Logger log = LoggerFactory.getLogger(TimerStartEventJobHandler.class);

  public static final String TYPE = "timer-start-event";

  public String getType() {
    return TYPE;
  }
  
  public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
    
    DeploymentManager deploymentManager = Context
        .getProcessEngineConfiguration()
        .getDeploymentManager();
    
    if (TimerEventHandler.hasRealActivityId(configuration)) {
      startProcessInstanceWithInitialActivity(job, configuration, deploymentManager, commandContext);
    } else {
      startProcessDefinitionByKey(job, configuration, deploymentManager, commandContext);
    }
  }

  protected void startProcessInstanceWithInitialActivity(JobEntity job, String configuration, DeploymentManager deploymentManager, CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = deploymentManager.findDeployedProcessDefinitionById(job.getProcessDefinitionId());
    
    String activityId = getActivityIdFromConfiguration(configuration);
    ActivityImpl startActivity = processDefinition.findActivity(activityId);
    
    if (!deploymentManager.isProcessDefinitionSuspended(processDefinition.getId())) {
      dispatchTimerFiredEvent(job, commandContext);

      ExecutionEntity processInstance = processDefinition.createProcessInstance(null, startActivity);
      processInstance.start();
      
    } else {
      log.debug("Ignoring timer of suspended process definition {}", processDefinition.getId());
    }
    
  }

  protected void startProcessDefinitionByKey(JobEntity job, String configuration, DeploymentManager deploymentManager, CommandContext commandContext) {
    
    // it says getActivityId, but < 5.21, this would have the process definition key stored
    String processDefinitionKey = TimerEventHandler.getActivityIdFromConfiguration(configuration); 
    
    ProcessDefinition processDefinition = null;
    if (job.getTenantId() == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(job.getTenantId())) {
    		processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(processDefinitionKey);
    } else {
    	processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(processDefinitionKey, job.getTenantId());
    }
    
    if (processDefinition == null) {
    	throw new ActivitiException("Could not find process definition needed for timer start event");
    }
    
    try {
      if (!deploymentManager.isProcessDefinitionSuspended(processDefinition.getId())) {
        dispatchTimerFiredEvent(job, commandContext);
 
        new StartProcessInstanceCmd<ProcessInstance>(processDefinitionKey, null, null, null, job.getTenantId()).execute(commandContext);
      } else {
        log.debug("Ignoring timer of suspended process definition {}", processDefinition.getId());
      }
    } catch (RuntimeException e) {
      log.error("exception during timer execution", e);
      throw e;
    } catch (Exception e) {
      log.error("exception during timer execution", e);
      throw new ActivitiException("exception during timer execution: " + e.getMessage(), e);
    }
  }
  
  protected void dispatchTimerFiredEvent(JobEntity job,
      CommandContext commandContext) {
    if (commandContext.getEventDispatcher().isEnabled()) {
      commandContext.getEventDispatcher().dispatchEvent(
        ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TIMER_FIRED, job));
    }
  }

  
}
