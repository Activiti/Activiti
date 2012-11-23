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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.GetUnlockedTimersByDuedateCmd;
import org.activiti.engine.impl.persistence.entity.JobManager;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;



/**
 * @author Tom Baeyens
 */
public class JobExecutorTest extends JobExecutorTestCase {

  public void testBasicJobExecutorOperation() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
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
    
    waitForJobExecutorToProcessAllJobs(8000L, 200L);
    
    Set<String> messages = new HashSet<String>(tweetHandler.getMessages());
    Set<String> expectedMessages = new HashSet<String>();
    expectedMessages.add("message-one");
    expectedMessages.add("message-two");
    expectedMessages.add("message-three");
    expectedMessages.add("message-four");
    expectedMessages.add("timer-one");
    expectedMessages.add("timer-two");
    
    assertEquals(new TreeSet<String>(expectedMessages), new TreeSet<String>(messages));
  }
  
  @Deployment
  public void testSuspendedProcessTimerExecution() throws Exception {
    // Process with boundary timer-event that fires in 1 hour
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("suspendProcess");
    assertNotNull(procInst);
    assertEquals(1, managementService.createJobQuery().processInstanceId(procInst.getId()).count());
    
    // Shutdown the job-executor so timer's won't be executed
    processEngineConfiguration.getJobExecutor().shutdown();
    
    // Roll time ahead to be sure timer is due to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    ClockUtil.setCurrentTime(tomorrow.getTime());
    
    // Check if timer is eligable to be executed, when process in not yet suspended
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    List<TimerEntity> jobs = commandExecutor.execute(new GetUnlockedTimersByDuedateCmd(ClockUtil.getCurrentTime(), new Page(0, 1)));
    assertEquals(1, jobs.size());
    
    // Suspend process instancd
    runtimeService.suspendProcessInstanceById(procInst.getId());

    // Check if the timer is NOT aquired, even though the duedate is reached
    jobs = commandExecutor.execute(new GetUnlockedTimersByDuedateCmd(ClockUtil.getCurrentTime(), new Page(0, 1)));
    assertEquals(0, jobs.size());
    
    // Start job-executor again
    processEngineConfiguration.getJobExecutor().start();
  }
}
