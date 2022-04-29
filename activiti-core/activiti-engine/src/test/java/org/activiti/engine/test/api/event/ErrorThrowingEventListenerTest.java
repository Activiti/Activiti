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

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.bpmn.helper.ErrorThrowingEventListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEventListener}s that throws an error BPMN event when an {@link ActivitiEvent} has been dispatched.
 *
 */
public class ErrorThrowingEventListenerTest extends PluggableActivitiTestCase {

  @Deployment
  public void testThrowError() throws Exception {
    ErrorThrowingEventListener listener = null;
    try {
      listener = new ErrorThrowingEventListener();

      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
      assertThat(processInstance).isNotNull();

      // Fetch the task and assign it. Should cause error-event to be
      // dispatched
      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("userTask").singleResult();
      assertThat(task).isNotNull();
      taskService.setAssignee(task.getId(), "kermit");

      // Error-handling should have been called, and "escalate" task
      // should be available instead of original one
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("escalatedTask").singleResult();
      assertThat(task).isNotNull();

    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  @Deployment
  public void testThrowErrorDefinedInProcessDefinition() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
    assertThat(processInstance).isNotNull();

    // Fetch the task and assign it. Should cause error-event to be
    // dispatched
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("userTask").singleResult();
    assertThat(task).isNotNull();
    taskService.setAssignee(task.getId(), "kermit");

    // Error-handling should have been called, and "escalate" task should be
    // available instead of original one
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("escalatedTask").singleResult();
    assertThat(task).isNotNull();
  }

  @Deployment
  public void testThrowErrorWithErrorcode() throws Exception {
    ErrorThrowingEventListener listener = null;
    try {
      listener = new ErrorThrowingEventListener();
      listener.setErrorCode("123");

      processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
      assertThat(processInstance).isNotNull();

      // Fetch the task and assign it. Should cause error-event to be
      // dispatched
      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("userTask").singleResult();
      assertThat(task).isNotNull();
      taskService.setAssignee(task.getId(), "kermit");

      // Error-handling should have been called, and "escalate" task
      // should be available instead of original one
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("escalatedTask").singleResult();
      assertThat(task).isNotNull();

      // Try with a different error-code, resulting in a different task
      // being created
      listener.setErrorCode("456");

      processInstance = runtimeService.startProcessInstanceByKey("testError");
      assertThat(processInstance).isNotNull();

      // Fetch the task and assign it. Should cause error-event to be
      // dispatched
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("userTask").singleResult();
      assertThat(task).isNotNull();
      taskService.setAssignee(task.getId(), "kermit");

      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("escalatedTask2").singleResult();
      assertThat(task).isNotNull();
    } finally {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }

  @Deployment
  public void testThrowErrorWithErrorcodeDefinedInProcessDefinition() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
    assertThat(processInstance).isNotNull();

    // Fetch the task and assign it. Should cause error-event to be
    // dispatched
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("userTask").singleResult();
    assertThat(task).isNotNull();
    taskService.setAssignee(task.getId(), "kermit");

    // Error-handling should have been called, and "escalate" task should be
    // available instead of original one
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDefinitionKey("escalatedTask").singleResult();
    assertThat(task).isNotNull();
  }
}
