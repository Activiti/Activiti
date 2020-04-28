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

package org.activiti.engine.test.bpmn.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * A test specifically written to test how events (start/boundary) are handled
 * when deploying a new version of a process definition.
 *

 */
public class MessageEventsAndNewVersionDeploymentsWithTenantIdTest extends PluggableActivitiTestCase {

  private static final String TEST_PROCESS_GLOBAL_BOUNDARY_MESSAGE =
      "org/activiti/engine/test/bpmn/deployment/MessageEventsAndNewVersionDeploymentsTest.testGlobalMessageBoundaryEvent.bpmn20.xml";

  private static final String TEST_PROCESS_START_MESSAGE =
      "org/activiti/engine/test/bpmn/deployment/MessageEventsAndNewVersionDeploymentsTest.testStartMessageEvent.bpmn20.xml";

  private static final String TEST_PROCESS_NO_EVENTS =
      "org/activiti/engine/test/bpmn/deployment/MessageEventsAndNewVersionDeploymentsTest.processWithoutEvents.bpmn20.xml";

  private static final String TEST_PROCESS_BOTH_START_AND_BOUNDARY_MESSAGE =
      "org/activiti/engine/test/bpmn/deployment/MessageEventsAndNewVersionDeploymentsTest.testBothBoundaryAndStartMessage.bpmn20.xml";

  private static final String TEST_PROCESS_BOTH_START_AND_BOUNDARY_MESSAGE_SAME_MESSAGE =
      "org/activiti/engine/test/bpmn/deployment/MessageEventsAndNewVersionDeploymentsTest.testBothBoundaryAndStartMessageSameMessage.bpmn20.xml";

  private static final String TENANT_ID = "223344";

  /*
   * BOUNDARY MESSAGE EVENT
   */

  public void testMessageBoundaryEvent() {
    String deploymentId1 = deployBoundaryMessageTestProcess();
    runtimeService.startProcessInstanceByKeyAndTenantId("messageTest", TENANT_ID);
    assertThat(getAllEventSubscriptions()).hasSize(1);

    String deploymentId2 = deployBoundaryMessageTestProcess();
    runtimeService.startProcessInstanceByKeyAndTenantId("messageTest", TENANT_ID);
    assertThat(getAllEventSubscriptions()).hasSize(2);

    assertReceiveMessage("myMessage", 2);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);

    for (Task task : tasks) {
      assertThat(task.getName()).isEqualTo("Task after message");
    }

