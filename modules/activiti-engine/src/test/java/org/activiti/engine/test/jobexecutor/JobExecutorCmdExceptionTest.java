/**
 * 
 */
package org.activiti.engine.test.jobexecutor;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;

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
        MessageEntity message = createTweetExceptionMessage();
        commandContext.getJobEntityManager().send(message);
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
    
    job = managementService.createJobQuery().singleResult();
    assertEquals(2, job.getRetries());
    
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createJobQuery().singleResult();
    assertEquals(1, job.getRetries());
    
    managementService.executeJob(job.getId());
  }

  public void testJobCommandsWith3Exceptions() {
    tweetExceptionHandler.setExceptionsRemaining(3);

    commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageEntity message = createTweetExceptionMessage();
        commandContext.getJobEntityManager().send(message);
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
    
    job = managementService.createJobQuery().singleResult();
    assertEquals(2, job.getRetries());
    
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createJobQuery().singleResult();
    assertEquals(1, job.getRetries());
    
    try {
      managementService.executeJob(job.getId());
      fail("exception expected");
    } catch (Exception e) {
      // exception expected;
    }
    
    job = managementService.createJobQuery().singleResult();
    assertEquals(0, job.getRetries());
    
    managementService.deleteJob(job.getId());
  }

  protected MessageEntity createTweetExceptionMessage() {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType("tweet-exception");
    return message;
  }
}
