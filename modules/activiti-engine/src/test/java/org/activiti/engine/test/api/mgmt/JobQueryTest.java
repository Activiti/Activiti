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

package org.activiti.engine.test.api.mgmt;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cmd.DeleteJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.runtime.JobEntity;
import org.activiti.engine.impl.runtime.MessageEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;


/**
 * @author Joram Barrez
 */
public class JobQueryTest extends PluggableActivitiTestCase {
  
  private String deploymentId;
  private String messageId;
  private CommandExecutor commandExecutor;
  
  private Date testStartTime;
  private Date timerOneFireTime;
  private Date timerTwoFireTime;
  private Date timerThreeFireTime;
  
  private String processInstanceIdOne;
  private String processInstanceIdTwo;
  private String processInstanceIdThree;
  
  private static final long ONE_HOUR = 60L * 60L * 1000L;
  private static final long ONE_SECOND = 1000L;

  /**
   * Setup will create
   *   - 3 process instances, each with one timer, each firing at t1/t2/t3 + 1 hour (see process)
   *   - 1 message
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    this.commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    
    deploymentId = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml")
        .deploy()
        .getId();
    
    // Create proc inst that has timer that will fire on t1 + 1 hour
    Date t1 = new Date();
    ClockUtil.setCurrentTime(t1);
    processInstanceIdOne = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    testStartTime = t1;
    timerOneFireTime = new Date(t1.getTime() + ONE_HOUR);
    
    // Create proc inst that has timer that will fire on t2 + 1 hour
    Date t2 = new Date(t1.getTime() + ONE_HOUR);  // t2 = t1 + 1 hour
    ClockUtil.setCurrentTime(t2);
    processInstanceIdTwo = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerTwoFireTime = new Date(t2.getTime() + ONE_HOUR);
    
    // Create proc inst that has timer that will fire on t3 + 1 hour
    Date t3 = new Date(t2.getTime() + ONE_HOUR); // t3 = t2 + 1 hour
    ClockUtil.setCurrentTime(t3);
    processInstanceIdThree = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerThreeFireTime = new Date(t3.getTime() + ONE_HOUR);
    
    // Create one message
    messageId = commandExecutor.execute(new Command<String>() {
      public String execute(CommandContext commandContext) {
        MessageEntity message = new MessageEntity();
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });
  }
  
  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentId, true);
    commandExecutor.execute(new DeleteJobsCmd(messageId));
    super.tearDown();
  }
  
  public void testQueryByNoCriteria() {
    JobQuery query = managementService.createJobQuery();
    verifyQueryResults(query, 4);
  }
  
  public void testQueryByProcessInstanceId() {
    JobQuery query = managementService.createJobQuery().processInstanceId(processInstanceIdOne);
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidProcessInstanceId() {
    JobQuery query = managementService.createJobQuery().processInstanceId("invalid");
    verifyQueryResults(query, 0);
    
    try {
      managementService.createJobQuery().processInstanceId(null);
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByExecutionId() {
    Job job = managementService.createJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    JobQuery query = managementService.createJobQuery().executionId(job.getExecutionId());
    assertEquals(query.singleResult().getId(), job.getId());
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidExecutionId() {
    JobQuery query = managementService.createJobQuery().executionId("invalid");
    verifyQueryResults(query, 0);
    
    try {
      managementService.createJobQuery().executionId(null).list();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByRetriesLeft() {
    JobQuery query = managementService.createJobQuery().withRetriesLeft();
    verifyQueryResults(query, 4);
    
    setRetries(processInstanceIdOne, 0);
    // Re-running the query should give only 3 jobs now, since one job has retries=0
    verifyQueryResults(query, 3);
  }
  
  public void testQueryByExecutable() {
    ClockUtil.setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // all jobs should be executable at t3 + 1hour.1second
    JobQuery query = managementService.createJobQuery().executable();
    verifyQueryResults(query, 4);
    
    // Setting retries of one job to 0, makes it non-executable
    setRetries(processInstanceIdOne, 0);
    verifyQueryResults(query, 3);
    
    // Setting the clock before the start of the process instance, makes none of the jobs executable
    ClockUtil.setCurrentTime(testStartTime);
    verifyQueryResults(query, 1); // 1, since a message is always executable when retries > 0
  }
  
  public void testQueryByOnlyTimers() {
    JobQuery query = managementService.createJobQuery().timers();
    verifyQueryResults(query, 3);
  }
  
  public void testQueryByOnlyMessages() {
    JobQuery query = managementService.createJobQuery().messages();
    verifyQueryResults(query, 1);
  }
  
  public void testInvalidOnlyTimersUsage() {
    try {
      managementService.createJobQuery().timers().messages().list();
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Cannot combine onlyTimers() with onlyMessages() in the same query", e.getMessage());
    }
  }
  
  public void testQueryByDuedateLowerThen() {
    JobQuery query = managementService.createJobQuery().duedateLowerThen(testStartTime);
    verifyQueryResults(query, 0);
    
    query = managementService.createJobQuery().duedateLowerThen(new Date(timerOneFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 1);
    
    query = managementService.createJobQuery().duedateLowerThen(new Date(timerTwoFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 2);
    
    query = managementService.createJobQuery().duedateLowerThen(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 3);
  }
  
  public void testQueryByDuedateLowerThenOrEqual() {
    JobQuery query = managementService.createJobQuery().duedateLowerThenOrEquals(testStartTime);
    verifyQueryResults(query, 0);
    
    query = managementService.createJobQuery().duedateLowerThenOrEquals(timerOneFireTime);
    verifyQueryResults(query, 1);
    
    query = managementService.createJobQuery().duedateLowerThenOrEquals(timerTwoFireTime);
    verifyQueryResults(query, 2);
    
    query = managementService.createJobQuery().duedateLowerThenOrEquals(timerThreeFireTime);
    verifyQueryResults(query, 3);
  }
  
  public void testQueryByDuedateHigherThen() {
    JobQuery query = managementService.createJobQuery().duedateHigherThen(testStartTime);
    verifyQueryResults(query, 3);
    
    query = managementService.createJobQuery().duedateHigherThen(timerOneFireTime);
    verifyQueryResults(query, 2);
    
    query = managementService.createJobQuery().duedateHigherThen(timerTwoFireTime);
    verifyQueryResults(query, 1);
    
    query = managementService.createJobQuery().duedateHigherThen(timerThreeFireTime);
    verifyQueryResults(query, 0);
  }
  
  public void testQueryByDuedateHigherThenOrEqual() {
    JobQuery query = managementService.createJobQuery().duedateHigherThenOrEquals(testStartTime);
    verifyQueryResults(query, 3);
    
    query = managementService.createJobQuery().duedateHigherThenOrEquals(timerOneFireTime);
    verifyQueryResults(query, 3);
    
    query = managementService.createJobQuery().duedateHigherThenOrEquals(new Date(timerOneFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 2);
    
    query = managementService.createJobQuery().duedateHigherThenOrEquals(timerThreeFireTime);
    verifyQueryResults(query, 1);
    
    query = managementService.createJobQuery().duedateHigherThenOrEquals(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 0);
  }
  
  public void testQuerySorting() {
    // asc
    assertEquals(4, managementService.createJobQuery().orderByJobId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobDuedate().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByExecutionId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessInstanceId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobRetries().asc().count());

    // desc
    assertEquals(4, managementService.createJobQuery().orderByJobId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobDuedate().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByExecutionId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessInstanceId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobRetries().desc().count());
    
    // sorting on multiple fields
    setRetries(processInstanceIdTwo, 2);
    ClockUtil.setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // make sure all timers can fire
    
    JobQuery query = managementService.createJobQuery()
      .timers()
      .executable()
      .orderByJobRetries()
      .asc()
      .orderByJobDuedate()
      .desc();
     
    List<Job> jobs = query.list();
    assertEquals(3, jobs.size());
    
    assertEquals(2, jobs.get(0).getRetries());
    assertEquals(3, jobs.get(1).getRetries());
    assertEquals(3, jobs.get(2).getRetries());
    
    assertEquals(processInstanceIdTwo, jobs.get(0).getProcessInstanceId());
    assertEquals(processInstanceIdThree, jobs.get(1).getProcessInstanceId());
    assertEquals(processInstanceIdOne, jobs.get(2).getProcessInstanceId());
  }
  
  public void testQueryInvalidSortingUsage() {
    try {
      managementService.createJobQuery().orderByJobId().list();
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("call asc() or desc() after using orderByXX()", e.getMessage());
    }
    
    try {
      managementService.createJobQuery().asc();
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("You should call any of the orderBy methods first before specifying a direction", e.getMessage());
    }
  }
  
  //helper ////////////////////////////////////////////////////////////
  
  private void setRetries(final String processInstanceId, final int retries) {
    final Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    commandExecutor.execute(new Command<Void>() {
      
      public Void execute(CommandContext commandContext) {
        JobEntity timer = commandContext.getDbSqlSession().selectById(JobEntity.class, job.getId());
        timer.setRetries(retries);
        return null;
      }
      
    });
  }

  private void verifyQueryResults(JobQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());
    
    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }
  
  private void verifySingleResultFails(JobQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
}
