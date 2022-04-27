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

package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MoveTimerToExecutableJobCmd implements Command<JobEntity>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(MoveTimerToExecutableJobCmd.class);

  protected String jobId;

  public MoveTimerToExecutableJobCmd(String jobId) {
    this.jobId = jobId;
  }

  public JobEntity execute(CommandContext commandContext) {

    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId and job is null");
    }

    TimerJobEntity timerJob = commandContext.getTimerJobEntityManager().findById(jobId);

    if (timerJob == null) {
      throw new JobNotFoundException(jobId);
    }

    if (log.isDebugEnabled()) {
      log.debug("Executing timer job {}", timerJob.getId());
    }

    return commandContext.getJobManager().moveTimerJobToExecutableJob(timerJob);
  }

  public String getJobId() {
    return jobId;
  }

}
