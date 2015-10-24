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

import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cmd.CancelJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.util.TimerUtil;
import org.activiti.engine.runtime.Job;
import org.apache.commons.collections.CollectionUtils;
import org.activiti.bpmn.model.Process;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages timers for newly-deployed process definitions and their previous versions.
 */
public class TimerManager {
  protected void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
    List<Job> jobsToDelete = null;

    if (processDefinition.getTenantId() != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
      jobsToDelete = Context.getCommandContext().getJobEntityManager().findJobsByTypeAndProcessDefinitionKeyAndTenantId(
          TimerStartEventJobHandler.TYPE, processDefinition.getKey(), processDefinition.getTenantId());
    } else {
      jobsToDelete = Context.getCommandContext().getJobEntityManager()
          .findJobsByTypeAndProcessDefinitionKeyNoTenantId(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
    }

    if (jobsToDelete != null) {
      for (Job job :jobsToDelete) {
        new CancelJobsCmd(job.getId()).execute(Context.getCommandContext());
      }
    }
  }
  
  protected void scheduleTimers(ProcessDefinitionEntity processDefinition, Process process) {
    List<TimerEntity> timers = getTimerDeclarations(processDefinition, process);
    for (TimerEntity timer : timers) {
      Context.getCommandContext().getJobEntityManager().schedule(timer);
    }
  }
  
  protected List<TimerEntity> getTimerDeclarations(ProcessDefinitionEntity processDefinition, Process process) {
    List<TimerEntity> timers = new ArrayList<TimerEntity>();
    if (CollectionUtils.isNotEmpty(process.getFlowElements())) {
      for (FlowElement element : process.getFlowElements()) {
        if (element instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) element;
          if (CollectionUtils.isNotEmpty(startEvent.getEventDefinitions())) {
            EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
            if (eventDefinition instanceof TimerEventDefinition) {
              TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
              TimerEntity timer = TimerUtil.createTimerEntityForTimerEventDefinition(timerEventDefinition, false, null, TimerStartEventJobHandler.TYPE,
                  TimerEventHandler.createConfiguration(startEvent.getId(), timerEventDefinition.getEndDate()));

              if (timer != null) {
                timer.setProcessDefinitionId(processDefinition.getId());

                if (processDefinition.getTenantId() != null) {
                  timer.setTenantId(processDefinition.getTenantId());
                }
                timers.add(timer);
              }

            }
          }
        }
      }
    }

    return timers;
  }
}

