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
package org.activiti.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.asyncexecutor.FindExpiredJobsCmd;
import org.activiti.engine.impl.asyncexecutor.ResetExpiredJobsCmd;
import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.test.Deployment;

/**
 */
public class ResetExpiredJobsTest extends PluggableActivitiTestCase {

  @Deployment
  public void testResetExpiredJobs() {

    // This first tets 'mimics' the async executor:
    // first the job will be acquired via the lowlevel API instead of using threads
    // and then they will be reset, using the lowlevel API again.

    Date startOfTestTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startOfTestTime);

    // Starting process instance will make one job ready
    runtimeService.startProcessInstanceByKey("myProcess");
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    // Running the 'reset expired' logic should have no effect now
    int expiredJobsPagesSize = processEngineConfiguration.getAsyncExecutorResetExpiredJobsPageSize();
    List<JobEntity> expiredJobs = managementService.executeCommand(new FindExpiredJobsCmd(expiredJobsPagesSize));
    assertThat(expiredJobs).hasSize(0);
    assertJobDetails(false);

    // Run the acquire logic. This should lock the job
    managementService.executeCommand(new AcquireJobsCmd(processEngineConfiguration.getAsyncExecutor()));
    assertJobDetails(true);

    // Running the 'reset expired' logic should have no effect, the lock time is not yet passed
    expiredJobs = managementService.executeCommand(new FindExpiredJobsCmd(expiredJobsPagesSize));
    assertThat(expiredJobs).hasSize(0);
    assertJobDetails(true);

    // Move clock to past the lock time
    Date newDate = new Date(startOfTestTime.getTime() + processEngineConfiguration.getAsyncExecutor().getAsyncJobLockTimeInMillis() + 10000);
    processEngineConfiguration.getClock().setCurrentTime(newDate);

    // Running the reset logic should now reset the lock
    expiredJobs = managementService.executeCommand(new FindExpiredJobsCmd(expiredJobsPagesSize));
    assertThat(expiredJobs.size() > 0).isTrue();

    List<String> jobIds = new ArrayList<String>();
    for (JobEntity jobEntity : expiredJobs) {
      jobIds.add(jobEntity.getId());
    }

    managementService.executeCommand(new ResetExpiredJobsCmd(jobIds));
    assertJobDetails(false);

    // And it can be re-acquired
    managementService.executeCommand(new AcquireJobsCmd(processEngineConfiguration.getAsyncExecutor()));
    assertJobDetails(true);

    // Start two new process instances, those jobs should not be locked
    runtimeService.startProcessInstanceByKey("myProcess");
    runtimeService.startProcessInstanceByKey("myProcess");
    assertThat(managementService.createJobQuery().count()).isEqualTo(3);
    assertJobDetails(true);

    List<Job> unlockedJobs = managementService.createJobQuery().unlocked().list();
    assertThat(unlockedJobs).hasSize(2);
    for (Job job : unlockedJobs) {
      JobEntity jobEntity = (JobEntity) job;
      assertThat(jobEntity.getLockOwner()).isNull();
      assertThat(jobEntity.getLockExpirationTime()).isNull();
    }
  }

  protected void assertJobDetails(boolean locked) {
    JobQuery jobQuery = managementService.createJobQuery();

    if (locked) {
      jobQuery.locked();
    }

    Job job = jobQuery.singleResult();
    assertThat(job).isInstanceOf(JobEntity.class);
    JobEntity jobEntity = (JobEntity) job;

    if (locked) {
      assertThat(jobEntity.getLockOwner()).isNotNull();
      assertThat(jobEntity.getLockExpirationTime()).isNotNull();
    } else {
      assertThat(jobEntity.getLockOwner()).isNull();
      assertThat(jobEntity.getLockExpirationTime()).isNull();
    }
  }


}
