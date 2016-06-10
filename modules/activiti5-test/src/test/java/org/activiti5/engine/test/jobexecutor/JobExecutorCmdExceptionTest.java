/**
 * 
 */
package org.activiti5.engine.test.jobexecutor;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

/**
 * @author Tom Baeyens
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
    assertEquals(3, job.getRetries());
    
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createTimerJobQuery().singleResult();
    assertEquals(2, job.getRetries());
    
    managementService.moveTimerToExecutableJob(job.getId());
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createTimerJobQuery().singleResult();
    assertEquals(1, job.getRetries());
    
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
    assertEquals(3, job.getRetries());
    
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createTimerJobQuery().singleResult();
    assertEquals(2, job.getRetries());
    
    managementService.moveTimerToExecutableJob(job.getId());
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createTimerJobQuery().singleResult();
    assertEquals(1, job.getRetries());
    
    managementService.moveTimerToExecutableJob(job.getId());
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createDeadLetterJobQuery().singleResult();
    assertEquals(0, job.getRetries());
    
    managementService.deleteDeadLetterJob(job.getId());
  }

  protected JobEntity createTweetExceptionMessage() {
    JobEntity message = processEngineConfiguration.getJobEntityManager().create();
    message.setJobType(JobEntity.JOB_TYPE_MESSAGE);
    message.setJobHandlerType("tweet-exception");
    message.setRetries(3);
    return message;
  }
}
