/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.cmd.CancelJobsCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages timers for newly-deployed process definitions and their previous versions.
 */
public class TimerManager {

  protected void removeObsoleteTimers(ProcessDefinitionEntity processDefinition) {
    List<TimerJobEntity> jobsToDelete = null;

    if (processDefinition.getTenantId() != null && !ProcessEngineConfiguration.NO_TENANT_ID.equals(processDefinition.getTenantId())) {
      jobsToDelete = Context.getCommandContext().getTimerJobEntityManager().findJobsByTypeAndProcessDefinitionKeyAndTenantId(
          TimerStartEventJobHandler.TYPE, processDefinition.getKey(), processDefinition.getTenantId());
    } else {
      jobsToDelete = Context.getCommandContext().getTimerJobEntityManager()
          .findJobsByTypeAndProcessDefinitionKeyNoTenantId(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
    }

    if (jobsToDelete != null) {
      for (TimerJobEntity job :jobsToDelete) {
        new CancelJobsCmd(job.getId()).execute(Context.getCommandContext());
      }
    }
  }

  protected void removeExistingTimerStartEventJobs() {
    List<TimerJobEntity> timerStartJobs = Context.getCommandContext().getTimerJobEntityManager().
        findTimerStartEvents();
    if (timerStartJobs != null) {
        for (TimerJobEntity timerStartJob : timerStartJobs) {
            new CancelJobsCmd(timerStartJob.getId()).execute(Context.getCommandContext());
        }
    }
  }

  protected void scheduleTimers(ProcessDefinitionEntity processDefinition, Process process) {
    JobManager jobManager = Context.getCommandContext().getJobManager();
    List<TimerJobEntity> timers = getTimerDeclarations(processDefinition, process);
    for (TimerJobEntity timer : timers) {
      jobManager.scheduleTimerJob(timer);
    }
  }

  protected List<TimerJobEntity> getTimerDeclarations(ProcessDefinitionEntity processDefinition, Process process) {
    JobManager jobManager = Context.getCommandContext().getJobManager();
    List<TimerJobEntity> timers = new ArrayList<TimerJobEntity>();
    if (process != null && CollectionUtil.isNotEmpty(process.getFlowElements())) {
      for (FlowElement element : process.getFlowElements()) {
        if (element instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) element;
          if (CollectionUtil.isNotEmpty(startEvent.getEventDefinitions())) {
            EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
            if (eventDefinition instanceof TimerEventDefinition) {
              TimerEventDefinition timerEventDefinition = (TimerEventDefinition) eventDefinition;
              TimerJobEntity timerJob = jobManager.createTimerJob(timerEventDefinition, false, null, TimerStartEventJobHandler.TYPE,
                  TimerEventHandler.createConfiguration(startEvent.getId(), timerEventDefinition.getEndDate(), timerEventDefinition.getCalendarName()));

              if (timerJob != null) {
                timerJob.setProcessDefinitionId(processDefinition.getId());

                if (processDefinition.getTenantId() != null) {
                  timerJob.setTenantId(processDefinition.getTenantId());
                }
                timers.add(timerJob);
              }

            }
          }
        }
      }
    }

    return timers;
  }
}
