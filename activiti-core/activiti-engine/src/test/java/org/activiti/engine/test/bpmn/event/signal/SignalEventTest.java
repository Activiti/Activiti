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

package org.activiti.engine.test.bpmn.event.signal;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.validation.validator.Problems;

/**
 */
public class SignalEventTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml" })
  public void testSignalCatchIntermediate() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalExpression.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalExpression.bpmn20.xml" })
  public void testSignalCatchIntermediateExpression() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("mySignalName", "testSignal");
    runtimeService.startProcessInstanceByKey("catchSignal", variableMap);

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    runtimeService.startProcessInstanceByKey("throwSignal", variableMap);

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundary.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml" })
  public void testSignalCatchBoundary() {
    runtimeService.startProcessInstanceByKey("catchSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml" })
  public void testSignalCatchBoundaryWithVariables() {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("catchSignal", variables1);

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    runtimeService.startProcessInstanceByKey("throwSignal", variables2);

    assertThat(runtimeService.getVariable(pi.getId(), "processName")).isEqualTo("throwSignal");
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsynch.bpmn20.xml" })
  public void testSignalCatchIntermediateAsynch() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    // there is a job:
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    try {
      processEngineConfiguration.getClock().setCurrentTime(new Date(System.currentTimeMillis() + 1000));
      waitForJobExecutorToProcessAllJobs(10000, 100l);

      assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
      assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
      assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    } finally {
      processEngineConfiguration.getClock().setCurrentTime(new Date());
    }

  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchMultipleSignals.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml", "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAbortSignal.bpmn20.xml" })
  public void testSignalCatchDifferentSignals() {

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(2);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    runtimeService.startProcessInstanceByKey("throwAbort");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    Task taskAfterAbort = taskService.createTaskQuery().taskAssignee("gonzo").singleResult();
    assertThat(taskAfterAbort).isNotNull();
    taskService.complete(taskAfterAbort.getId());

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  /**
   * Verifies the solution of https://jira.codehaus.org/browse/ACT-1309
   */
  @Deployment
  public void testSignalBoundaryOnSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("signalEventOnSubprocess");
    runtimeService.signalEventReceived("stopSignal");
    assertProcessEnded(pi.getProcessInstanceId());
  }

  public void testDuplicateSignalNames() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.duplicateSignalNames.bpmn20.xml").deploy())
      .withMessageContaining(Problems.SIGNAL_DUPLICATE_NAME);
  }

  public void testNoSignalName() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.noSignalName.bpmn20.xml")
        .deploy())
      .withMessageContaining(Problems.SIGNAL_MISSING_NAME);
  }

  public void testSignalNoId() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.signalNoId.bpmn20.xml")
        .deploy())
      .withMessageContaining(Problems.SIGNAL_MISSING_ID);
  }

  public void testSignalNoRef() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.signalNoRef.bpmn20.xml")
        .deploy())
      .withMessageContaining(Problems.SIGNAL_EVENT_MISSING_SIGNAL_REF);
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }

  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  public void testNonInterruptingSignal() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalEvent");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(1);
    Task currentTask = tasks.get(0);
    assertThat(currentTask.getName()).isEqualTo("My User Task");

    runtimeService.signalEventReceived("alert");

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(2);

    for (Task task : tasks) {
      assertThat(task.getName()).isIn("My User Task", "My Second User Task");
    }

    taskService.complete(taskService.createTaskQuery().taskName("My User Task").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(1);
    currentTask = tasks.get(0);
    assertThat(currentTask.getName()).isEqualTo("My Second User Task");
  }

  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  public void testNonInterruptingSignalWithSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalWithSubProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(1);

    Task currentTask = tasks.get(0);
    assertThat(currentTask.getName()).isEqualTo("Approve");

    runtimeService.signalEventReceived("alert");

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(2);

    for (Task task : tasks) {
      assertThat(task.getName()).isIn("Approve", "Review");
    }

    taskService.complete(taskService.createTaskQuery().taskName("Approve").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(1);

    currentTask = tasks.get(0);
    assertThat(currentTask.getName()).isEqualTo("Review");

    taskService.complete(taskService.createTaskQuery().taskName("Review").singleResult().getId());

    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertThat(tasks).hasSize(1);
  }

  @Deployment
  public void testUseSignalForExceptionsBetweenParallelPaths() {
    runtimeService.startProcessInstanceByKey("processWithSignal");

    // First task should be to select the developers
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Enter developers");
    taskService.complete(task.getId(), singletonMap("developers", asList("developerOne", "developerTwo", "developerThree")));

    // Should be three distinct tasks for each developer
    assertThat(taskService.createTaskQuery().taskAssignee("developerOne").singleResult().getName()).isEqualTo("Develop specifications");
    assertThat(taskService.createTaskQuery().taskAssignee("developerTwo").singleResult().getName()).isEqualTo("Develop specifications");
    assertThat(taskService.createTaskQuery().taskAssignee("developerThree").singleResult().getName()).isEqualTo("Develop specifications");

    // Negotiate with client is a task for kermit
    task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertThat(task.getName()).isEqualTo("Negotiate with client");

    // When the kermit task is completed, it throws a signal which should
    // cancel the multi instance
    taskService.complete(task.getId(), singletonMap("negotationFailed", true));

    // No tasks should be open then and process should have ended
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testSignalWithProcessInstanceScope() {
    // Start the process that catches the signal
    ProcessInstance processInstanceCatch = runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertThat(taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName()).isEqualTo("userTaskWithSignalCatch");

    // Then start the process that will throw the signal
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");

    // Since the signal is process instance scoped, the second process
    // shouldn't have proceeded in any way
    assertThat(taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName()).isEqualTo("userTaskWithSignalCatch");

    // Let's try to trigger the catch using the API, that should also fail
    runtimeService.signalEventReceived("The Signal");
    assertThat(taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName()).isEqualTo("userTaskWithSignalCatch");
  }

  @Deployment
  public void testSignalWithGlobalScope() {
    // Start the process that catches the signal
    ProcessInstance processInstanceCatch = runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertThat(taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName()).isEqualTo("userTaskWithSignalCatch");

    // Then start the process that will throw thee signal
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");

    // Since the signal is process instance scoped, the second process
    // shouldn't have proceeded in any way
    assertThat(taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName()).isEqualTo("userTaskAfterSignalCatch");
  }

  @Deployment
  public void testAsyncTriggeredSignalEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithSignalCatch");

    assertThat(processInstance).isNotNull();
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).signalEventSubscriptionName("The Signal").singleResult();
    assertThat(execution).isNotNull();
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(2);

    runtimeService.signalEventReceivedAsync("The Signal", execution.getId());

    assertThat(managementService.createJobQuery().messages().count()).isEqualTo(1);

    waitForJobExecutorToProcessAllJobs(8000L, 200L);
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testSignalUserTask() {
    runtimeService.startProcessInstanceByKey("catchSignal");
    Execution execution = runtimeService.createExecutionQuery().onlyChildExecutions().activityId("waitState").singleResult();

    assertThat(execution).isNotNull();

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.trigger(execution.getId()));
  }

  public void testSignalStartEventFromProcess() {

    // Deploy test processes
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEvent.bpmn20.xml").deploy();

    // Starting the process that fires the signal should start three process instances that are listening on that signal
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");

    // Verify
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(3);

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    List<String> names = asList("A", "B", "C");
    for (int i = 0; i < tasks.size(); i++) {
      assertThat(tasks.get(i).getName()).isEqualTo("Task in process " + names.get(i));
    }

    // Start a process with a signal boundary event
    runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().taskName("Task in process D").count()).isEqualTo(1);

    // Firing the signal should now trigger the one with the boundary event
    // too
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().taskName("Task after signal").count()).isEqualTo(1);

    // Cleanup
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  public void testSignalStartEventFromProcesAsync() {

    // Deploy test processes
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEventAsync.bpmn20.xml").deploy();

    // Starting the process that fires the signal should start 1 process
    // instance that are listening on that signal, the others are done async
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");

    // Verify
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);

    assertThat(managementService.createJobQuery().count()).isEqualTo(3);
    for (Job job : managementService.createJobQuery().list()) {
      managementService.executeJob(job.getId());
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(3);

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    List<String> names = asList("A", "B", "C");
    for (int i = 0; i < tasks.size(); i++) {
      assertThat(tasks.get(i).getName()).isEqualTo("Task in process " + names.get(i));
    }

    // Start a process with a signal boundary event
    runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().taskName("Task in process D").count()).isEqualTo(1);

    // Firing again
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");

    assertThat(managementService.createJobQuery().count()).isEqualTo(4);
    for (Job job : managementService.createJobQuery().list()) {
      managementService.executeJob(job.getId());
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().taskName("Task after signal").count()).isEqualTo(1);

    // Cleanup
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  public void testSignalStartEventFromAPI() {

    // Deploy test processes
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEvent.bpmn20.xml").deploy();

    runtimeService.signalEventReceived("The Signal");

    // Verify
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(3);

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    List<String> names = asList("A", "B", "C");
    for (int i = 0; i < tasks.size(); i++) {
      assertThat(tasks.get(i).getName()).isEqualTo("Task in process " + names.get(i));
    }

    // Start a process with a signal boundary event
    runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().taskName("Task in process D").count()).isEqualTo(1);

    // Firing the signal should now trigger the one with the boundary event
    // too
    runtimeService.signalEventReceived("The Signal");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().taskName("Task after signal").count()).isEqualTo(1);

    // Cleanup
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  public void testSignalStartEventFromAPIAsync() {

    // Deploy test processes
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEventAsync.bpmn20.xml").deploy();

    runtimeService.signalEventReceivedAsync("The Signal");

    assertThat(managementService.createJobQuery().count()).isEqualTo(3);
    for (Job job : managementService.createJobQuery().list()) {
      managementService.executeJob(job.getId());
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(3);

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    List<String> names = asList("A", "B", "C");
    for (int i = 0; i < tasks.size(); i++) {
      assertThat(tasks.get(i).getName()).isEqualTo("Task in process " + names.get(i));
    }

    // Start a process with a signal boundary event
    runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().taskName("Task in process D").count()).isEqualTo(1);

    // Firing again
    runtimeService.signalEventReceivedAsync("The Signal");

    assertThat(managementService.createJobQuery().count()).isEqualTo(4);
    for (Job job : managementService.createJobQuery().list()) {
      managementService.executeJob(job.getId());
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(7);
    assertThat(taskService.createTaskQuery().taskName("Task after signal").count()).isEqualTo(1);

    // Cleanup
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  public void testSignalStartEventSuspendedProcessDefinition() {

    // Deploy test processes
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEvent.bpmn20.xml")
      .deploy();

    repositoryService.suspendProcessDefinitionByKey("processWithSignalStart1");

    assertThatExceptionOfType(ActivitiException.class)
      .as("ActivitiException expected. Process definition is suspended")
      .isThrownBy(() -> runtimeService.signalEventReceived("The Signal"));

    // Cleanup
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

  }

  @Deployment
  public void testEarlyFinishedProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callerProcess");
    assertThat(processInstance.getId()).isNotNull();
  }

  @Deployment
  public void testNoneEndEventAfterSignalInConcurrentProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
    assertThat(processInstance).isNotNull();

    Task task = taskService.createTaskQuery().taskDefinitionKey("usertask1").singleResult();
    taskService.claim(task.getId(), "user");
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();

    assertThat(task.getTaskDefinitionKey()).isEqualTo("usertask2");
  }

  /**
   * Test case for https://activiti.atlassian.net/browse/ACT-1978
   */
  public void testSignalDeleteOnRedeploy() {

    // Deploy test processes
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEvent.bpmn20.xml").deploy();

    // Deploy new versions
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEvent.bpmn20.xml").deploy();
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalStartEvent.bpmn20.xml").deploy();

    // Firing a signal start event should only start ONE process instance
    // This used to be two, due to subscriptions not being cleaned up
    runtimeService.signalEventReceived("The Signal");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);

    // Cleanup
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Deployment
  public void testSignalWaitOnUserTaskBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signal-wait");
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).signalEventSubscriptionName("waitsig").singleResult();
    assertThat(execution).isNotNull();
    runtimeService.signalEventReceived("waitsig", execution.getId());
    execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).signalEventSubscriptionName("waitsig").singleResult();
    assertThat(execution).isNull();
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo("Wait2");
  }

  /**
   * From https://forums.activiti.org/content/boundary-signal-causes-already-taking-transition
   */
  @Deployment
  public void testSignalThrowAndCatchInSameTransaction() {

    String fileExistsVar = "fileexists";

    // remove mock file
    FileExistsMock.getInstance().removeFile();

    // create first instance
    ProcessInstance firstProcessInstance = runtimeService.startProcessInstanceByKey("signalBoundaryProcess");
    assertThat(firstProcessInstance).isNotNull();

    // task should be "add a file"
    Task firstTask = taskService.createTaskQuery().singleResult();
    assertThat(firstTask.getName()).isEqualTo("Add a file");

    Map<String, Object> vars = runtimeService.getVariables(firstTask.getExecutionId());
    // file does not exists
    assertThat(vars.get(fileExistsVar)).isEqualTo(false);

    // create second instance
    ProcessInstance secondProcessInstance = runtimeService.startProcessInstanceByKey("signalBoundaryProcess");
    assertThat(secondProcessInstance).isNotNull();

    // there should be two open tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);

    // get current second task
    Task secondTask = taskService.createTaskQuery().processInstanceId(secondProcessInstance.getProcessInstanceId()).singleResult();
    // must be also in "add a file"
    assertThat(secondTask.getName()).isEqualTo("Add a file");

    // file does not exists yet
    vars = runtimeService.getVariables(secondTask.getExecutionId());
    assertThat(vars.get(fileExistsVar)).isEqualTo(false);

    // now, we "add a file"
    taskService.claim(firstTask.getId(), "user");
    // create the file
    FileExistsMock.getInstance().touchFile();
    // complete the task - this should cancel all tasks waiting in "Add a file"
    // using the "fileAddedSignal"
    // FIXME: this causes the exception:
    taskService.complete(firstTask.getId());

    List<Task> usingTask = taskService.createTaskQuery().taskName("Use the file").list();
    assertThat(usingTask).hasSize(1);
  }

  @Deployment
  public void testMultipleSignalStartEvents() {
    runtimeService.signalEventReceived("signal1");
    validateTaskCounts(1, 0, 0);

    runtimeService.signalEventReceived("signal2");
    validateTaskCounts(1, 1, 0);

    runtimeService.signalEventReceived("signal3");
    validateTaskCounts(1, 1, 1);

    runtimeService.signalEventReceived("signal1");
    validateTaskCounts(2, 1, 1);

    runtimeService.signalEventReceived("signal1");
    validateTaskCounts(3, 1, 1);

    runtimeService.signalEventReceived("signal3");
    validateTaskCounts(3, 1, 2);
  }

  private void validateTaskCounts(long taskACount, long taskBCount, long taskCCount) {
    assertThat(taskService.createTaskQuery().taskName("Task A").count()).isEqualTo(taskACount);
    assertThat(taskService.createTaskQuery().taskName("Task B").count()).isEqualTo(taskBCount);
    assertThat(taskService.createTaskQuery().taskName("Task C").count()).isEqualTo(taskCCount);
  }

}
