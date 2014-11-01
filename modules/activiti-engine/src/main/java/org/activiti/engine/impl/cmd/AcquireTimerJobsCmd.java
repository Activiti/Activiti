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
package org.activiti.engine.impl.cmd;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AcquiredJobEntities;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;


/**
 * @author Tijs Rademakers
 */
public class AcquireTimerJobsCmd implements Command<AcquiredJobEntities> {

  private final String lockOwner;
  private final int lockTimeInMillis;
  private final int maxJobsPerAcquisition;

  public AcquireTimerJobsCmd(String lockOwner, int lockTimeInMillis, int maxJobsPerAcquisition) {
    this.lockOwner = lockOwner;
    this.lockTimeInMillis = lockTimeInMillis;
    this.maxJobsPerAcquisition = maxJobsPerAcquisition;
  }
  
  public AcquiredJobEntities execute(CommandContext commandContext) {
    AcquiredJobEntities acquiredJobs = new AcquiredJobEntities();
    List<JobEntity> jobs = commandContext
      .getJobEntityManager()
      .findNextTimerJobsToExecute(new Page(0, maxJobsPerAcquisition));

    for (JobEntity job: jobs) {
      if (job != null && !acquiredJobs.contains(job.getId())) {
        lockJob(commandContext, job, lockOwner, lockTimeInMillis);
        acquiredJobs.addJob(job);
      }
    }

    return acquiredJobs;
  }

  protected void lockJob(CommandContext commandContext, JobEntity job, String lockOwner, int lockTimeInMillis) {    
    job.setLockOwner(lockOwner);
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockExpirationTime(gregorianCalendar.getTime());    
  }
}