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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class MessageStartEventTest extends PluggableActivitiTestCase {

  public void testDeploymentCreatesSubscriptions() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy().getId();

    List<EventSubscriptionEntity> eventSubscriptions = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor()).list();

    assertThat(eventSubscriptions).hasSize(1);

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testSameMessageNameFails() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy().getId();
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/message/otherProcessWithNewInvoiceMessage.bpmn20.xml")
        .deploy())
      .withMessageContaining("there already is a message event subscription for the message with name");

    // clean db:
    repositoryService.deleteDeployment(deploymentId);
  }

  public void testSameMessageNameInSameProcessFails() {
    assertThatExceptionOfType(ActivitiException.class)
      .as("exception expected: Cannot have more than one message event subscription with name 'newInvoiceMessage' for scope")
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/message/testSameMessageNameInSameProcessFails.bpmn20.xml")
        .deploy());
  }

  public void testUpdateProcessVersionCancelsSubscriptions() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy().getId();

    List<EventSubscriptionEntity> eventSubscriptions = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor()).list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertThat(eventSubscriptions).hasSize(1);
    assertThat(processDefinitions).hasSize(1);

    String newDeploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
        .deploy().getId();

    List<EventSubscriptionEntity> newEventSubscriptions = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor()).list();
    List<ProcessDefinition> newProcessDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertThat(newEventSubscriptions).hasSize(1);
    assertThat(newProcessDefinitions).hasSize(2);
    for (ProcessDefinition processDefinition : newProcessDefinitions) {
      if (processDefinition.getVersion() == 1) {
        for (EventSubscriptionEntity subscription : newEventSubscriptions) {
          assertThat(subscription.getConfiguration().equals(processDefinition.getId())).isFalse();
        }
      } else {
        for (EventSubscriptionEntity subscription : newEventSubscriptions) {
          assertThat(subscription.getConfiguration().equals(processDefinition.getId())).isTrue();
        }
      }
    }
    assertThat(eventSubscriptions.equals(newEventSubscriptions)).isFalse();

    repositoryService.deleteDeployment(deploymentId);
    repositoryService.deleteDeployment(newDeploymentId);
  }

  @Deployment
  public void testSingleMessageStartEvent() {

    // using startProcessInstanceByMessage triggers the message start event

    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    assertThat(processInstance.isEnded()).isFalse();

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    // using startProcessInstanceByKey also triggers the message event, if
    // there is a single start event

    processInstance = runtimeService.startProcessInstanceByKey("singleMessageStartEvent");

    assertThat(processInstance.isEnded()).isFalse();

    task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testMessageStartEventAndNoneStartEvent() {

    // using startProcessInstanceByKey triggers the none start event

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    assertThat(processInstance.isEnded()).isFalse();

    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterNoneStart").singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    // using startProcessInstanceByMessage triggers the message start event

    processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    assertThat(processInstance.isEnded()).isFalse();

    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

  }

  @Deployment
  public void testMultipleMessageStartEvents() {

    // sending newInvoiceMessage

    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    assertThat(processInstance.isEnded()).isFalse();

    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    // sending newInvoiceMessage2

    processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage2");

    assertThat(processInstance.isEnded()).isFalse();

    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart2").singleResult();
    assertThat(task).isNotNull();

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    // starting the process using startProcessInstanceByKey is possible, the
    // first message start event will be the default:
    processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertThat(processInstance.isEnded()).isFalse();
    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
  public void testMessageStartEventDispatchActivitiMessageReceivedBeforeProcessStarted() {

    // given
    List<ActivitiEvent> events = new ArrayList<>();

    runtimeService.addEventListener(new ActivitiEventListener() {
        @Override
        public void onEvent(ActivitiEvent event) {
            events.add(event);
        }
        @Override
        public boolean isFailOnException() {
            return false;
        }
    });

    // when
    ProcessInstance process = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");

    String executionId = runtimeService.createExecutionQuery()
                                       .processInstanceId(process.getId())
                                       .onlyChildExecutions()
                                       .singleResult()
                                       .getId();

    // then ACTIVITY_MESSAGE_RECEIVED should be fired before PROCESS_STARTED
    assertThat(events)
                .filteredOn(event -> event.getType() == ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED
                                        || event.getType() == ActivitiEventType.PROCESS_STARTED)
                .extracting("type",
                            "processDefinitionId",
                            "processInstanceId",
                            "executionId")
                .containsExactly(tuple(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED,
                                       process.getProcessDefinitionId(),
                                       process.getId(),
                                       executionId),
                                 tuple(ActivitiEventType.PROCESS_STARTED,
                                       process.getProcessDefinitionId(),
                                       process.getId(),
                                       executionId));
  }
}
