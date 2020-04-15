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

import java.util.Date;

import org.activiti.engine.impl.asyncexecutor.AcquiredTimerJobEntities;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.cmd.ExecuteAsyncJobCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.runtime.Job;

/**

 */
public class JobExecutorCmdHappyTest extends JobExecutorTestCase {

  public void testJobCommandsWithMessage() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();

    String jobId = commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        JobEntity message = createTweetMessage("i'm coding a test");
        commandContext.getJobManager().scheduleAsyncJob(message);
        return message.getId();
      }
    });

    Job job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    assertThat(job.getId()).isEqualTo(jobId);

    assertThat(tweetHandler.getMessages()).hasSize(0);

    managementService.executeJob(job.getId());

    assertThat(tweetHandler.getMessages().get(0)).isEqualTo("i'm coding a test");
    assertThat(tweetHandler.getMessages()).hasSize(1);
  }

  static final long SOME_TIME = 928374923546L;
  static final long SECOND = 1000;

  public void testJobCommandsWithTimer() {
    // clock gets automatically reset in LogTestCase.runTest
    processEngineConfiguration.getClock().setCurrentTime(new Date(SOME_TIME));

    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();

    String jobId = commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        TimerJobEntity timer = createTweetTimer("i'm coding a test", new Date(SOME_TIME + (10 * SECOND)));
        commandContext.getJobManager().scheduleTimerJob(timer);
        return timer.getId();
      }
    });

    AcquiredTimerJobEntities acquiredJobs = commandExecutor.execute(new AcquireTimerJobsCmd(asyncExecutor));
    assertThat(acquiredJobs.size()).isEqualTo(0);

    processEngineConfiguration.getClock().setCurrentTime(new Date(SOME_TIME + (20 * SECOND)));

    acquiredJobs = commandExecutor.execute(new AcquireTimerJobsCmd(asyncExecutor));
    assertThat(acquiredJobs.size()).isEqualTo(1);

    TimerJobEntity job = acquiredJobs.getJobs().iterator().next();

    assertThat(job.getId()).isEqualTo(jobId);

    assertThat(tweetHandler.getMessages()).hasSize(0);

    Job executableJob = managementService.moveTimerToExecutableJob(jobId);
    commandExecutor.execute(new ExecuteAsyncJobCmd(executableJob.getId()));

    assertThat(tweetHandler.getMessages().get(0)).isEqualTo("i'm coding a test");
    assertThat(tweetHandler.getMessages()).hasSize(1);
  }
}
