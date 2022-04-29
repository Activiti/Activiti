/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;

/**
 * Send job cancelled event and delete job
 *

 */
public class CancelJobsCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  List<String> jobIds;

  public CancelJobsCmd(List<String> jobIds) {
    this.jobIds = jobIds;
  }

  public CancelJobsCmd(String jobId) {
    this.jobIds = new ArrayList<String>();
    jobIds.add(jobId);
  }

  public Void execute(CommandContext commandContext) {
    JobEntity jobToDelete = null;
    for (String jobId : jobIds) {
      jobToDelete = commandContext.getJobEntityManager().findById(jobId);

      if (jobToDelete != null) {
        // When given job doesn't exist, ignore
        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
          commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, jobToDelete));
        }

        commandContext.getJobEntityManager().delete(jobToDelete);

      } else {
        TimerJobEntity timerJobToDelete = commandContext.getTimerJobEntityManager().findById(jobId);

        if (timerJobToDelete != null) {
          // When given job doesn't exist, ignore
          if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            commandContext.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, timerJobToDelete));
          }

          commandContext.getTimerJobEntityManager().delete(timerJobToDelete);
        }
      }
    }
    return null;
  }
}
