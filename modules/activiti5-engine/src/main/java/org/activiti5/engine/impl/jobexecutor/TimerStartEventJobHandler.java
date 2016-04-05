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
package org.activiti5.engine.impl.jobexecutor;

import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.ProcessEngineConfiguration;
import org.activiti5.engine.delegate.event.ActivitiEventType;
import org.activiti5.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti5.engine.impl.cmd.StartProcessInstanceCmd;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.persistence.entity.JobEntity;
import org.activiti5.engine.repository.ProcessDefinition;
import org.activiti5.engine.runtime.ProcessInstance;
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

    String nestedActivityId = TimerEventHandler.getActivityIdFromConfiguration(configuration);

    ProcessDefinition processDefinition = null;
    if (job.getTenantId() == null || ProcessEngineConfiguration.NO_TENANT_ID.equals(job.getTenantId())) {
      processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKey(nestedActivityId);
    } else {
    	processDefinition = deploymentManager.findDeployedLatestProcessDefinitionByKeyAndTenantId(nestedActivityId, job.getTenantId());
    }
    
    if (processDefinition == null) {
    	throw new ActivitiException("Could not find process definition needed for timer start event");
    }
    
    try {
      if (!deploymentManager.isProcessDefinitionSuspended(processDefinition.getId())) {
        if (commandContext.getEventDispatcher().isEnabled()) {
          commandContext.getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TIMER_FIRED, job));
        }

        new StartProcessInstanceCmd<ProcessInstance>(nestedActivityId, null, null, null, job.getTenantId()).execute(commandContext);
      } else {
        log.debug("ignoring timer of suspended process definition {}", processDefinition.getName());
      }
    } catch (RuntimeException e) {
      log.error("exception during timer execution", e);
      throw e;
    } catch (Exception e) {
      log.error("exception during timer execution", e);
      throw new ActivitiException("exception during timer execution: " + e.getMessage(), e);
    }
  }
}
