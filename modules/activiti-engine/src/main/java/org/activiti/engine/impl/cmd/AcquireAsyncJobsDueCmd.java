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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AcquiredJobs;
import org.activiti.engine.impl.persistence.entity.JobEntity;


/**
 * @author Tijs Rademakers
 */
public class AcquireAsyncJobsDueCmd implements Command<AcquiredJobs> {

  private final AsyncExecutor asyncExecutor;

  public AcquireAsyncJobsDueCmd(AsyncExecutor asyncExecutor) {
    this.asyncExecutor = asyncExecutor;
  }
  
  public AcquiredJobs execute(CommandContext commandContext) {
    AcquiredJobs acquiredJobs = new AcquiredJobs();
    List<JobEntity> jobs = commandContext
      .getJobEntityManager()
      .findAsyncJobsDueToExecute(new Page(0, 10));

    List<String> jobIds = new ArrayList<String>();
    for (JobEntity job: jobs) {
      lockJob(commandContext, job, 60000);
      jobIds.add(job.getId());
      asyncExecutor.executeAsyncJob(job);
    }
    
    acquiredJobs.addJobIdBatch(jobIds);
    return acquiredJobs;
  }

  protected void lockJob(CommandContext commandContext, JobEntity job, int lockTimeInMillis) {
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    job.setLockExpirationTime(gregorianCalendar.getTime());    
  }
}