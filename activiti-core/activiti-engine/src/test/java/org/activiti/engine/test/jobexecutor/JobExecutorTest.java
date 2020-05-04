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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityManager;

/**

 */
public class JobExecutorTest extends JobExecutorTestCase {

  public void testBasicJobExecutorOperation() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        jobManager.execute(createTweetMessage("message-one"));
        jobManager.execute(createTweetMessage("message-two"));
        jobManager.execute(createTweetMessage("message-three"));
        jobManager.execute(createTweetMessage("message-four"));

        TimerJobEntityManager timerJobManager = commandContext.getTimerJobEntityManager();
        timerJobManager.insert(createTweetTimer("timer-one", new Date()));
        timerJobManager.insert(createTweetTimer("timer-one", new Date()));
        timerJobManager.insert(createTweetTimer("timer-two", new Date()));
        return null;
      }
    });

    GregorianCalendar currentCal = new GregorianCalendar();
    currentCal.add(Calendar.MINUTE, 1);
    processEngineConfiguration.getClock().setCurrentTime(currentCal.getTime());

    waitForJobExecutorToProcessAllJobs(50000L, 200L);

    Set<String> messages = new HashSet<String>(tweetHandler.getMessages());
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("message-one");
    expectedMessages.add("message-two");
    expectedMessages.add("message-three");
    expectedMessages.add("message-four");
    expectedMessages.add("timer-one");
    expectedMessages.add("timer-two");

    assertThat(new TreeSet<String>(messages)).isEqualTo(new TreeSet<String>(expectedMessages));
  }
}
