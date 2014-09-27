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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AcquiredJobs;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;


/**
 * @author Nick Burch
 * @author Daniel Meyer
 */
public class AcquireJobsCmd implements Command<AcquiredJobs> {

  private final JobExecutor jobExecutor;

  public AcquireJobsCmd(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }
  
  public AcquiredJobs execute(CommandContext commandContext) {
    
    String lockOwner = jobExecutor.getLockOwner();
    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();
    int maxNonExclusiveJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();
    
    AcquiredJobs acquiredJobs = new AcquiredJobs();
    List<JobEntity> jobs = commandContext
      .getJobEntityManager()
      .findNextJobsToExecute(new Page(0, maxNonExclusiveJobsPerAcquisition));

    List<String> jobIds = new ArrayList<String>();
    List<JobEntity> finalJobs = new ArrayList<JobEntity>();
    for (JobEntity job: jobs) {
      if (job != null && !acquiredJobs.contains(job.getId())) {
        if (job.isExclusive() && job.getProcessInstanceId() != null) {
          // wait to get exclusive jobs within 100ms
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {}
          
          // acquire all exclusive jobs in the same process instance
          // (includes the current job)
          List<JobEntity> exclusiveJobs = commandContext.getJobEntityManager()
            .findExclusiveJobsToExecute(job.getProcessInstanceId());
          for (JobEntity exclusiveJob : exclusiveJobs) {
            if (exclusiveJob != null) {
              jobIds.add(exclusiveJob.getId());
              finalJobs.add(exclusiveJob);
            }
          }
          
        } else {
          jobIds.add(job.getId());
          finalJobs.add(job);
        }
      }
    }
    
    if (jobIds.size() > 0) {
      commandContext.getJobEntityManager().updateJobLock(lockOwner, getLockExpirationTime(
          commandContext, lockTimeInMillis), jobIds);
      
      acquiredJobs.addJobIdBatch(finalJobs);
    }

    return acquiredJobs;
  }
  
  protected Date getLockExpirationTime(CommandContext commandContext, int lockTimeInMillis) {    
    GregorianCalendar gregorianCalendar = new GregorianCalendar();
    gregorianCalendar.setTime(commandContext.getProcessEngineConfiguration().getClock().getCurrentTime());
    gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
    return gregorianCalendar.getTime(); 
  }
}
