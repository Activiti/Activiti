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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AcquiredJobEntities;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;


public class AcquireJobsCmd implements Command<AcquiredJobEntities> {

  private final AsyncExecutor asyncExecutor;

  public AcquireJobsCmd(AsyncExecutor asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }

  public AcquiredJobEntities execute(CommandContext commandContext) {
    AcquiredJobEntities acquiredJobs = new AcquiredJobEntities();
    List<JobEntity> jobs = commandContext.getJobEntityManager().findJobsToExecute(new Page(0, asyncExecutor.getMaxAsyncJobsDuePerAcquisition()));

    for (JobEntity job : jobs) {
      lockJob(commandContext, job, asyncExecutor.getAsyncJobLockTimeInMillis());
      acquiredJobs.addJob(job);
    }

    return acquiredJobs;
  }

  protected void lockJob(CommandContext commandContext, JobEntity job, int lockTimeInMillis) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockOwner(asyncExecutor.getLockOwner());
    job.setLockExpirationTime(gregorianCalendar.getTime());
  }
}
