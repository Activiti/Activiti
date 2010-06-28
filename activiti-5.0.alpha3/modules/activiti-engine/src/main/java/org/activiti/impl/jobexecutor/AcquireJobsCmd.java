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
package org.activiti.impl.jobexecutor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.job.JobImpl;
import org.activiti.impl.persistence.PersistenceSession;


/**
 * @author Nick Burch
 */
public class AcquireJobsCmd implements Command<AcquiredJobs> {

  
  public AcquiredJobs execute(CommandContext commandContext) {
    PersistenceSession persistenceSession = commandContext.getPersistenceSession();
    JobExecutor jobExecutor = commandContext.getJobExecutor();
    
    String lockOwner = jobExecutor.getLockOwner();
    int lockTimeInMillis = jobExecutor.getLockTimeInMillis();
    int maxJobsPerAcquisition = jobExecutor.getMaxJobsPerAcquisition();
    
    
    AcquiredJobs acquiredJobs = new AcquiredJobs();
    List<JobImpl> jobs = persistenceSession.findNextJobsToExecute(maxJobsPerAcquisition);
    for (JobImpl job: jobs) {
      List<String> jobIds = new ArrayList<String>();

      if (job != null) {
        job.setLockOwner(lockOwner);
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
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
