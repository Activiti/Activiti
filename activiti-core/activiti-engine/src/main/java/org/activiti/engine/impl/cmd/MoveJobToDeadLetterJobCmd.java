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
import org.activiti.engine.impl.persistence.entity.AbstractJobEntity;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class MoveJobToDeadLetterJobCmd implements Command<DeadLetterJobEntity>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(MoveJobToDeadLetterJobCmd.class);

  protected String jobId;

  public MoveJobToDeadLetterJobCmd(String jobId) {
    this.jobId = jobId;
  }

  public DeadLetterJobEntity execute(CommandContext commandContext) {

    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId and job is null");
    }

    AbstractJobEntity job = commandContext.getTimerJobEntityManager().findById(jobId);
    if (job == null) {
      job = commandContext.getJobEntityManager().findById(jobId);
    }

    if (job == null) {
      throw new JobNotFoundException(jobId);
    }

    if (log.isDebugEnabled()) {
      log.debug("Moving job to deadletter job table {}", job.getId());
    }

    DeadLetterJobEntity deadLetterJob = commandContext.getJobManager().moveJobToDeadLetterJob(job);

    return deadLetterJob;
  }

  public String getJobId() {
    return jobId;
  }

}
