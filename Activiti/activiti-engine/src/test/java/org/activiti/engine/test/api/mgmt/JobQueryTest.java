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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.cmd.CancelJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.test.Deployment;

/**


 */
public class JobQueryTest extends PluggableActivitiTestCase {

  private String deploymentId;
  private String messageId;
  private CommandExecutor commandExecutor;
  private JobEntity jobEntity;

  private Date testStartTime;
  private Date timerOneFireTime;
  private Date timerTwoFireTime;
  private Date timerThreeFireTime;

  private String processInstanceIdOne;
  private String processInstanceIdTwo;
  private String processInstanceIdThree;

  private static final long ONE_HOUR = 60L * 60L * 1000L;
  private static final long ONE_SECOND = 1000L;
  private static final String EXCEPTION_MESSAGE = "problem evaluating script: javax.script.ScriptException: java.lang.RuntimeException: This is an exception thrown from scriptTask";

  /**
   * Setup will create - 3 process instances, each with one timer, each firing at t1/t2/t3 + 1 hour (see process) - 1 message
   */
  protected void setUp() throws Exception {
    super.setUp();

    this.commandExecutor = processEngineConfiguration.getCommandExecutor();

    deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml").deploy().getId();

    // Create proc inst that has timer that will fire on t1 + 1 hour
    Calendar startTime = Calendar.getInstance();
    startTime.set(Calendar.MILLISECOND, 0);

    Date t1 = startTime.getTime();
    processEngineConfiguration.getClock().setCurrentTime(t1);

    processInstanceIdOne = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    testStartTime = t1;
    timerOneFireTime = new Date(t1.getTime() + ONE_HOUR);

    // Create process instance that has timer that will fire on t2 + 1 hour
    startTime.add(Calendar.HOUR_OF_DAY, 1);
    Date t2 = startTime.getTime(); // t2 = t1 + 1 hour
    processEngineConfiguration.getClock().setCurrentTime(t2);
    processInstanceIdTwo = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerTwoFireTime = new Date(t2.getTime() + ONE_HOUR);

    // Create process instance that has timer that will fire on t3 + 1 hour
    startTime.add(Calendar.HOUR_OF_DAY, 1);
    Date t3 = startTime.getTime(); // t3 = t2 + 1 hour
    processEngineConfiguration.getClock().setCurrentTime(t3);
    processInstanceIdThree = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerThreeFireTime = new Date(t3.getTime() + ONE_HOUR);

    // Create one message
    messageId = commandExecutor.execute(new Command<String>() {
      public String execute(CommandContext commandContext) {
        JobEntity message = commandContext.getJobEntityManager().create();
        message.setJobType(Job.JOB_TYPE_MESSAGE);
        message.setRetries(3);
        commandContext.getJobManager().scheduleAsyncJob(message);
        return message.getId();
      }
    });
  }

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentId, true);
    commandExecutor.execute(new CancelJobsCmd(messageId));
    super.tearDown();
  }

  public void testQueryByNoCriteria() {
    JobQuery query = managementService.createJobQuery();
    verifyQueryResults(query, 1);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery();
    verifyQueryResults(timerQuery, 3);
  }

  public void testQueryByProcessInstanceId() {
    TimerJobQuery query = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne);
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidProcessInstanceId() {
    TimerJobQuery query = managementService.createTimerJobQuery().processInstanceId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().processInstanceId(null);
      fail();
    } catch (ActivitiIllegalArgumentException e) {
    }
  }

  public void testQueryByExecutionId() {
    Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    TimerJobQuery query = managementService.createTimerJobQuery().executionId(job.getExecutionId());
    assertEquals(query.singleResult().getId(), job.getId());
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidExecutionId() {
    JobQuery query = managementService.createJobQuery().executionId("invalid");
    verifyQueryResults(query, 0);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery().executionId("invalid");
    verifyQueryResults(timerQuery, 0);

    try {
      managementService.createJobQuery().executionId(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
    }
    
    try {
      managementService.createTimerJobQuery().executionId(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
    }
  }

  public void testQueryByRetriesLeft() {
    JobQuery query = managementService.createJobQuery();
    verifyQueryResults(query, 1);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery();
    verifyQueryResults(timerQuery, 3);

    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    managementService.setTimerJobRetries(job.getId(), 0);
    managementService.moveJobToDeadLetterJob(job.getId());
    
    // Re-running the query should give only 3 jobs now, since one job has retries=0
    verifyQueryResults(query, 1);
    verifyQueryResults(timerQuery, 2);
  }

  public void testQueryByExecutable() {
    processEngineConfiguration.getClock().setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // all obs should be executable at t3 + 1hour.1second
    JobQuery query = managementService.createJobQuery();
    verifyQueryResults(query, 1);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery().executable();
    verifyQueryResults(timerQuery, 3);

    // Setting retries of one job to 0, makes it non-executable
    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    managementService.setTimerJobRetries(job.getId(), 0);
    managementService.moveJobToDeadLetterJob(job.getId());
    
    verifyQueryResults(query, 1);
    verifyQueryResults(timerQuery, 2);

    // Setting the clock before the start of the process instance, makes
    // none of the timer jobs executable
    processEngineConfiguration.getClock().setCurrentTime(testStartTime);
    verifyQueryResults(query, 1);
    verifyQueryResults(timerQuery, 0);
    
    // Moving the job back to be executable
    managementService.moveDeadLetterJobToExecutableJob(job.getId(), 5);
    verifyQueryResults(query, 2);
    verifyQueryResults(timerQuery, 0);
  }

  public void testQueryByOnlyTimers() {
    JobQuery query = managementService.createJobQuery().timers();
    verifyQueryResults(query, 0);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery().timers();
    verifyQueryResults(timerQuery, 3);
  }

  public void testQueryByOnlyMessages() {
    JobQuery query = managementService.createJobQuery().messages();
    verifyQueryResults(query, 1);
  }

  public void testInvalidOnlyTimersUsage() {
    try {
      managementService.createJobQuery().timers().messages().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      assertTextPresent("Cannot combine onlyTimers() with onlyMessages() in the same query", e.getMessage());
    }
  }

  public void testQueryByDuedateLowerThan() {
    JobQuery query = managementService.createJobQuery().duedateLowerThan(testStartTime);
    verifyQueryResults(query, 0);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery().duedateLowerThan(testStartTime);
    verifyQueryResults(timerQuery, 0);

    timerQuery = managementService.createTimerJobQuery().duedateLowerThan(new Date(timerOneFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(timerQuery, 1);

    timerQuery = managementService.createTimerJobQuery().duedateLowerThan(new Date(timerTwoFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(timerQuery, 2);

    timerQuery = managementService.createTimerJobQuery().duedateLowerThan(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(timerQuery, 3);
  }

  public void testQueryByDuedateHigherThan() {
    JobQuery query = managementService.createJobQuery().duedateHigherThan(testStartTime);
    verifyQueryResults(query, 0);
    
    query = managementService.createJobQuery();
    verifyQueryResults(query, 1);
    
    TimerJobQuery timerQuery = managementService.createTimerJobQuery().duedateHigherThan(testStartTime);
    verifyQueryResults(timerQuery, 3);
    
    query = managementService.createJobQuery().duedateHigherThan(timerOneFireTime);
    verifyQueryResults(query, 0);

    timerQuery = managementService.createTimerJobQuery().duedateHigherThan(timerOneFireTime);
    verifyQueryResults(timerQuery, 2);

    timerQuery = managementService.createTimerJobQuery().duedateHigherThan(timerTwoFireTime);
    verifyQueryResults(timerQuery, 1);

    timerQuery = managementService.createTimerJobQuery().duedateHigherThan(timerThreeFireTime);
    verifyQueryResults(timerQuery, 0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml" })
  public void testQueryByException() {
    TimerJobQuery query = managementService.createTimerJobQuery().withException();
    verifyQueryResults(query, 0);

    ProcessInstance processInstance = startProcessInstanceWithFailingJob();

    query = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).withException();
    verifyFailedJob(query, processInstance);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml" })
  public void testQueryByExceptionMessage() {
    TimerJobQuery query = managementService.createTimerJobQuery().exceptionMessage(EXCEPTION_MESSAGE);
    verifyQueryResults(query, 0);

    ProcessInstance processInstance = startProcessInstanceWithFailingJob();

    query = managementService.createTimerJobQuery().exceptionMessage(EXCEPTION_MESSAGE);
    verifyFailedJob(query, processInstance);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml" })
  public void testQueryByExceptionMessageEmpty() {
    JobQuery query = managementService.createJobQuery().exceptionMessage("");
    verifyQueryResults(query, 0);

    startProcessInstanceWithFailingJob();

    query = managementService.createJobQuery().exceptionMessage("");
    verifyQueryResults(query, 0);
  }

  public void testQueryByExceptionMessageNull() {
    try {
      managementService.createJobQuery().exceptionMessage(null);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException e) {
      assertEquals("Provided exception message is null", e.getMessage());
    }
  }

  public void testJobQueryWithExceptions() throws Throwable {

    createJobWithoutExceptionMsg();

    Job job = managementService.createJobQuery().jobId(jobEntity.getId()).singleResult();

    assertNotNull(job);

    List<Job> list = managementService.createJobQuery().withException().list();
    assertEquals(1, list.size());

    deleteJobInDatabase();

    createJobWithoutExceptionStacktrace();

    job = managementService.createJobQuery().jobId(jobEntity.getId()).singleResult();

    assertNotNull(job);

    list = managementService.createJobQuery().withException().list();
    assertEquals(1, list.size());

    deleteJobInDatabase();

  }

  // sorting //////////////////////////////////////////

  public void testQuerySorting() {
    // asc
    assertEquals(1, managementService.createJobQuery().orderByJobId().asc().count());
    assertEquals(1, managementService.createJobQuery().orderByJobDuedate().asc().count());
    assertEquals(1, managementService.createJobQuery().orderByExecutionId().asc().count());
    assertEquals(1, managementService.createJobQuery().orderByProcessInstanceId().asc().count());
    assertEquals(1, managementService.createJobQuery().orderByJobRetries().asc().count());
    
    assertEquals(3, managementService.createTimerJobQuery().orderByJobId().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobDuedate().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByExecutionId().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByProcessInstanceId().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobRetries().asc().count());

    // desc
    assertEquals(1, managementService.createJobQuery().orderByJobId().desc().count());
    assertEquals(1, managementService.createJobQuery().orderByJobDuedate().desc().count());
    assertEquals(1, managementService.createJobQuery().orderByExecutionId().desc().count());
    assertEquals(1, managementService.createJobQuery().orderByProcessInstanceId().desc().count());
    assertEquals(1, managementService.createJobQuery().orderByJobRetries().desc().count());
    
    assertEquals(3, managementService.createTimerJobQuery().orderByJobId().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobDuedate().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByExecutionId().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByProcessInstanceId().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobRetries().desc().count());

    // sorting on multiple fields
    setRetries(processInstanceIdTwo, 2);
    processEngineConfiguration.getClock().setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // make sure all timers can fire

    TimerJobQuery query = managementService.createTimerJobQuery().timers().executable().orderByJobRetries().asc().orderByJobDuedate().desc();

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
    } catch (ActivitiIllegalArgumentException e) {
      assertTextPresent("call asc() or desc() after using orderByXX()", e.getMessage());
    }

    try {
      managementService.createJobQuery().asc();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      assertTextPresent("You should call any of the orderBy methods first before specifying a direction", e.getMessage());
    }
  }

  // helper ////////////////////////////////////////////////////////////

  private void setRetries(final String processInstanceId, final int retries) {
    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceId).singleResult();
    managementService.setTimerJobRetries(job.getId(), retries);
  }

  private ProcessInstance startProcessInstanceWithFailingJob() {
    // start a process with a failing job
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull("No job found for process instance", timerJob);

    try {
      managementService.moveTimerToExecutableJob(timerJob.getId());
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
      
    } catch (RuntimeException re) {
      assertTextPresent(EXCEPTION_MESSAGE, re.getMessage());
    }
    
    return processInstance;
  }

  private void verifyFailedJob(TimerJobQuery query, ProcessInstance processInstance) {
    verifyQueryResults(query, 1);

    Job failedJob = query.singleResult();
    assertNotNull(failedJob);
    assertEquals(processInstance.getId(), failedJob.getProcessInstanceId());
    assertNotNull(failedJob.getExceptionMessage());
    assertTextPresent(EXCEPTION_MESSAGE, failedJob.getExceptionMessage());
  }
  
  private void verifyQueryResults(JobQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1) {
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(JobQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
    }
  }

  private void verifyQueryResults(TimerJobQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1) {
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(TimerJobQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
    }
  }

  private void createJobWithoutExceptionMsg() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        jobEntity = commandContext.getJobEntityManager().create();
        jobEntity.setJobType(Job.JOB_TYPE_MESSAGE);
        jobEntity.setLockOwner(UUID.randomUUID().toString());
        jobEntity.setRetries(0);
        
        StringWriter stringWriter = new StringWriter();
        NullPointerException exception = new NullPointerException();
        exception.printStackTrace(new PrintWriter(stringWriter));
        jobEntity.setExceptionStacktrace(stringWriter.toString());

        commandContext.getJobEntityManager().insert(jobEntity);

        assertNotNull(jobEntity.getId());

        return null;

      }
    });

  }

  private void createJobWithoutExceptionStacktrace() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        jobEntity = commandContext.getJobEntityManager().create();
        jobEntity.setJobType(Job.JOB_TYPE_MESSAGE);
        jobEntity.setLockOwner(UUID.randomUUID().toString());
        jobEntity.setRetries(0);
        
        jobEntity.setExceptionMessage("I'm supposed to fail");
        
        commandContext.getJobEntityManager().insert(jobEntity);

        assertNotNull(jobEntity.getId());

        return null;

      }
    });

  }

  private void deleteJobInDatabase() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        commandContext.getJobEntityManager().delete(jobEntity.getId());
        return null;
      }
    });
  }

}
