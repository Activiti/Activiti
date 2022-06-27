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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.AbstractJobEntity;
import org.activiti.engine.runtime.Job;

/**


 */
public class GetJobExceptionStacktraceCmd implements Command<String>, Serializable {

  private static final long serialVersionUID = 1L;
  private String jobId;
  protected JobType jobType;

  public GetJobExceptionStacktraceCmd(String jobId, JobType jobType) {
    this.jobId = jobId;
    this.jobType = jobType;
  }

  public String execute(CommandContext commandContext) {
    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId is null");
    }

    AbstractJobEntity job = null;
    switch (jobType) {
    case ASYNC:
      job = commandContext.getJobEntityManager().findById(jobId);
      break;
    case TIMER:
      job = commandContext.getTimerJobEntityManager().findById(jobId);
      break;
    case SUSPENDED:
      job = commandContext.getSuspendedJobEntityManager().findById(jobId);
      break;
    case DEADLETTER:
      job = commandContext.getDeadLetterJobEntityManager().findById(jobId);
      break;
    }

    if (job == null) {
      throw new ActivitiObjectNotFoundException("No job found with id " + jobId, Job.class);
    }

    return job.getExceptionStacktrace();
  }

}
