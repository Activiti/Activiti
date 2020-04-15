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

package org.activiti.engine.test.bpmn.event.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 */
public class MessageIntermediateEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSingleIntermediateMessageEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertThat(activeActivityIds).isNotNull();
    assertThat(activeActivityIds).hasSize(1);
    assertThat(activeActivityIds.contains("messageCatch")).isTrue();

    String messageName = "newInvoiceMessage";
    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName(messageName).singleResult();

    assertThat(execution).isNotNull();

    runtimeService.messageEventReceived(messageName, execution.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

  }

  @Deployment
  public void testSingleIntermediateMessageExpressionEvent() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("myMessageName", "testMessage");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process", variableMap);

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertThat(activeActivityIds).isNotNull();
    assertThat(activeActivityIds).hasSize(1);
    assertThat(activeActivityIds.contains("messageCatch")).isTrue();

    String messageName = "testMessage";
    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName(messageName).singleResult();
    assertThat(execution).isNotNull();

    runtimeService.messageEventReceived(messageName, execution.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageIntermediateEventTest.testSingleIntermediateMessageExpressionEvent.bpmn20.xml")
  public void testSingleIntermediateMessageExpressionEventWithNullExpressionShouldFail() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("myMessageName", null);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("process", variableMap))
      .withMessage("Expression '${myMessageName}' is null");
  }

  @Deployment
  public void testConcurrentIntermediateMessageEvent() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    List<String> activeActivityIds = runtimeService.getActiveActivityIds(pi.getId());
    assertThat(activeActivityIds).isNotNull();
    assertThat(activeActivityIds).hasSize(2);
    assertThat(activeActivityIds.contains("messageCatch1")).isTrue();
    assertThat(activeActivityIds.contains("messageCatch2")).isTrue();

    String messageName = "newInvoiceMessage";
    List<Execution> executions = runtimeService.createExecutionQuery().messageEventSubscriptionName(messageName).list();

    assertThat(executions).isNotNull();
    assertThat(executions).hasSize(2);

    runtimeService.messageEventReceived(messageName, executions.get(0).getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNull();

    runtimeService.messageEventReceived(messageName, executions.get(1).getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());
  }

  @Deployment
  public void testAsyncTriggeredMessageEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    assertThat(processInstance).isNotNull();
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).messageEventSubscriptionName("newMessage").singleResult();
    assertThat(execution).isNotNull();
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(2);

    runtimeService.messageEventReceivedAsync("newMessage", execution.getId());

    assertThat(managementService.createJobQuery().messages().count()).isEqualTo(1);

    waitForJobExecutorToProcessAllJobs(8000L, 200L);
    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }
}
