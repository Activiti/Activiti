/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 */
public class ManagementServiceTest extends PluggableActivitiTestCase {

  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertThat(metaData).isNull();
  }

  public void testGetMetaDataNullTableName() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.getTableMetaData(null))
      .withMessageContaining("tableName is null");
  }

  public void testExecuteJobNullJobId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.executeJob(null))
      .withMessageContaining("JobId is null");
  }

  public void testExecuteJobUnexistingJob() {
    assertThatExceptionOfType(JobNotFoundException.class)
      .isThrownBy(() -> managementService.executeJob("unexistingjob"))
      .withMessageContaining("No job found with id")
      .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Job.class));
  }

  @Deployment
  public void testGetJobExceptionStacktrace() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(timerJob).as("No job found for process instance").isNotNull();

    assertThatExceptionOfType(RuntimeException.class)
      .as("RuntimeException from within the script task expected")
      .isThrownBy(() -> {
        managementService.moveTimerToExecutableJob(timerJob.getId());
        managementService.executeJob(timerJob.getId());
      })
      .withMessageContaining("This is an exception thrown from scriptTask");

    // Fetch the task to see that the exception that occurred is persisted
    Job reloadedTimerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(reloadedTimerJob).isNotNull();
    assertThat(reloadedTimerJob.getExceptionMessage()).isNotNull();
    assertThat(reloadedTimerJob.getExceptionMessage()).contains("This is an exception thrown from scriptTask");

    // Get the full stacktrace using the managementService
    String exceptionStack = managementService.getTimerJobExceptionStacktrace(reloadedTimerJob.getId());
    assertThat(exceptionStack).isNotNull();
    assertThat(exceptionStack).contains("This is an exception thrown from scriptTask");
  }

  public void testGetJobExceptionStacktraceUnexistingJobId() {
    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> managementService.getJobExceptionStacktrace("unexistingjob"))
      .withMessageContaining("No job found with id unexistingjob")
      .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Job.class));
  }

  public void testgetJobExceptionStacktraceNullJobId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.getJobExceptionStacktrace(null))
      .withMessageContaining("jobId is null");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml" })
  public void testSetJobRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary timer event.
    Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    Date duedate = timerJob.getDuedate();

    assertThat(timerJob).as("No job found for process instance").isNotNull();
    assertThat(timerJob.getRetries()).isEqualTo(processEngineConfiguration.getAsyncExecutorNumberOfRetries());

    managementService.setTimerJobRetries(timerJob.getId(), 5);

    timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(timerJob.getRetries()).isEqualTo(5);
    assertThat(timerJob.getDuedate()).isEqualTo(duedate);
  }

  public void testSetJobRetriesUnexistingJobId() {
    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> managementService.setJobRetries("unexistingjob", 5))
      .withMessageContaining("No job found with id 'unexistingjob'.")
      .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Job.class));
  }

  public void testSetJobRetriesEmptyJobId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.setJobRetries("", 5))
      .withMessageContaining("The job id is mandatory, but '' has been provided.");
  }

  public void testSetJobRetriesJobIdNull() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.setJobRetries(null, 5))
      .withMessageContaining("The job id is mandatory, but 'null' has been provided.");
  }

  public void testSetJobRetriesNegativeNumberOfRetries() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.setJobRetries("unexistingjob", -1))
      .withMessageContaining("The number of job retries must be a non-negative Integer, but '-1' has been provided.");
  }

  public void testDeleteJobNullJobId() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> managementService.deleteJob(null))
      .withMessageContaining("jobId is null");
  }

  public void testDeleteJobUnexistingJob() {
    assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
      .isThrownBy(() -> managementService.deleteJob("unexistingjob"))
      .withMessageContaining("No job found with id")
      .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Job.class));
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobDeletion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(timerJob).as("Task timer should be there").isNotNull();
    managementService.deleteTimerJob(timerJob.getId());

    timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(timerJob).as("There should be no job now. It was deleted").isNull();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobThatWasAlreadyAcquired() {
    processEngineConfiguration.getClock().setCurrentTime(new Date());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    // We need to move time at least one hour to make the timer executable
    processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + 7200000L));

    // Acquire job by running the acquire command manually
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) processEngine;
    AcquireTimerJobsCmd acquireJobsCmd = new AcquireTimerJobsCmd(processEngine.getProcessEngineConfiguration().getAsyncExecutor());
    CommandExecutor commandExecutor = processEngineImpl.getProcessEngineConfiguration().getCommandExecutor();
    commandExecutor.execute(acquireJobsCmd);

    // Try to delete the job. This should fail.
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> managementService.deleteJob(timerJob.getId()));

    // Clean up
    managementService.moveTimerToExecutableJob(timerJob.getId());
    managementService.executeJob(timerJob.getId());
  }

  // https://jira.codehaus.org/browse/ACT-1816:
  // ManagementService doesn't seem to give actual table Name for EventSubscriptionEntity.class
  public void testGetTableName() {
    String table = managementService.getTableName(EventSubscriptionEntity.class);
    assertThat(table).isEqualTo("ACT_RU_EVENT_SUBSCR");
  }
}
