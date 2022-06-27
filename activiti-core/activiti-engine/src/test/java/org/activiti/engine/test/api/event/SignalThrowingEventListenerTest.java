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

package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.bpmn.helper.SignalThrowingEventListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEventListener}s that throws a signal BPMN event when an {@link ActivitiEvent} has been dispatched.
 *
 */
public class SignalThrowingEventListenerTest extends PluggableActivitiTestCase {

  @Deployment
  public void testThrowSignal() throws Exception {
    SignalThrowingEventListener listener = null;
    try {
      listener = new SignalThrowingEventListener();
      listener.setSignalName("Signal");
      listener.setProcessInstanceScope(true);

      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
      assertThat(processInstance).isNotNull();

      // Fetch the task and re-assign it to trigger the event-listener
      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(task).isNotNull();
      taskService.setAssignee(task.getId(), "kermit");

      // Boundary-event should have been signaled and a new task should be
      // available, on top of the already
      // existing one, since the cancelActivity='false'
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("subTask").singleResult();
      assertThat(task).isNotNull();
      assertThat(task.getAssignee()).isEqualTo("kermit");

      Task boundaryTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("boundaryTask").singleResult();
      assertThat(boundaryTask).isNotNull();

    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  @Deployment
  public void testThrowSignalDefinedInProcessDefinition() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
    assertThat(processInstance).isNotNull();

    // Fetch the task and re-assign it to trigger the event-listener
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.setAssignee(task.getId(), "kermit");

    // Boundary-event should have been signaled and a new task should be
    // available, on top of the already
    // existing one, since the cancelActivity='false'
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("subTask").singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getAssignee()).isEqualTo("kermit");

    Task boundaryTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("boundaryTask").singleResult();
    assertThat(boundaryTask).isNotNull();
  }

  @Deployment
  public void testThrowSignalInterrupting() throws Exception {
    SignalThrowingEventListener listener = null;
    try {
      listener = new SignalThrowingEventListener();
      listener.setSignalName("Signal");
      listener.setProcessInstanceScope(true);
      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
      assertThat(processInstance).isNotNull();

      // Fetch the task and re-assign it to trigger the event-listener
      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(task).isNotNull();
      taskService.setAssignee(task.getId(), "kermit");

      // Boundary-event should have been signalled and a new task should
      // be available, the already
      // existing one is gone, since the cancelActivity='true'
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("subTask").singleResult();
      assertThat(task).isNull();

      Task boundaryTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("boundaryTask").singleResult();
      assertThat(boundaryTask).isNotNull();

    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  /**
   * Test signal throwing when a job failed and the retries are decremented, affectively starting a new transaction.
   */
  @Deployment
  public void testThrowSignalInNewTransaction() throws Exception {
    SignalThrowingEventListener listener = null;
    try {
      listener = new SignalThrowingEventListener();
      listener.setSignalName("Signal");
      listener.setProcessInstanceScope(true);
      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.JOB_RETRIES_DECREMENTED);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
      assertThat(processInstance).isNotNull();

      Job signalJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

      assertThatExceptionOfType(ActivitiException.class)
        .isThrownBy(() -> managementService.executeJob(signalJob.getId()));

      Job failedJob = managementService.createTimerJobQuery().withException().processInstanceId(processInstance.getId()).singleResult();

      assertThat(failedJob).isNotNull();
      assertThat(failedJob.getRetries()).isEqualTo(2);

      // One retry should have triggered dispatching of a retry-decrement event
      assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

      assertThatExceptionOfType(ActivitiException.class)
        .isThrownBy(() -> {
          managementService.moveTimerToExecutableJob(failedJob.getId());
          managementService.executeJob(failedJob.getId());
        });

      assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(2);
    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  /**
   * Test signal throwing when a job failed, signaling will happen in the rolled back transaction, not doing anything in the end...
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.testThrowSignalInNewTransaction.bpmn20.xml" })
  public void testThrowSignalInRolledbackTransaction() throws Exception {
    SignalThrowingEventListener listener = null;

    try {
      listener = new SignalThrowingEventListener();
      listener.setSignalName("Signal");
      listener.setProcessInstanceScope(true);
      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.JOB_EXECUTION_FAILURE);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
      assertThat(processInstance).isNotNull();

      Job signalJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

      assertThatExceptionOfType(ActivitiException.class)
        .isThrownBy(() -> managementService.executeJob(signalJob.getId()));

      Job failedJob = managementService.createTimerJobQuery().withException().processInstanceId(processInstance.getId()).singleResult();

      assertThat(failedJob).as("Expected job with exception, found no such job").isNotNull();
      assertThat(failedJob.getRetries()).isEqualTo(2);

      // Three retries should each have triggered dispatching of a retry-decrement event
      assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

      assertThatExceptionOfType(ActivitiException.class)
        .isThrownBy(() -> {
          managementService.moveTimerToExecutableJob(failedJob.getId());
          managementService.executeJob(failedJob.getId());
        });

      assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  /**
   * Test if an engine-wide signal is thrown as response to a dispatched event.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignal.bpmn20.xml",
      "org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignalExternalProcess.bpmn20.xml" })
  public void testGlobalSignal() throws Exception {
    SignalThrowingEventListener listener = null;

    try {
      listener = new SignalThrowingEventListener();
      listener.setSignalName("Signal");
      listener.setProcessInstanceScope(false);
      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("globalSignalProcess");
      assertThat(processInstance).isNotNull();

      ProcessInstance externalProcess = runtimeService.startProcessInstanceByKey("globalSignalProcessExternal");
      assertThat(processInstance).isNotNull();
      // Make sure process is not ended yet by querying it again
      externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId()).singleResult();
      assertThat(externalProcess).isNotNull();

      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(task).isNotNull();

      // Assign task to trigger signal
      taskService.setAssignee(task.getId(), "kermit");

      // Second process should have been signaled
      externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId()).singleResult();
      assertThat(externalProcess).isNull();

      // Task assignee should still be set
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(task).isNotNull();
      assertThat(task.getAssignee()).isEqualTo("kermit");

    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }

  }

  /**
   * Test if an engine-wide signal is thrown as response to a dispatched event.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignalDefinedInProcessDefinition.bpmn20.xml",
      "org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignalExternalProcess.bpmn20.xml" })
  public void testGlobalSignalDefinedInProcessDefinition() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("globalSignalProcess");
    assertThat(processInstance).isNotNull();

    ProcessInstance externalProcess = runtimeService.startProcessInstanceByKey("globalSignalProcessExternal");
    assertThat(processInstance).isNotNull();
    // Make sure process is not ended yet by querying it again
    externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId()).singleResult();
    assertThat(externalProcess).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Assign task to trigger signal
    taskService.setAssignee(task.getId(), "kermit");

    // Second process should have been signaled
    externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId()).singleResult();
    assertThat(externalProcess).isNull();

    // Task assignee should still be set
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getAssignee()).isEqualTo("kermit");
  }
}
