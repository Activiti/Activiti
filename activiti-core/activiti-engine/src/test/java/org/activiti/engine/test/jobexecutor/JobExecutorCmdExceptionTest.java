/**
 *
 */
package org.activiti.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

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

    Job job = managementService.createJobQuery().singleResult();
    assertThat(job.getRetries()).isEqualTo(3);

    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }

    job = managementService.createTimerJobQuery().singleResult();
    assertThat(job.getRetries()).isEqualTo(2);

    try {
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }

    job = managementService.createTimerJobQuery().singleResult();
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

    Job job = managementService.createJobQuery().singleResult();
    assertThat(job.getRetries()).isEqualTo(3);

    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }

    job = managementService.createTimerJobQuery().singleResult();
    assertThat(job.getRetries()).isEqualTo(2);

    try {
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }

    job = managementService.createTimerJobQuery().singleResult();
    assertThat(job.getRetries()).isEqualTo(1);

    try {
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }

    job = managementService.createDeadLetterJobQuery().singleResult();
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
