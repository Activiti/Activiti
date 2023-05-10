/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests specifically for the {@link AsyncExecutor}.
 *
 */
public class AsyncExecutorTest {

  @Test
  public void testRegularAsyncExecution() {

    ProcessEngine processEngine = null;

    try {
      // Deploy
      processEngine = createProcessEngine(true);
      setClockToCurrentTime(processEngine);
      deploy(processEngine, "AsyncExecutorTest.testRegularAsyncExecution.bpmn20.xml");

      // Start process instance. Wait for all jobs to be done
      processEngine.getRuntimeService().startProcessInstanceByKey("asyncExecutor");

      // Move clock 3 minutes. Nothing should happen
      addSecondsToCurrentTime(processEngine, 180L);
      ProcessEngine processEngineForException = processEngine;
      waitForAllJobsBeingExecuted(processEngineForException, 500L);
      assertThat(processEngine.getTaskService().createTaskQuery().taskName("The Task").count()).isEqualTo(1);
      assertThat(processEngine.getTaskService().createTaskQuery().taskName("Task after timer").count()).isEqualTo(0);
      assertThat(processEngine.getManagementService().createTimerJobQuery().count()).isEqualTo(1);
      assertThat(getAsyncExecutorJobCount(processEngine)).isEqualTo(0);

      // Move clock 3 minutes and 1 second. Triggers the timer
      addSecondsToCurrentTime(processEngine, 181);
      waitForAllJobsBeingExecuted(processEngine);

      // Verify if all is as expected
      assertThat(processEngine.getTaskService().createTaskQuery().taskName("The Task").count()).isEqualTo(0);
      assertThat(processEngine.getTaskService().createTaskQuery().taskName("Task after timer").count()).isEqualTo(1);
      assertThat(processEngine.getManagementService().createTimerJobQuery().count()).isEqualTo(0);
      assertThat(processEngine.getManagementService().createJobQuery().count()).isEqualTo(0);

      assertThat(getAsyncExecutorJobCount(processEngine)).isEqualTo(1);
    } finally {
      // Clean up
      if (processEngine != null) {
        cleanup(processEngine);
      }
    }
  }

  @Test
  public void testAsyncExecutorDisabledOnOneEngine() {

    ProcessEngine firstProcessEngine = null;
    ProcessEngine secondProcessEngine = null;

    try {

      // Deploy on one engine, where the async executor is disabled
      firstProcessEngine = createProcessEngine(false);
      Date now = setClockToCurrentTime(firstProcessEngine);
      deploy(firstProcessEngine, "AsyncExecutorTest.testRegularAsyncExecution.bpmn20.xml");

      // Start process instance on first engine
      firstProcessEngine.getRuntimeService().startProcessInstanceByKey("asyncExecutor");

      // Move clock 5 minutes and 1 second. Triggers the timer normally,
      // but not now since async execution is disabled
      addSecondsToCurrentTime(firstProcessEngine, 301); // 301 = 5m01s
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("The Task").count()).isEqualTo(1);
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after timer").count()).isEqualTo(0);
      assertThat(firstProcessEngine.getManagementService().createTimerJobQuery().count()).isEqualTo(1);

      // Create second engine, with async executor enabled. Same time as
      // the first engine to start, then add 301 seconds
      secondProcessEngine = createProcessEngine(true, now);
      addSecondsToCurrentTime(secondProcessEngine, 361);
      waitForAllJobsBeingExecuted(secondProcessEngine);

      // Verify if all is as expected
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("The Task").count()).isEqualTo(0);
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after timer").count()).isEqualTo(1);
      assertThat(firstProcessEngine.getManagementService().createTimerJobQuery().count()).isEqualTo(0);
      assertThat(firstProcessEngine.getManagementService().createJobQuery().count()).isEqualTo(0);

