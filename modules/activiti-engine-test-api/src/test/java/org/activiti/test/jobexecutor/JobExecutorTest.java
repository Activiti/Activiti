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
package org.activiti.test.jobexecutor;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.msg.MessageSession;
import org.activiti.impl.timer.TimerSession;
import org.activiti.test.JobExecutorPoller;
import org.junit.Test;



/**
 * @author Tom Baeyens
 */
public class JobExecutorTest extends JobExecutorTestCase {

  @Test
  public void testBasicJobExecutorOperation() throws Exception {
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl)processEngineBuilder.getProcessEngine();
    CommandExecutor commandExecutor = processEngineImpl.getProcessEngineConfiguration().getCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        MessageSession messageSession = commandContext.getMessageSession();
        messageSession.send(createTweetMessage("message-one"));
        messageSession.send(createTweetMessage("message-two"));
        messageSession.send(createTweetMessage("message-three"));
        messageSession.send(createTweetMessage("message-four"));
        
        TimerSession timerSession = commandContext.getTimerSession();
        timerSession.schedule(createTweetTimer("timer-one", new Date()));
        timerSession.schedule(createTweetTimer("timer-two", new Date()));
        return null;
      }
    });
    
    new JobExecutorPoller(processEngineBuilder.getProcessEngine()).waitForJobExecutorToProcessAllJobs(8000, 200);
    
    Set<String> messages = new HashSet<String>(tweetHandler.getMessages());
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("message-one");
    expectedMessages.add("message-two");
    expectedMessages.add("message-three");
    expectedMessages.add("message-four");
    expectedMessages.add("timer-one");
    expectedMessages.add("timer-two");
    
    assertEquals(expectedMessages, messages);
  }
}
