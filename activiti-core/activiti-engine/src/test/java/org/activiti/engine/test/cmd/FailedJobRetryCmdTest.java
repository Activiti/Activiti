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
package org.activiti.engine.test.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**

 */
public class FailedJobRetryCmdTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/cmd/FailedJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void testFailedServiceTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("failedServiceTask");
    assertThat(pi).isNotNull();
    waitForExecutedJobWithRetriesLeft(4);

    stillOneJobWithExceptionAndRetriesLeft();

    Job job = fetchJob(pi.getProcessInstanceId());
    assertThat(job).isNotNull();
    assertThat(job.getProcessInstanceId()).isEqualTo(pi.getProcessInstanceId());

    assertThat(job.getRetries()).isEqualTo(4);

    Execution execution = runtimeService.createExecutionQuery().onlyChildExecutions().processInstanceId(pi.getId()).singleResult();
    assertThat(execution.getActivityId()).isEqualTo("failingServiceTask");

    waitForExecutedJobWithRetriesLeft(3);

    job = refreshJob(job.getId());
    assertThat(job.getRetries()).isEqualTo(3);
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId()).isEqualTo("failingServiceTask");

    waitForExecutedJobWithRetriesLeft(2);

    job = refreshJob(job.getId());
    assertThat(job.getRetries()).isEqualTo(2);
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId()).isEqualTo("failingServiceTask");

    waitForExecutedJobWithRetriesLeft(1);

    job = refreshJob(job.getId());
    assertThat(job.getRetries()).isEqualTo(1);
    stillOneJobWithExceptionAndRetriesLeft();

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId()).isEqualTo("failingServiceTask");

    waitForExecutedJobWithRetriesLeft(0);

    job = managementService.createDeadLetterJobQuery().jobId(job.getId()).singleResult();
    assertThat(job.getRetries()).isEqualTo(0);
    assertThat(managementService.createDeadLetterJobQuery().withException().count()).isEqualTo(1);
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
    assertThat(managementService.createDeadLetterJobQuery().count()).isEqualTo(1);

    execution = refreshExecutionEntity(execution.getId());
    assertThat(execution.getActivityId()).isEqualTo("failingServiceTask");

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

  protected void stillOneJobWithExceptionAndRetriesLeft() {
    assertThat(managementService.createTimerJobQuery().withException().count()).isEqualTo(1);
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
  }

  protected Job fetchJob(String processInstanceId) {
    return managementService.createTimerJobQuery().processInstanceId(processInstanceId).singleResult();
  }

  protected Job refreshJob(String jobId) {
    return managementService.createTimerJobQuery().jobId(jobId).singleResult();
  }

  protected ExecutionEntity refreshExecutionEntity(String executionId) {
    return (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
  }

}
