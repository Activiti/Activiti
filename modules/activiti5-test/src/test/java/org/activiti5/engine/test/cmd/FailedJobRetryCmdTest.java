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
package org.activiti5.engine.test.cmd;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;
/**
 * @author Saeid Mirzaei
 */
public class FailedJobRetryCmdTest extends PluggableActivitiTestCase{
	
  @Deployment(resources = { "org/activiti5/engine/test/cmd/FailedJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
	public void testFailedServiceTask() {
  	ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
  	assertNotNull(pi);
  	waitForExecutedJobWithRetriesLeft(4);
  	
  	stillOneJobWithExceptionAndRetriesLeft();
  	 
  	Job job = fetchJob(pi.getProcessInstanceId());
    assertNotNull(job);
    assertEquals(pi.getProcessInstanceId(), job.getProcessInstanceId());

    assertEquals(4, job.getRetries());

    ExecutionEntity execution = fetchExecutionEntity(pi.getProcessInstanceId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertEquals(3, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertEquals(2, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertEquals(1, job.getRetries());
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());

    waitForExecutedJobWithRetriesLeft(0);

    job = managementService.createDeadLetterJobQuery().jobId(job.getId()).singleResult();
    assertEquals(0, job.getRetries());
    assertEquals(1, managementService.createDeadLetterJobQuery().withException().count());
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, managementService.createTimerJobQuery().count());
    assertEquals(1, managementService.createDeadLetterJobQuery().count());

    execution = refreshExecutionEntity(execution.getId());
    assertEquals("failingServiceTask", execution.getActivityId());    	 
	}
	
  protected void waitForExecutedJobWithRetriesLeft(final int retriesLeft) {

    Job job = managementService.createJobQuery().singleResult();
    if (job == null) {
      job = managementService.createTimerJobQuery().singleResult();
      managementService.moveTimerToExecutableJob(job.getId());
    }

    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
    }

    // update job
    job = managementService.createTimerJobQuery().singleResult();
    if (job == null) {
      job = managementService.createDeadLetterJobQuery().singleResult();
    }

    if (job.getRetries() > retriesLeft) {
      waitForExecutedJobWithRetriesLeft(retriesLeft);
    }
  }
  
  private void stillOneJobWithExceptionAndRetriesLeft() {
    assertEquals(1, managementService.createTimerJobQuery().withException().count());
    assertEquals(1, managementService.createTimerJobQuery().count());
  }
  
  private Job fetchJob(String processInstanceId) {
    return managementService.createTimerJobQuery().processInstanceId(processInstanceId).singleResult();
  }
  
  private ExecutionEntity fetchExecutionEntity(String processInstanceId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId(processInstanceId).singleResult();
  }
  
  private Job refreshJob(String jobId) {
    return managementService.createTimerJobQuery().jobId(jobId).singleResult();
  }
  private ExecutionEntity refreshExecutionEntity(String executionId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
  }

}
