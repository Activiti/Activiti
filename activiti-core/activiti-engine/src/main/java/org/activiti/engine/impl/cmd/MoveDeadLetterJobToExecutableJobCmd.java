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
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MoveDeadLetterJobToExecutableJobCmd implements Command<JobEntity>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = LoggerFactory.getLogger(MoveDeadLetterJobToExecutableJobCmd.class);

  protected String jobId;
  protected int retries;

  public MoveDeadLetterJobToExecutableJobCmd(String jobId, int retries) {
    this.jobId = jobId;
    this.retries = retries;
  }

  public JobEntity execute(CommandContext commandContext) {

    if (jobId == null) {
      throw new ActivitiIllegalArgumentException("jobId and job is null");
    }

    DeadLetterJobEntity job = commandContext.getDeadLetterJobEntityManager().findById(jobId);
    if (job == null) {
      throw new JobNotFoundException(jobId);
    }

    if (log.isDebugEnabled()) {
      log.debug("Moving deadletter job to executable job table {}", job.getId());
    }

    return commandContext.getJobManager().moveDeadLetterJobToExecutableJob(job, retries);
  }

  public String getJobId() {
    return jobId;
  }

}