      assertThat(getAsyncExecutorJobCount(firstProcessEngine)).isEqualTo(0);
      assertThat(getAsyncExecutorJobCount(secondProcessEngine)).isEqualTo(1);

    } finally {

      // Clean up
      cleanup(firstProcessEngine);
      cleanup(secondProcessEngine);

    }

  }

  @Test
  public void testAsyncScriptExecution() {

    ProcessEngine processEngine = null;

    try {

      // Deploy
      processEngine = createProcessEngine(true);
      setClockToCurrentTime(processEngine);
      deploy(processEngine, "AsyncExecutorTest.testAsyncScriptExecution.bpmn20.xml");

      // Start process instance. Wait for all jobs to be done
      ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");
      waitForAllJobsBeingExecuted(processEngine);

      // Verify if all is as expected
      assertThat(processEngine.getManagementService().createJobQuery().count()).isEqualTo(0);
      assertThat(processEngine.getManagementService().createTimerJobQuery().count()).isEqualTo(0);
      assertThat(processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);
      assertThat(processEngine.getTaskService().createTaskQuery().taskName("Task after script").count()).isEqualTo(1);

      assertThat(getAsyncExecutorJobCount(processEngine)).isEqualTo(1);

    } finally {

      // Clean up
      cleanup(processEngine);

    }

  }

  //TODO enable this test again: temporary disabled because it's randomly failing
  @Ignore
  @Test
  public void testAsyncScriptExecutionOnTwoEngines() {

    ProcessEngine firstProcessEngine = null;
    ProcessEngine secondProcessEngine = null;

    try {

      // Deploy
      firstProcessEngine = createProcessEngine(false);
      Date now = setClockToCurrentTime(firstProcessEngine);
      deploy(firstProcessEngine, "AsyncExecutorTest.testAsyncScriptExecution.bpmn20.xml");

      // Start process instance. Nothing should happen
      firstProcessEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after script").count()).isEqualTo(0);
      assertThat(firstProcessEngine.getManagementService().createJobQuery().count()).isEqualTo(1);

      // Start second engine, with async executor enabled
      secondProcessEngine = createProcessEngine(true, now); // Same timestamp as first engine
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after script").count()).isEqualTo(0);
      assertThat(firstProcessEngine.getManagementService().createJobQuery().count()).isEqualTo(1);

      // Move the clock 1 second. Should be executed now by second engine
      addSecondsToCurrentTime(secondProcessEngine, 1);
      waitForAllJobsBeingExecuted(secondProcessEngine, 10000L);

      // Verify if all is as expected
      assertThat(firstProcessEngine.getTaskService().createTaskQuery().taskName("Task after script").count()).isEqualTo(1);
      assertThat(firstProcessEngine.getManagementService().createJobQuery().count()).isEqualTo(0);

      assertThat(getAsyncExecutorJobCount(firstProcessEngine)).isEqualTo(0);
      assertThat(getAsyncExecutorJobCount(secondProcessEngine)).isEqualTo(1);

    } finally {

      // Clean up
      cleanup(firstProcessEngine);
      cleanup(secondProcessEngine);

    }

  }

  @Test
  public void testAsyncFailingScript() {

    ProcessEngine processEngine = null;

    try {

      // Deploy
      processEngine = createProcessEngine(true);
      processEngine.getProcessEngineConfiguration().getClock().reset();
      deploy(processEngine, "AsyncExecutorTest.testAsyncFailingScript.bpmn20.xml");

      // There is a back off mechanism for the retry, so need a bit of
      // time. But to be sure, we make the wait time small
      processEngine.getProcessEngineConfiguration().setAsyncFailedJobWaitTime(1);
      processEngine.getProcessEngineConfiguration().setDefaultFailedJobWaitTime(1);

      // Start process instance. Wait for all jobs to be done.
      processEngine.getRuntimeService().startProcessInstanceByKey("asyncScript");

      final ProcessEngine processEngineCopy = processEngine;
      JobTestHelper.waitForJobExecutorOnCondition(processEngine.getProcessEngineConfiguration(), 10000L, () -> {
        long timerJobCount = processEngineCopy.getManagementService().createTimerJobQuery().count();
        if (timerJobCount == 0) {
          return processEngineCopy.getManagementService().createJobQuery().count() == 0;
        } else {
          return false;
        }
      });

      // Verify if all is as expected
      assertThat(processEngine.getTaskService().createTaskQuery().taskName("Task after script").count()).isEqualTo(0);
      assertThat(processEngine.getManagementService().createJobQuery().count()).isEqualTo(0);
      assertThat(processEngine.getManagementService().createDeadLetterJobQuery().count()).isEqualTo(1);

      assertThat(getAsyncExecutorJobCount(processEngine)).isEqualTo(3);

    } finally {

      // Clean up
      cleanup(processEngine);
    }
  }

  // Helpers ////////////////////////////////////////////////////////

  private ProcessEngine createProcessEngine(boolean enableAsyncExecutor) {
    return createProcessEngine(enableAsyncExecutor, null);
  }

  private ProcessEngine createProcessEngine(boolean enableAsyncExecutor, Date time) {
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-AsyncExecutorTest;DB_CLOSE_DELAY=1000");
    processEngineConfiguration.setDatabaseSchemaUpdate("true");

    if (enableAsyncExecutor) {
      processEngineConfiguration.setAsyncExecutorActivate(true);

      CountingAsyncExecutor countingAsyncExecutor = new CountingAsyncExecutor();
      countingAsyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(50); // To avoid waiting too long when a retry happens
      countingAsyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(50);
      processEngineConfiguration.setAsyncExecutor(countingAsyncExecutor);
    }

    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

    if (time != null) {
      processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(time);
    }

    return processEngine;
  }

  private Date setClockToCurrentTime(ProcessEngine processEngine) {
    Date date = new Date();
    processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(date);
    return date;
  }

  private void addSecondsToCurrentTime(ProcessEngine processEngine, long nrOfSeconds) {
    Date currentTime = processEngine.getProcessEngineConfiguration().getClock().getCurrentTime();
    processEngine.getProcessEngineConfiguration().getClock().setCurrentTime(new Date(currentTime.getTime() + (nrOfSeconds * 1000L)));
  }

  private void cleanup(ProcessEngine processEngine) {
      if(processEngine != null) {
          for (org.activiti.engine.repository.Deployment deployment : processEngine.getRepositoryService().createDeploymentQuery().list()) {
              processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
          }
          processEngine.close();
      }
  }

  private String deploy(ProcessEngine processEngine, String resource) {
    return processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/engine/test/jobexecutor/" + resource).deploy().getId();
  }

  private void waitForAllJobsBeingExecuted(ProcessEngine processEngine) {
    waitForAllJobsBeingExecuted(processEngine, 10000L);
  }

  private void waitForAllJobsBeingExecuted(ProcessEngine processEngine, long maxWaitTime) {
    JobTestHelper.waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(processEngine.getProcessEngineConfiguration(), processEngine.getManagementService(), maxWaitTime, false);
  }

  private int getAsyncExecutorJobCount(ProcessEngine processEngine) {
    AsyncExecutor asyncExecutor = processEngine.getProcessEngineConfiguration().getAsyncExecutor();
    if (asyncExecutor instanceof CountingAsyncExecutor) {
      return ((CountingAsyncExecutor) asyncExecutor).getCounter().get();
    }
    return 0;
  }

  static class CountingAsyncExecutor extends DefaultAsyncJobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CountingAsyncExecutor.class);

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean executeAsyncJob(Job job) {
      logger.info("About to execute job " + job.getId());
      counter.incrementAndGet();
      boolean success = super.executeAsyncJob(job);
      logger.info("Handed off job " + job.getId() + " to async executor (retries=" + job.getRetries() + ")");
      return success;
    }

    public AtomicInteger getCounter() {
      return counter;
    }

    public void setCounter(AtomicInteger counter) {
      this.counter = counter;
    }

  }

}
