/**
 * 
 */
package org.activiti.engine.test.jobexecutor;

import org.activiti.engine.impl.cmd.DeleteJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

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

    waitForJobExecutorToProcessAllJobs(15000L, 50L);
  }

  public void testJobCommandsWith3Exceptions() {
    tweetExceptionHandler.setExceptionsRemaining(3);

    String jobId = commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageEntity message = createTweetExceptionMessage();
        commandContext.getJobEntityManager().send(message);
        return message.getId();
      }
    });

    waitForJobExecutorToProcessAllJobs(15000L, 50L);

    // TODO check if there is a failed job in the DLQ

    commandExecutor.execute(new DeleteJobsCmd(jobId));
  }

  protected MessageEntity createTweetExceptionMessage() {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType("tweet-exception");
    return message;
  }
}
