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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.jobexecutor.TimerEventHandler;
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;


public class BoundaryTimerEventActivityBehavior extends BoundaryEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected TimerEventDefinition timerEventDefinition;

  public BoundaryTimerEventActivityBehavior(TimerEventDefinition timerEventDefinition, boolean interrupting) {
    super(interrupting);
    this.timerEventDefinition = timerEventDefinition;
  }

  @Override
  public void execute(DelegateExecution execution) {

    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    if (!(execution.getCurrentFlowElement() instanceof BoundaryEvent)) {
      throw new ActivitiException("Programmatic error: " + this.getClass() + " should not be used for anything else than a boundary event");
    }

    JobManager jobManager = Context.getCommandContext().getJobManager();
    TimerJobEntity timerJob = jobManager.createTimerJob(timerEventDefinition, interrupting, executionEntity, TriggerTimerEventJobHandler.TYPE,
        TimerEventHandler.createConfiguration(execution.getCurrentActivityId(), timerEventDefinition.getEndDate(), timerEventDefinition.getCalendarName()));
    if (timerJob != null) {
      jobManager.scheduleTimerJob(timerJob);
    }
  }

}