    cleanup(deploymentId1, deploymentId2);
  }

  /**
   * Verifying that the event subscriptions do get removed when removing a deployment.
   */
  public void testBoundaryEventSubscriptionDeletedOnDeploymentDelete() {
    String deploymentId = deployBoundaryMessageTestProcess();
    runtimeService.startProcessInstanceByKeyAndTenantId("messageTest", TENANT_ID);
    assertThat(taskService.createTaskQuery().singleResult().getName()).isEqualTo("My Task");

    String deploymentId2 = deployBoundaryMessageTestProcess();
    runtimeService.startProcessInstanceByKeyAndTenantId("messageTest", TENANT_ID);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(2);
    assertThat(getAllEventSubscriptions()).hasSize(2);

    repositoryService.deleteDeployment(deploymentId, true);
    assertThat(taskService.createTaskQuery().singleResult().getName()).isEqualTo("My Task");
    assertThat(getAllEventSubscriptions()).hasSize(1);

    repositoryService.deleteDeployment(deploymentId2, true);
    assertThat(getAllEventSubscriptions()).hasSize(0);
  }

  /**
   * Verifying that the event subscriptions do get removed when removing a process instance.
   */
  public void testBoundaryEventSubscrptionsDeletedOnProcessInstanceDelete() {
    String deploymentId1 = deployBoundaryMessageTestProcess();
    runtimeService.startProcessInstanceByKeyAndTenantId("messageTest", TENANT_ID);
    assertThat(taskService.createTaskQuery().singleResult().getName()).isEqualTo("My Task");

    String deploymentId2 = deployBoundaryMessageTestProcess();
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("messageTest", TENANT_ID);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(2);
    assertThat(getAllEventSubscriptions()).hasSize(2);

    // Deleting PI of second deployment
    runtimeService.deleteProcessInstance(processInstance2.getId(), "testing");
    assertThat(taskService.createTaskQuery().singleResult().getName()).isEqualTo("My Task");
    assertThat(getAllEventSubscriptions()).hasSize(1);

    runtimeService.messageEventReceived("myMessage", getExecutionIdsForMessageEventSubscription("myMessage").get(0));
    assertThat(getAllEventSubscriptions()).hasSize(0);
    assertThat(taskService.createTaskQuery().singleResult().getName()).isEqualTo("Task after message");

    cleanup(deploymentId1, deploymentId2);
  }


  /*
   * START MESSAGE EVENT
   */

  public void testStartMessageEvent() {
    String deploymentId1 = deployStartMessageTestProcess();
    assertThat(getAllEventSubscriptions()).hasSize(1);
    assertEventSubscriptionsCount(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    String deploymentId2 = deployStartMessageTestProcess();
    assertEventSubscriptionsCount(1);

    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);
    assertEventSubscriptionsCount(1);

    cleanup(deploymentId1, deploymentId2);
  }

  public void testMessageStartEventSubscriptionAfterDeploymentDelete() {

    // Deploy two version of process definition, delete latest and check if all is good

    String deploymentId1 = deployStartMessageTestProcess();
    List<EventSubscriptionEntity> eventSubscriptions = getAllEventSubscriptions();
    assertThat(eventSubscriptions).hasSize(1);

    String deploymentId2 = deployStartMessageTestProcess();
    eventSubscriptions = getAllEventSubscriptions();
    assertEventSubscriptionsCount(1);

    repositoryService.deleteDeployment(deploymentId2, true);
    eventSubscriptions = getAllEventSubscriptions();
    assertThat(eventSubscriptions).hasSize(1);

    cleanup(deploymentId1);
    assertThat(getAllEventSubscriptions()).hasSize(0);

    // Deploy two versions of process definition, delete the first
    deploymentId1 = deployStartMessageTestProcess();
    deploymentId2 = deployStartMessageTestProcess();
    assertThat(getAllEventSubscriptions()).hasSize(1);
    repositoryService.deleteDeployment(deploymentId1, true);
    eventSubscriptions = getAllEventSubscriptions();
    assertThat(eventSubscriptions).hasSize(1);
    assertThat(eventSubscriptions.get(0).getProcessDefinitionId()).isEqualTo(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId2).singleResult().getId());

    cleanup(deploymentId2);
    assertThat(getAllEventSubscriptions()).hasSize(0);
  }


  /**
   * v1 -> has start message event
   * v2 -> has no start message event
   * v3 -> has start message event
   */
  public void testDeployIntermediateVersionWithoutMessageStartEvent() {
    String deploymentId1 = deployStartMessageTestProcess();
    assertThat(getAllEventSubscriptions()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    assertEventSubscriptionsCount(1);

    String deploymentId2 = deployProcessWithoutEvents();
    assertThat(getAllEventSubscriptions()).hasSize(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    assertEventSubscriptionsCount(0);

    String deploymentId3 = deployStartMessageTestProcess();
    assertThat(getAllEventSubscriptions()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);
    assertEventSubscriptionsCount(1);

    List<EventSubscriptionEntity> eventSubscriptions = getAllEventSubscriptions();
    assertThat(eventSubscriptions.get(0).getProcessDefinitionId())
        .isEqualTo(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId3).singleResult().getId());

    cleanup(deploymentId1, deploymentId2, deploymentId3);
  }

  public void testDeleteDeploymentWithStartMessageEvents1() {
    String deploymentId1, deploymentId2, deploymentId3;
    deploymentId1 = deployStartMessageTestProcess();
    deploymentId2 = deployProcessWithoutEvents();
    deploymentId3 = deployStartMessageTestProcess();
    repositoryService.deleteDeployment(deploymentId3, true);
    assertEventSubscriptionsCount(0); // the latest is now the one without a message start
    cleanup(deploymentId1, deploymentId2);
  }

  public void testDeleteDeploymentWithStartMessageEvents2() {
    String deploymentId1 = deployStartMessageTestProcess();
    String deploymentId2 = deployProcessWithoutEvents();
    String deploymentId3 = deployStartMessageTestProcess();
    repositoryService.deleteDeployment(deploymentId2, true);
    assertEventSubscriptionsCount(1); // the latest is now the one with the message
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().singleResult().getProcessDefinitionId())
        .isEqualTo(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId3).singleResult().getId());
    cleanup(deploymentId1, deploymentId3);
  }

  public void testDeleteDeploymentWithStartMessageEvents3() {
    String deploymentId1 = deployStartMessageTestProcess();
    String deploymentId2 = deployProcessWithoutEvents();
    String deploymentId3 = deployStartMessageTestProcess();
    repositoryService.deleteDeployment(deploymentId1, true);
    assertEventSubscriptionsCount(1); // the latest is now the one with the message
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().singleResult().getProcessDefinitionId())
        .isEqualTo(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId3).singleResult().getId());
    cleanup(deploymentId2, deploymentId3);
  }

  public void testDeleteDeploymentWithStartMessageEvents4() {
    String deploymentId1 = deployStartMessageTestProcess();
    String deploymentId2 = deployProcessWithoutEvents();
    String deploymentId3 = deployStartMessageTestProcess();
    repositoryService.deleteDeployment(deploymentId2, true);
    repositoryService.deleteDeployment(deploymentId3, true);
    assertEventSubscriptionsCount(1); // the latest is now the one with the message start
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().singleResult().getProcessDefinitionId())
        .isEqualTo(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId1).singleResult().getId());
    cleanup(deploymentId1);
  }

  public void testDeleteDeploymentWithStartMessageEvents5() {
    String deploymentId1 = deployStartMessageTestProcess();
    String deploymentId2 = deployProcessWithoutEvents();
    assertEventSubscriptionsCount(0);
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
    repositoryService.deleteDeployment(deploymentId2, true);
    assertEventSubscriptionsCount(1); // the first is now the one with the signal
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().singleResult().getProcessDefinitionId())
        .isEqualTo(repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId1).singleResult().getId());
    cleanup(deploymentId1);
  }

  public void testDeleteDeploymentWithStartMessageEvents6() {
    String deploymentId1 = deployStartMessageTestProcess();
    String deploymentId2 = deployProcessWithoutEvents();
    String deploymentId3 = deployStartMessageTestProcess();
    String deploymentId4 = deployProcessWithoutEvents();
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    repositoryService.deleteDeployment(deploymentId2, true);
    repositoryService.deleteDeployment(deploymentId3, true);
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    repositoryService.deleteDeployment(deploymentId1, true);
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
    cleanup(deploymentId4);
  }

  public void testDeleteDeploymentWithStartMessageEvents7() {
    String deploymentId1 = deployStartMessageTestProcess();
    String deploymentId2 = deployProcessWithoutEvents();
    String deploymentId3 = deployStartMessageTestProcess();
    String deploymentId4 = deployProcessWithoutEvents();
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    repositoryService.deleteDeployment(deploymentId2, true);
    repositoryService.deleteDeployment(deploymentId3, true);
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    repositoryService.deleteDeployment(deploymentId4, true);
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    cleanup(deploymentId1);
  }


  /*
   * BOTH BOUNDARY AND START MESSAGE
   */

  public void testBothBoundaryAndStartEvent() {

    // Deploy process with both boundary and start event

    String deploymentId1 = deployProcessWithBothStartAndBoundaryMessage();
    assertEventSubscriptionsCount(1);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);
    assertThat(getAllEventSubscriptions()).hasSize(3); // 1 for the start, 2 for the boundary

    // Deploy version with only a boundary signal
    String deploymentId2 = deployBoundaryMessageTestProcess();
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);
    assertEventSubscriptionsCount(2); // 2 boundary events remain

    // Deploy version with signal start
    String deploymentId3 = deployStartMessageTestProcess();
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
    assertEventSubscriptionsCount(3);

    // Delete last version again, making the one with the boundary the latest
    repositoryService.deleteDeployment(deploymentId3, true);
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID));
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2); // -1, cause process instance of deploymentId3 is gone too
    assertEventSubscriptionsCount(2); // The 2 boundary remains

    // Test the boundary signal
    assertReceiveMessage("myBoundaryMessage", 2);
    assertThat(taskService.createTaskQuery().taskName("Task after boundary message").list()).hasSize(2);

    // Delete second version
    repositoryService.deleteDeployment(deploymentId2, true);
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3); // -1, cause process instance of deploymentId3 is gone too
    assertEventSubscriptionsCount(2); // 2 boundaries

    cleanup(deploymentId1);
  }

 public void testBothBoundaryAndStartSameMessageId() {

    // Deploy process with both boundary and start event

    String deploymentId1 = deployProcessWithBothStartAndBoundarySameMessage();
    assertThat(getAllEventSubscriptions()).hasSize(1);
    assertEventSubscriptionsCount(1);
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

    for (int i=0; i<9; i++) {
      // Every iteration will signal the boundary event of the previous iteration!
      runtimeService.startProcessInstanceByMessageAndTenantId("myMessage", TENANT_ID);
    }

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricProcessInstanceQuery().count()).isEqualTo(9);
    }
    assertThat(getAllEventSubscriptions()).hasSize(10); // 1 for the start, 9 for boundary

    // Deploy version with only a start signal. The boundary events should still react though!
    String deploymentId2 = deployStartMessageTestProcess();
    runtimeService.startProcessInstanceByMessageAndTenantId("myStartMessage", TENANT_ID);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(10);
    assertEventSubscriptionsCount(10); // Remains 10: 1 one was removed, but one added for the new message

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByMessageAndTenantId("myMessage", TENANT_ID));

    cleanup(deploymentId1, deploymentId2);
  }

  /*
   * HELPERS
   */

  private String deployBoundaryMessageTestProcess() {
    return deploy(TEST_PROCESS_GLOBAL_BOUNDARY_MESSAGE);
  }

  private String deployStartMessageTestProcess() {
    return deploy(TEST_PROCESS_START_MESSAGE);
  }

  private String deployProcessWithoutEvents() {
    return deploy(TEST_PROCESS_NO_EVENTS);
  }

  private String deployProcessWithBothStartAndBoundaryMessage() {
    return deploy(TEST_PROCESS_BOTH_START_AND_BOUNDARY_MESSAGE);
  }

  private String deployProcessWithBothStartAndBoundarySameMessage() {
    return deploy(TEST_PROCESS_BOTH_START_AND_BOUNDARY_MESSAGE_SAME_MESSAGE);
  }

  private String deploy(String path) {
    String deploymentId = repositoryService
      .createDeployment()
	  .tenantId(TENANT_ID)
      .addClasspathResource(path)
      .deploy()
      .getId();
    return deploymentId;
  }

  private void cleanup(String ... deploymentIds) {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }

  private List<String> getExecutionIdsForMessageEventSubscription(final String messageName) {
    return managementService.executeCommand(new Command<List<String>>() {
      public List<String> execute(CommandContext commandContext) {
        EventSubscriptionQueryImpl query = new EventSubscriptionQueryImpl(commandContext);
        query.eventType("message");
        query.eventName(messageName);
        query.tenantId(TENANT_ID);
        query.orderByCreated().desc();
        List<EventSubscriptionEntity> eventSubscriptions = query.list();

        List<String> executionIds = new ArrayList<String>();
        for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
          executionIds.add(eventSubscription.getExecutionId());
        }
        return executionIds;
      }
    });
  }

  private List<EventSubscriptionEntity> getAllEventSubscriptions() {
    return managementService.executeCommand(new Command<List<EventSubscriptionEntity>>() {
      public List<EventSubscriptionEntity> execute(CommandContext commandContext) {
        EventSubscriptionQueryImpl query = new EventSubscriptionQueryImpl(commandContext);
        query.tenantId(TENANT_ID);
        query.orderByCreated().desc();

        List<EventSubscriptionEntity> eventSubscriptionEntities = query.list();
        for (EventSubscriptionEntity entity : eventSubscriptionEntities) {
          assertThat(entity.getEventType()).isEqualTo("message");
          assertThat(entity.getProcessDefinitionId()).isNotNull();
        }
        return eventSubscriptionEntities;
      }
    });
  }

  private void assertReceiveMessage(String messageName, int executionIdsCount) {
    List<String> executionIds =getExecutionIdsForMessageEventSubscription(messageName);
    assertThat(executionIds).hasSize(executionIdsCount);
    for (String executionId : executionIds) {
      runtimeService.messageEventReceived(messageName, executionId);
    }
  }

  private void assertEventSubscriptionsCount(int count) {
  	assertThat(getAllEventSubscriptions()).hasSize(count);
  }

}
