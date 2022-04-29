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


package org.activiti.engine.test.bpmn.event.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class MessageEventSubprocessTest extends PluggableActivitiTestCase {

  @Deployment
  public void testInterruptingUnderProcessDefinition() {
    testInterruptingUnderProcessDefinition(1, 3);
  }

  /**
   * Checks if unused event subscriptions are properly deleted.
   */
  @Deployment
  public void testTwoInterruptingUnderProcessDefinition() {
    testInterruptingUnderProcessDefinition(2, 4);
  }

  private void testInterruptingUnderProcessDefinition(int expectedNumberOfEventSubscriptions, int numberOfExecutions) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("newMessage").singleResult();
    assertThat(execution).isNotNull();
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(expectedNumberOfEventSubscriptions);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(numberOfExecutions);

    // if we trigger the usertask, the process terminates and the event subscription is removed:
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("task");
    taskService.complete(task.getId());
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
    assertProcessEnded(processInstance.getId());

    // now we start a new instance but this time we trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("newMessage").singleResult();
    assertThat(execution).isNotNull();
    runtimeService.messageEventReceived("newMessage", execution.getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("eventSubProcessTask");
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testNonInterruptingUnderProcessDefinition() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    // the process instance must have a message event subscription:
    Execution execution = runtimeService.createExecutionQuery()
                                        .processInstanceId(processInstance.getId())
                                        .messageEventSubscriptionName("newMessage")
                                        .singleResult();
    assertThat(execution).isNotNull();
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(3);

    // if we trigger the usertask, the process terminates and the event
    // subscription is removed:
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("task");
    taskService.complete(task.getId());
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    // ###################### now we start a new instance but this time we
    // trigger the event subprocess:
    processInstance = runtimeService.startProcessInstanceByKey("process");

    execution = runtimeService.createExecutionQuery()
                              .processInstanceId(processInstance.getId())
                              .messageEventSubscriptionName("newMessage")
                              .singleResult();

    runtimeService.messageEventReceived("newMessage", execution.getId());

    assertThat(taskService.createTaskQuery().count()).isEqualTo(2);

    // now let's first complete the task in the main flow:
    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // we still have 3 executions:
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(3);

    // now let's complete the task in the event subprocess
    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // done!
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    // #################### again, the other way around:

    processInstance = runtimeService.startProcessInstanceByKey("process");
    execution = runtimeService.createExecutionQuery()
                              .processInstanceId(processInstance.getId())
                              .messageEventSubscriptionName("newMessage")
                              .singleResult();

    runtimeService.messageEventReceived("newMessage", execution.getId());

    assertThat(taskService.createTaskQuery().count()).isEqualTo(2);

    task = taskService.createTaskQuery().taskDefinitionKey("eventSubProcessTask").singleResult();
    taskService.complete(task.getId());
    // we still have task executions:
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(2);

    task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());
    // done!
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }

}
