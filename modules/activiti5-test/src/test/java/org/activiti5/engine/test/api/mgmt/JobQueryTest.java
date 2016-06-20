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

package org.activiti5.engine.test.api.mgmt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.DeploymentProperties;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.cmd.CancelJobsCmd;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.persistence.entity.JobEntity;
import org.activiti5.engine.impl.persistence.entity.JobEntityManager;
import org.activiti5.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;


/**
 * @author Joram Barrez
 * @author Tijs Rademakers
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
   * Setup will create
   *   - 3 process instances, each with one timer, each firing at t1/t2/t3 + 1 hour (see process)
   *   - 1 message
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    Clock clock = processEngineConfiguration.getClock();
    
    this.commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
    
    deploymentId = repositoryService.createDeployment()
        .addClasspathResource("org/activiti5/engine/test/api/mgmt/timerOnTask.bpmn20.xml")
        .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
        .deploy()
        .getId();
    
    // Create proc inst that has timer that will fire on t1 + 1 hour
    Calendar startTime = Calendar.getInstance();
    startTime.set(Calendar.MILLISECOND, 0);
    
    Date t1 = startTime.getTime();
    clock.setCurrentTime(t1);
    processEngineConfiguration.setClock(clock);

    processInstanceIdOne = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    testStartTime = t1;
    timerOneFireTime = new Date(t1.getTime() + ONE_HOUR);
    
    // Create proc inst that has timer that will fire on t2 + 1 hour
    startTime.add(Calendar.HOUR_OF_DAY, 1);
    Date t2 = startTime.getTime();  // t2 = t1 + 1 hour
    clock.setCurrentTime(t2);
    processEngineConfiguration.setClock(clock);
    processInstanceIdTwo = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerTwoFireTime = new Date(t2.getTime() + ONE_HOUR);
    
    // Create proc inst that has timer that will fire on t3 + 1 hour
    startTime.add(Calendar.HOUR_OF_DAY, 1);
    Date t3 = startTime.getTime(); // t3 = t2 + 1 hour
    clock.setCurrentTime(t3);
    processEngineConfiguration.setClock(clock);
    processInstanceIdThree = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerThreeFireTime = new Date(t3.getTime() + ONE_HOUR);
  }
  
  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentId, true);
    commandExecutor.execute(new CancelJobsCmd(messageId));
    
    processEngineConfiguration.resetClock();
    
    super.tearDown();
  }
  
  public void testQueryByNoCriteria() {
    TimerJobQuery query = managementService.createTimerJobQuery();
    verifyQueryResults(query, 3);
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
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByExecutionId() {
    Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    TimerJobQuery query = managementService.createTimerJobQuery().executionId(job.getExecutionId());
    assertEquals(query.singleResult().getId(), job.getId());
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidExecutionId() {
    TimerJobQuery query = managementService.createTimerJobQuery().executionId("invalid");
    verifyQueryResults(query, 0);
    
    try {
      managementService.createTimerJobQuery().executionId(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByRetriesLeft() {
    TimerJobQuery query = managementService.createTimerJobQuery();
    verifyQueryResults(query, 3);
    
    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    managementService.setTimerJobRetries(job.getId(), 0);
    managementService.moveJobToDeadLetterJob(job.getId());
    
    // Re-running the query should give only 3 jobs now, since one job has retries=0
    verifyQueryResults(query, 2);
  }
  
  public void testQueryByExecutable() {
    processEngineConfiguration.getClock().setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // all jobs should be executable at t3 + 1hour.1second
    TimerJobQuery query = managementService.createTimerJobQuery().executable();;
    verifyQueryResults(query, 3);
    
    // Setting retries of one job to 0, makes it non-executable
    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    managementService.setTimerJobRetries(job.getId(), 0);
    managementService.moveJobToDeadLetterJob(job.getId());
    verifyQueryResults(query, 2);
    
    // Setting the clock before the start of the process instance, makes none of the jobs executable
    processEngineConfiguration.getClock().setCurrentTime(testStartTime);
    verifyQueryResults(query, 0); 
  }
  
  public void testQueryByOnlyTimers() {
    TimerJobQuery query = managementService.createTimerJobQuery().timers();
    verifyQueryResults(query, 3);
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
    TimerJobQuery query = managementService.createTimerJobQuery().duedateLowerThan(testStartTime);
    verifyQueryResults(query, 0);
    
    query = managementService.createTimerJobQuery().duedateLowerThan(new Date(timerOneFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 1);
    
    query = managementService.createTimerJobQuery().duedateLowerThan(new Date(timerTwoFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 2);
    
    query = managementService.createTimerJobQuery().duedateLowerThan(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 3);
  }
  
  public void testQueryByDuedateHigherThan() {
    TimerJobQuery query = managementService.createTimerJobQuery().duedateHigherThan(testStartTime);
    verifyQueryResults(query, 3);
    
    query = managementService.createTimerJobQuery().duedateHigherThan(timerOneFireTime);
    verifyQueryResults(query, 2);
    
    query = managementService.createTimerJobQuery().duedateHigherThan(timerTwoFireTime);
    verifyQueryResults(query, 1);
    
    query = managementService.createTimerJobQuery().duedateHigherThan(timerThreeFireTime);
    verifyQueryResults(query, 0);
  }
  
  @Deployment(resources = {"org/activiti5/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testQueryByException() {
    TimerJobQuery query = managementService.createTimerJobQuery().withException();
    verifyQueryResults(query, 0);
    
    ProcessInstance processInstance = startProcessInstanceWithFailingJob();
    
    query = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).withException();
    verifyFailedJob(query, processInstance);
  }
  
  @Deployment(resources = {"org/activiti5/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testQueryByExceptionMessage() {
    TimerJobQuery query = managementService.createTimerJobQuery().exceptionMessage(EXCEPTION_MESSAGE);
    verifyQueryResults(query, 0);
    
    ProcessInstance processInstance = startProcessInstanceWithFailingJob();
    
    query = managementService.createTimerJobQuery().exceptionMessage(EXCEPTION_MESSAGE);
    verifyFailedJob(query, processInstance);
  }

  @Deployment(resources = {"org/activiti5/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testQueryByExceptionMessageEmpty() {
    TimerJobQuery query = managementService.createTimerJobQuery().exceptionMessage("");
    verifyQueryResults(query, 0);
    
    startProcessInstanceWithFailingJob();
    
    query = managementService.createTimerJobQuery().exceptionMessage("");
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

  //sorting //////////////////////////////////////////
  
  public void testQuerySorting() {
    // asc
    assertEquals(3, managementService.createTimerJobQuery().orderByJobId().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobDuedate().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByExecutionId().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByProcessInstanceId().asc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobRetries().asc().count());

    // desc
    assertEquals(3, managementService.createTimerJobQuery().orderByJobId().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobDuedate().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByExecutionId().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByProcessInstanceId().desc().count());
    assertEquals(3, managementService.createTimerJobQuery().orderByJobRetries().desc().count());
    
    // sorting on multiple fields
    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceIdTwo).singleResult();
    managementService.setTimerJobRetries(job.getId(), 2);
    
    processEngineConfiguration.getClock().setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // make sure all timers can fire
    
    TimerJobQuery query = managementService.createTimerJobQuery()
      .timers()
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
  
  //helper ////////////////////////////////////////////////////////////
  
  private void setRetries(final String processInstanceId, final int retries) {
    final Job job = managementService.createTimerJobQuery().processInstanceId(processInstanceId).singleResult();
    commandExecutor.execute(new Command<Void>() {
      
      public Void execute(CommandContext commandContext) {
        TimerJobEntity timer = commandContext.getDbSqlSession().selectById(TimerJobEntity.class, job.getId());
        timer.setRetries(retries);
        return null;
      }
      
    });
  }

  private ProcessInstance startProcessInstanceWithFailingJob() {
    // start a process with a failing job
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    
    // The execution is waiting in the first usertask. This contains a boundary
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createTimerJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    
    assertNotNull("No job found for process instance", timerJob);
    
    try {
      managementService.moveTimerToExecutableJob(timerJob.getId());
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
    } catch(RuntimeException re) {
      assertTextPresent(EXCEPTION_MESSAGE, re.getCause().getMessage());
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

  private void verifyQueryResults(TimerJobQuery query, int countExpected) {
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
  
  private void verifySingleResultFails(TimerJobQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
  private void createJobWithoutExceptionMsg() {
    CommandExecutor commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobEntityManager jobManager = commandContext.getJobEntityManager();
        
        jobEntity = new JobEntity();
        jobEntity.setJobType(Job.JOB_TYPE_MESSAGE);
        jobEntity.setRevision(1);
        jobEntity.setLockOwner(UUID.randomUUID().toString());
        jobEntity.setRetries(0);

        StringWriter stringWriter = new StringWriter();
        NullPointerException exception = new NullPointerException();
        exception.printStackTrace(new PrintWriter(stringWriter));
        jobEntity.setExceptionStacktrace(stringWriter.toString());

        jobManager.insert(jobEntity);
        
        assertNotNull(jobEntity.getId());
        
        return null;
        
      }
    });
    
  }
  
  private void createJobWithoutExceptionStacktrace() {
    CommandExecutor commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobEntityManager jobManager = commandContext.getJobEntityManager();
        
        jobEntity = new JobEntity();
        jobEntity.setJobType(Job.JOB_TYPE_MESSAGE);
        jobEntity.setRevision(1);
        jobEntity.setLockOwner(UUID.randomUUID().toString());
        jobEntity.setRetries(0);
        jobEntity.setExceptionMessage("I'm supposed to fail");

        jobManager.insert(jobEntity);
        
        assertNotNull(jobEntity.getId());
        
        return null;
        
      }
    });
    
  }  
  
  private void deleteJobInDatabase() {
      CommandExecutor commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          
          jobEntity.delete();          
          return null;
        }
      });    
  }  
  
}
