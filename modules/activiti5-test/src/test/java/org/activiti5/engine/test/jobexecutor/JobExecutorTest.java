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
package org.activiti5.engine.test.jobexecutor;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.activiti.engine.runtime.Clock;
import org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.persistence.entity.JobEntityManager;
import org.activiti5.engine.impl.test.JobTestHelper;

/**
 * @author Tom Baeyens
 */
public class JobExecutorTest extends JobExecutorTestCase {

  public void testBasicJobExecutorOperation() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    Date previousTime = clock.getCurrentTime();
    CommandExecutor commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobEntityManager jobManager = commandContext.getJobEntityManager();
        jobManager.send(createTweetMessage("message-one"));
        jobManager.send(createTweetMessage("message-two"));
        jobManager.send(createTweetMessage("message-three"));
        jobManager.send(createTweetMessage("message-four"));
        
        jobManager.schedule(createTweetTimer("timer-one", new Date()));
        jobManager.schedule(createTweetTimer("timer-one", new Date()));
        jobManager.schedule(createTweetTimer("timer-two", new Date()));
        return null;
      }
    });
    
    GregorianCalendar currentCal = new GregorianCalendar();
    currentCal.add(Calendar.MINUTE, 1);
    clock.setCurrentCalendar(currentCal);
    processEngineConfiguration.setClock(clock);
    
    ProcessEngineConfigurationImpl activiti5ProcessEngineConfig = (ProcessEngineConfigurationImpl) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawProcessConfiguration();
    JobTestHelper.waitForJobExecutorActiviti5ToProcessAllJobs(activiti5ProcessEngineConfig, activiti5ProcessEngineConfig.getManagementService(), 8000L, 200L, true);
    
    Set<String> messages = new HashSet<String>(tweetHandler.getMessages());
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("message-one");
    expectedMessages.add("message-two");
    expectedMessages.add("message-three");
    expectedMessages.add("message-four");
    expectedMessages.add("timer-one");
    expectedMessages.add("timer-two");
    
    assertEquals(new TreeSet<String>(expectedMessages), new TreeSet<String>(messages));
    
    clock.setCurrentTime(previousTime);
    processEngineConfiguration.setClock(clock);
  }
}
