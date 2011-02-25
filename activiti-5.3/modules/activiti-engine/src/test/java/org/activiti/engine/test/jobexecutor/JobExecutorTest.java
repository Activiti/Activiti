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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.impl.cfg.MessageSession;
import org.activiti.engine.impl.cfg.TimerSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;



/**
 * @author Tom Baeyens
 */
public class JobExecutorTest extends JobExecutorTestCase {

  public void testBasicJobExecutorOperation() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
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
    
    waitForJobExecutorToProcessAllJobs(8000L, 200L);
    
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
