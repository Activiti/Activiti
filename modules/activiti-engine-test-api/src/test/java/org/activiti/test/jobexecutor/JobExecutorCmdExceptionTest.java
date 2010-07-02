/**
 * 
 */
package org.activiti.test.jobexecutor;

import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.cmd.DeleteJobsCmd;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.job.MessageImpl;
import org.activiti.test.JobExecutorPoller;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author tombaeyens
 * 
 */
public class JobExecutorCmdExceptionTest {

  protected TweetExceptionHandler tweetExceptionHandler = new TweetExceptionHandler();

  private ProcessEngineImpl processEngineImpl;

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Before
  public void setUp() throws Exception {
    // FIXME: downcast
    processEngineImpl = (ProcessEngineImpl) deployer.getProcessEngine();
    processEngineImpl.getProcessEngineConfiguration().getJobHandlers().addJobHandler(tweetExceptionHandler);
  }

  @After
  public void tearDown() throws Exception {
    processEngineImpl.getProcessEngineConfiguration().getJobHandlers().removeJobHandler(tweetExceptionHandler);
  }

  @Test
  public void testJobCommandsWith2Exceptions() {
    CommandExecutor commandExecutor = deployer.getCommandExecutor();
    commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageImpl message = createTweetExceptionMessage();
        commandContext.getMessageSession().send(message);
        return message.getId();
      }
    });

    new JobExecutorPoller(deployer.getJobExecutor(), deployer.getCommandExecutor()).waitForJobExecutorToProcessAllJobs(8000, 250);
  }

  @Test
  public void testJobCommandsWith3Exceptions() {
    tweetExceptionHandler.setExceptionsRemaining(3);

    CommandExecutor commandExecutor = deployer.getCommandExecutor();
    String jobId = commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageImpl message = createTweetExceptionMessage();
        commandContext.getMessageSession().send(message);
        return message.getId();
      }
    });

    new JobExecutorPoller(deployer.getJobExecutor(), deployer.getCommandExecutor()).waitForJobExecutorToProcessAllJobs(8000, 250);

    // TODO check if there is a failed job in the DLQ

    commandExecutor.execute(new DeleteJobsCmd(jobId));
  }

  protected MessageImpl createTweetExceptionMessage() {
    MessageImpl message = new MessageImpl();
    message.setJobHandlerType("tweet-exception");
    return message;
  }
}
