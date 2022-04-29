/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
/**
 *
 */
package org.activiti.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;

/**
 */
public class JobExecutorCmdExceptionTest extends PluggableActivitiTestCase {

  protected TweetExceptionHandler tweetExceptionHandler = new TweetExceptionHandler();

  private CommandExecutor commandExecutor;

  public void setUp() throws Exception {
    processEngineConfiguration.getJobHandlers().put(tweetExceptionHandler.getType(), tweetExceptionHandler);
    this.commandExecutor = processEngineConfiguration.getCommandExecutor();
  }

  public void tearDown() throws Exception {
    processEngineConfiguration.getJobHandlers().remove(tweetExceptionHandler.getType());
  }

  public void testJobCommandsWith2Exceptions() {
    commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        JobEntity message = createTweetExceptionMessage();
        commandContext.getJobManager().scheduleAsyncJob(message);
        return message.getId();
      }
    });

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        Job job = managementService.createJobQuery().singleResult();
        assertThat(job.getRetries()).isEqualTo(3);
        managementService.executeJob(job.getId());
      });

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        Job job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getRetries()).isEqualTo(2);

        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
      });

    Job job = managementService.createTimerJobQuery().singleResult();
    assertThat(job.getRetries()).isEqualTo(1);

    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());
  }

  public void testJobCommandsWith3Exceptions() {
    tweetExceptionHandler.setExceptionsRemaining(3);

    commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        JobEntity message = createTweetExceptionMessage();
        commandContext.getJobManager().scheduleAsyncJob(message);
        return message.getId();
      }
    });


    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        Job job = managementService.createJobQuery().singleResult();
        assertThat(job.getRetries()).isEqualTo(3);
        managementService.executeJob(job.getId());
      });

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        Job job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getRetries()).isEqualTo(2);
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
      });

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> {
        Job job = managementService.createTimerJobQuery().singleResult();
        assertThat(job.getRetries()).isEqualTo(1);
        managementService.moveTimerToExecutableJob(job.getId());
        managementService.executeJob(job.getId());
      });

    Job job = managementService.createDeadLetterJobQuery().singleResult();
    assertThat(job).isNotNull();

    managementService.deleteDeadLetterJob(job.getId());
  }

  protected JobEntity createTweetExceptionMessage() {
    JobEntity message = new JobEntityImpl();
    message.setJobType(JobEntity.JOB_TYPE_MESSAGE);
    message.setRetries(3);
    message.setJobHandlerType("tweet-exception");
    return message;
  }
}
