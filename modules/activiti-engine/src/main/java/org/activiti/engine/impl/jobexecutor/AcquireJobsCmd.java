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
package org.activiti.engine.impl.jobexecutor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.JobEntity;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Nick Burch
 */
public class AcquireJobsCmd implements Command<AcquiredJobs> {

  private final JobExecutor jobExecutor;

  public AcquireJobsCmd(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }
  
  public AcquiredJobs execute(CommandContext commandContext) {
    String lockOwner = jobExecutor.getLockOwner();
    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();
    int maxJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();
    
    
    AcquiredJobs acquiredJobs = new AcquiredJobs();
    List<JobEntity> jobs = commandContext
      .getRuntimeSession()
      .findNextJobsToExecute(new Page(0, maxJobsPerAcquisition));
    for (JobEntity job: jobs) {
      List<String> jobIds = new ArrayList<String>();

      if (job != null) {
        job.setLockOwner(lockOwner);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(ClockUtil.getCurrentTime());
        gregorianCalendar.add(Calendar.MILLISECOND, lockTimeInMillis);
        job.setLockExpirationTime(gregorianCalendar.getTime());
        jobIds.add(job.getId());
        if (job.isExclusive()) {
          // TODO acquire other exclusive jobs for the same process instance.
        }
      }
      
      acquiredJobs.addJobIds(jobIds);
    }
    
    return acquiredJobs;
  }
}
