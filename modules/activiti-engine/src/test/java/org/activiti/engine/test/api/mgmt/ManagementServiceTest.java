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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Saeid Mizaei
 * @author Joram Barrez
 */
public class ManagementServiceTest extends PluggableActivitiTestCase {

  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertNull(metaData);
  }
  
  public void testGetMetaDataNullTableName() {
    try {
      managementService.getTableMetaData(null);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("tableName is null", re.getMessage());
    }
  }
  
  public void testExecuteJobNullJobId() {
    try {
      managementService.executeJob(null);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("jobId and job is null", re.getMessage());
    }
  }
  
  public void testExecuteJobUnexistingJob() {
    try {
      managementService.executeJob("unexistingjob");
      fail("ActivitiException expected");
    } catch (JobNotFoundException jnfe) {
      assertTextPresent("No job found with id", jnfe.getMessage());
      assertEquals(Job.class, jnfe.getObjectClass());
    }
  }
  
  
  @Deployment
  public void testGetJobExceptionStacktrace() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    
    // The execution is waiting in the first usertask. This contains a boundry
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    
    assertNotNull("No job found for process instance", timerJob);
    
    try {
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
    } catch(RuntimeException re) {
      assertTextPresent("This is an exception thrown from scriptTask", re.getCause().getMessage());
    }
    
    // Fetch the task to see that the exception that occurred is persisted
    timerJob = managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .singleResult();
    
    assertNotNull(timerJob);
    assertNotNull(timerJob.getExceptionMessage());
    assertTextPresent("This is an exception thrown from scriptTask", timerJob.getExceptionMessage());
    
    // Get the full stacktrace using the managementService
    String exceptionStack = managementService.getJobExceptionStacktrace(timerJob.getId());
    assertNotNull(exceptionStack);
    assertTextPresent("This is an exception thrown from scriptTask", exceptionStack);    
  }
  
  public void testgetJobExceptionStacktraceUnexistingJobId() {
    try {
      managementService.getJobExceptionStacktrace("unexistingjob");
      fail("ActivitiException expected");
    } catch (ActivitiObjectNotFoundException re) {
      assertTextPresent("No job found with id unexistingjob", re.getMessage());
      assertEquals(Job.class, re.getObjectClass());
    }
  }
  
  public void testgetJobExceptionStacktraceNullJobId() {
    try {
      managementService.getJobExceptionStacktrace(null);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testSetJobRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event.
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    Date duedate = timerJob.getDuedate();

    assertNotNull("No job found for process instance", timerJob);
    assertEquals(JobEntity.DEFAULT_RETRIES, timerJob.getRetries());

    managementService.setJobRetries(timerJob.getId(), 5);

    timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();
    assertEquals(5, timerJob.getRetries());
    assertEquals(duedate, timerJob.getDuedate());
  }
  
  public void testSetJobRetriesUnexistingJobId() {
    try {
      managementService.setJobRetries("unexistingjob", 5);
      fail("ActivitiException expected");
    } catch (ActivitiObjectNotFoundException re) {
      assertTextPresent("No job found with id 'unexistingjob'.", re.getMessage());
      assertEquals(Job.class, re.getObjectClass());
    }
  }
  
  public void testSetJobRetriesEmptyJobId() {
    try {
      managementService.setJobRetries("", 5);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("The job id is mandatory, but '' has been provided.", re.getMessage());
    }
  }
  
  public void testSetJobRetriesJobIdNull() {
    try {
      managementService.setJobRetries(null, 5);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("The job id is mandatory, but 'null' has been provided.", re.getMessage());
    }
  }

  public void testSetJobRetriesNegativeNumberOfRetries() {
    try {
      managementService.setJobRetries("unexistingjob", -1);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("The number of job retries must be a non-negative Integer, but '-1' has been provided.", re.getMessage());
    }
  }

  public void testDeleteJobNullJobId() {
    try {
      managementService.deleteJob(null);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }

  public void testDeleteJobUnexistingJob() {
    try {
      managementService.deleteJob("unexistingjob");
      fail("ActivitiException expected");
    } catch (ActivitiObjectNotFoundException ae) {
      assertTextPresent("No job found with id", ae.getMessage());
      assertEquals(Job.class, ae.getObjectClass());
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobDeletion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull("Task timer should be there", timerJob);
    managementService.deleteJob(timerJob.getId());
    
    timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull("There should be no job now. It was deleted", timerJob);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobThatWasAlreadyAcquired() {
    processEngineConfiguration.getClock().setCurrentTime(new Date());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // We need to move time at least one hour to make the timer executable
    processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + 7200000L));

    // Acquire job by running the acquire command manually
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) processEngine;
    AcquireTimerJobsCmd acquireJobsCmd = new AcquireTimerJobsCmd("testLockOwner", 60000, 5);
    CommandExecutor commandExecutor = processEngineImpl.getProcessEngineConfiguration().getCommandExecutor();
    commandExecutor.execute(acquireJobsCmd);
    
    // Try to delete the job. This should fail.
    try {
      managementService.deleteJob(timerJob.getId());
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
    
    // Clean up
    managementService.executeJob(timerJob.getId());
  }
  
  // https://activiti.atlassian.net/browse/ACT-1816:
  // ManagementService doesn't seem to give actual table Name for EventSubscriptionEntity.class
  public void testGetTableName() {
	  String table = managementService.getTableName(EventSubscriptionEntity.class);
	  assertEquals("ACT_RU_EVENT_SUBSCR", table);
  }
}
