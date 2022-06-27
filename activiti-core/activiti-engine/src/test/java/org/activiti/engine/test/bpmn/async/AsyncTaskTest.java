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

package org.activiti.engine.test.bpmn.async;

import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 *

 */
public class AsyncTaskTest extends PluggableActivitiTestCase {

  public static boolean INVOCATION;

  @Deployment
  public void testAsyncServiceNoListeners() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    // the service was not invoked:
    assertThat(INVOCATION).isFalse();

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service was invoked
    assertThat(INVOCATION).isTrue();
    // and the job is done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testAsyncServiceListeners() {
    String pid = runtimeService.startProcessInstanceByKey("asyncService").getProcessInstanceId();
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    // the listener was not yet invoked:
    assertThat(runtimeService.getVariable(pid, "listener")).isNull();

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testAsyncServiceConcurrent() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    // the service was not invoked:
    assertThat(INVOCATION).isFalse();

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service was invoked
    assertThat(INVOCATION).isTrue();
    // and the job is done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testAsyncServiceMultiInstance() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    // the service was not invoked:
    assertThat(INVOCATION).isFalse();

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service was invoked
    assertThat(INVOCATION).isTrue();
    // and the job is done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testFailingAsyncServiceTimer() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be one job in the database, and it is a message
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    Job job = managementService.createJobQuery().singleResult();

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> managementService.executeJob(job.getId()));

    // the service failed: the execution is still sitting in the service task:
    Execution execution = null;
    for (Execution e : runtimeService.createExecutionQuery().list()) {
      if (e.getParentId() != null) {
        execution = e;
      }
    }
    assertThat(execution).isNotNull();
    assertThat(runtimeService.getActiveActivityIds(execution.getId()).get(0)).isEqualTo("service");

    // there is still a single job because the timer was created in the same
    // transaction as the service was executed (which rolled back)
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

    runtimeService.deleteProcessInstance(execution.getId(), "dead");
  }

  // TODO: Think about this:
  @Deployment
  public void FAILING_testFailingAsycServiceTimer() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there are two jobs the message and a timer:
    assertThat(managementService.createJobQuery().count()).isEqualTo(2);

    // let 'max-retires' on the message be reached
    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // the service failed: the execution is still sitting in the service
    // task:
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    assertThat(execution).isNotNull();
    assertThat(runtimeService.getActiveActivityIds(execution.getId()).get(0)).isEqualTo("service");

    // there are tow jobs, the message and the timer (the message will not
    // be retried anymore, max retires is reached.)
    assertThat(managementService.createJobQuery().count()).isEqualTo(2);

    // now the timer triggers:
    Context.getProcessEngineConfiguration().getClock().setCurrentTime(new Date(System.currentTimeMillis() + 10000));
    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // and we are done:
    assertThat(runtimeService.createExecutionQuery().singleResult()).isNull();
    // and there are no more jobs left:
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);

  }

  @Deployment
  public void testAsyncServiceSubProcessTimer() {
    INVOCATION = false;
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");
    // now there should be two jobs in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    // the service was not invoked:
    assertThat(INVOCATION).isFalse();

    waitForJobExecutorToProcessAllJobs(5000L, 200L);

    // the service was invoked
    assertThat(INVOCATION).isTrue();

    // both the timer and the message are cancelled
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testAsyncServiceSubProcess() {
    // start process
    runtimeService.startProcessInstanceByKey("asyncService");

    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // both the timer and the message are cancelled
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);

  }

  @Deployment
  public void testAsyncTask() throws InterruptedException {
    AsyncExecutor asyncExecutor = processEngineConfiguration.getAsyncExecutor();

    try {
        // start process
        runtimeService.startProcessInstanceByKey("asyncTask");

        // now there should be one job in the database:
        assertThat(managementService.createJobQuery().count()).isEqualTo(1);

        // Let's start async executor
        asyncExecutor.start();

        // Let's wait for all executions to complete
        waitForAllExecutionsToComplete(5000L, 200L);

        // the job is done
        assertThat(managementService.createJobQuery().count()).isEqualTo(0);

    } finally {
        asyncExecutor.shutdown();
    }
  }

  @Deployment
  public void testAsyncEndEvent() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncEndEvent");
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    Object value = runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    assertThat(value).isNull();

    waitForJobExecutorToProcessAllJobs(2000L, 200L);

    // the job is done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);

    assertProcessEnded(processInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
      assertThat(variables).hasSize(3);

      Object historyValue = null;
      for (HistoricVariableInstance variable : variables) {
        if ("variableSetInExecutionListener".equals(variable.getVariableName())) {
          historyValue = variable.getValue();
        }
      }
      assertThat(historyValue).isEqualTo("firstValue");
    }
  }

  @Deployment
  public void testAsyncScript() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncScript");
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    // the script was not invoked:
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    String eid = null;
    for (Execution e : executions) {
      if (e.getParentId() != null) {
        eid = e.getId();
      }
    }
    assertThat(runtimeService.getVariable(eid, "invoked")).isNull();

    waitForJobExecutorToProcessAllJobs(5000L, 100L);

    // and the job is done
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);

    // the script was invoked
    assertThat(runtimeService.getVariable(eid, "invoked")).isEqualTo("true");

    runtimeService.trigger(eid);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsyncCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsyncServiceNoListeners.bpmn20.xml" })
  public void testAsyncCallActivity() throws Exception {
    // start process
    runtimeService.startProcessInstanceByKey("asyncCallactivity");
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    waitForJobExecutorToProcessAllJobs(20000L, 250L);

    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/async/AsyncTaskTest.testBasicAsyncCallActivity.bpmn20.xml", "org/activiti/engine/test/bpmn/StartToEndTest.testStartToEnd.bpmn20.xml" })
  public void testBasicAsyncCallActivity() {
    runtimeService.startProcessInstanceByKey("myProcess");
    assertThat(managementService.createJobQuery().count()).as("There should be one job available.").isEqualTo(1);
    waitForJobExecutorToProcessAllJobs(5000L, 250L);
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testAsyncUserTask() {
    // start process
    String pid = runtimeService.startProcessInstanceByKey("asyncUserTask").getId();
    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
    // the listener was not yet invoked:
    assertThat(runtimeService.getVariable(pid, "listener")).isNull();
    // the task listener was not yet invoked:
    assertThat(runtimeService.getVariable(pid, "taskListener")).isNull();
    // there is no usertask
    assertThat(taskService.createTaskQuery().singleResult()).isNull();

    waitForJobExecutorToProcessAllJobs(5000L, 250L);
    // the listener was now invoked:
    assertThat(runtimeService.getVariable(pid, "listener")).isNotNull();
    // the task listener was now invoked:
    assertThat(runtimeService.getVariable(pid, "taskListener")).isNotNull();

    // there is a usertask
    assertThat(taskService.createTaskQuery().singleResult()).isNotNull();
    // and no more job
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);

  }

  @Deployment
  public void testMultiInstanceAsyncTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");

    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(3);

    // execute first of 3 parallel multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).list().get(0).getId());
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(2);

    // execute second of 3 parallel multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).list().get(0).getId());
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

    // execute third of 3 parallel multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());

    // the job is done
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();

      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;

        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;

        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;

        } else {
          fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }

      assertThat(startCount).isEqualTo(1);
      assertThat(taskCount).isEqualTo(3);
      assertThat(endCount).isEqualTo(1);
    }
  }

  @Deployment
  public void testMultiInstanceTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();

      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;

        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;

        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;

        } else {
          fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }

      assertThat(startCount).isEqualTo(1);
      assertThat(taskCount).isEqualTo(3);
      assertThat(endCount).isEqualTo(1);
    }
  }

  @Deployment
  public void testMultiInstanceAsyncSequentialTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");

    // now there should be one job in the database:
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

    // execute first of 3 sequential multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

    // execute second of 3 sequential multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

    // execute third of 3 sequential multi instance tasks
    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());

    // the job is done
    assertThat(managementService.createJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();

      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;

        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;

        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;

        } else {
          fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }

      assertThat(startCount).isEqualTo(1);
      assertThat(taskCount).isEqualTo(3);
      assertThat(endCount).isEqualTo(1);
    }
  }

  @Deployment
  public void testMultiInstanceSequentialTask() {
    // start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTask");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();

      int startCount = 0;
      int taskCount = 0;
      int endCount = 0;
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        if ("task".equals(historicActivityInstance.getActivityId())) {
          taskCount++;

        } else if ("theStart".equals(historicActivityInstance.getActivityId())) {
          startCount++;

        } else if ("theEnd".equals(historicActivityInstance.getActivityId())) {
          endCount++;

        } else {
          fail("Unexpected activity found " + historicActivityInstance.getActivityId());
        }
      }

      assertThat(startCount).isEqualTo(1);
      assertThat(taskCount).isEqualTo(3);
      assertThat(endCount).isEqualTo(1);
    }
  }

  private void waitForAllExecutionsToComplete(long timeout, long sleep) throws InterruptedException {
      long counter = 0;

      while(runtimeService.createExecutionQuery().count() > 0) {
          Thread.sleep(sleep);
          counter += sleep;

          // timeout
          assertThat(counter)
              .as("Should have finished all process executions within " + timeout + " ms")
              .isLessThan(timeout);
      }
  }

}
