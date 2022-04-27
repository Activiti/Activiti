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


package org.activiti.engine.test.bpmn.subprocess.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class TransactionSubProcessTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml" })
  public void testSimpleCaseTxSuccessful() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count()).isEqualTo(1);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(1);

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();

    // making the tx succeed:
    taskService.setVariable(task.getId(), "confirmed", true);
    taskService.complete(task.getId());

    // now the process instance execution is sitting in the 'afterSuccess' task
    // -> has left the transaction using the "normal" sequence flow
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertThat(activeActivityIds.contains("afterSuccess")).isTrue();

    // there is a compensate event subscription for the transaction under the process instance
    EventSubscriptionEntity eventSubscriptionEntity = createEventSubscriptionQuery().eventType("compensate").activityId("tx").executionId(processInstance.getId()).singleResult();

    // there is an event-scope execution associated with the event-subscription:
    assertThat(eventSubscriptionEntity.getConfiguration()).isNotNull();
    Execution eventScopeExecution = runtimeService.createExecutionQuery().executionId(eventSubscriptionEntity.getConfiguration()).singleResult();
    assertThat(eventScopeExecution).isNotNull();

    // we still have compensate event subscriptions for the compensation handlers, only now they are part of the event scope
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").executionId(eventScopeExecution.getId()).count()).isEqualTo(1);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").executionId(eventScopeExecution.getId()).count()).isEqualTo(1);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoChargeCard").executionId(eventScopeExecution.getId()).count()).isEqualTo(1);

    // assert that the compensation handlers have not been invoked:
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isNull();
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookFlight")).isNull();
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoChargeCard")).isNull();

    // end the process instance
    Execution receiveExecution = runtimeService.createExecutionQuery().activityId("afterSuccess").singleResult();
    runtimeService.trigger(receiveExecution.getId());
    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml" })
  public void testSimpleCaseTxCancelled() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count()).isEqualTo(1);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(1);

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("askCustomer");

    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    // now the process instance execution is sitting in the 'afterCancellation' task
    // -> has left the transaction using the cancel boundary event
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("afterCancellation").singleResult();
    assertThat(execution).isNotNull();

    // we have no more compensate event subscriptions
    assertThat(createEventSubscriptionQuery().eventType("compensate").count()).isEqualTo(0);

    // assert that the compensation handlers have been invoked:
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(1);
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookFlight")).isEqualTo(1);
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoChargeCard")).isEqualTo(1);

    // if we have history, we check that the invocation of the compensation
    // handlers is recorded in history.
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoBookFlight").count()).isEqualTo(1);
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count()).isEqualTo(1);
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoChargeCard").count()).isEqualTo(1);
    }

    // end the process instance
    Execution receiveExecution = runtimeService.createExecutionQuery().activityId("afterCancellation").singleResult();
    runtimeService.trigger(receiveExecution.getId());
    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testCancelEndConcurrent() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count()).isEqualTo(1);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(1);

    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("askCustomer");

    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    // now the process instance execution is sitting in the 'afterCancellation' task
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertThat(activeActivityIds.contains("afterCancellation")).isTrue();

    // we have no more compensate event subscriptions
    assertThat(createEventSubscriptionQuery().eventType("compensate").count()).isEqualTo(0);

    // assert that the compensation handlers have been invoked:
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(1);
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookFlight")).isEqualTo(1);

    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count()).isEqualTo(1);
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("undoBookFlight").count()).isEqualTo(1);
    }

    // end the process instance
    Execution receiveExecution = runtimeService.createExecutionQuery().activityId("afterCancellation").singleResult();
    runtimeService.trigger(receiveExecution.getId());
    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testNestedCancelInner() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(0);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count()).isEqualTo(5);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count()).isEqualTo(1);

    // the tasks are present:
    Task taskInner = taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();
    Task taskOuter = taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();
    assertThat(taskInner).isNotNull();
    assertThat(taskOuter).isNotNull();

    // making the tx fail:
    taskService.setVariable(taskInner.getId(), "confirmed", false);
    taskService.complete(taskInner.getId());

    // now the process instance execution is sitting in the
    // 'afterInnerCancellation' task
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertThat(activeActivityIds.contains("afterInnerCancellation")).isTrue();

    // we have no more compensate event subscriptions for the inner tx
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count()).isEqualTo(0);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count()).isEqualTo(0);

    // we do not have a subscription or the outer tx yet
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(0);

    // assert that the compensation handlers have been invoked:
    assertThat(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookHotel")).isEqualTo(5);
    assertThat(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookFlight")).isEqualTo(1);

    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("innerTxundoBookHotel").count()).isEqualTo(5);
      assertThat(historyService.createHistoricActivityInstanceQuery().activityId("innerTxundoBookFlight").count()).isEqualTo(1);
    }

    // complete the task in the outer tx
    taskService.complete(taskOuter.getId());

    // end the process instance (signal the execution still sitting in afterInnerCancellation)
    runtimeService.trigger(runtimeService.createExecutionQuery().activityId("afterInnerCancellation").singleResult().getId());

    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testNestedCancelOuter() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // after the process is started, we have compensate event subscriptions:
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(0);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count()).isEqualTo(5);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count()).isEqualTo(1);

    // the tasks are present:
    Task taskInner = taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();
    Task taskOuter = taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();
    assertThat(taskInner).isNotNull();
    assertThat(taskOuter).isNotNull();

    // making the outer tx fail (invokes cancel end event)
    taskService.complete(taskOuter.getId());

    // now the process instance is sitting in 'afterOuterCancellation'
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertThat(activeActivityIds.contains("afterOuterCancellation")).isTrue();

    // we have no more compensate event subscriptions
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count()).isEqualTo(0);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count()).isEqualTo(0);
    assertThat(createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count()).isEqualTo(0);

    // the compensation handlers of the inner tx have not been invoked
    assertThat(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookHotel")).isNull();
    assertThat(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookFlight")).isNull();

    // the compensation handler in the outer tx has been invoked
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookFlight")).isEqualTo(1);

    // end the process instance (signal the execution still sitting in afterOuterCancellation)
    runtimeService.trigger(runtimeService.createExecutionQuery().activityId("afterOuterCancellation").singleResult().getId());

    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

  }

  /*
   * The cancel end event cancels all instances, compensation is performed for all instances
   *
   * see spec page 470: "If the cancelActivity attribute is set, the Activity the Event is attached to is then cancelled (in case of a multi-instance, all its instances are cancelled);"
   */
  @Deployment
  public void testMultiInstanceTx() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // there are now 5 instances of the transaction:

    List<EventSubscriptionEntity> eventSubscriptionEntities = createEventSubscriptionQuery().eventType("compensate").list();

    // there are 10 compensation event subscriptions
    assertThat(eventSubscriptionEntities).hasSize(10);

    Task task = taskService.createTaskQuery().listPage(0, 1).get(0);

    // canceling one instance triggers compensation for all other instances:
    taskService.setVariable(task.getId(), "confirmed", false);
    taskService.complete(task.getId());

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);

    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookHotel")).isEqualTo(5);
    assertThat(runtimeService.getVariable(processInstance.getId(), "undoBookFlight")).isEqualTo(5);

    runtimeService.trigger(runtimeService.createExecutionQuery().activityId("afterCancellation").singleResult().getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultiInstanceTx.bpmn20.xml" })
  public void testMultiInstanceTxSuccessful() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

    // there are now 5 instances of the transaction:

    List<EventSubscriptionEntity> EventSubscriptionEntitys = createEventSubscriptionQuery().eventType("compensate").list();

    // there are 10 compensation event subscriptions
    assertThat(EventSubscriptionEntitys).hasSize(10);

    // first complete the inner user-tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.setVariable(task.getId(), "confirmed", true);
      taskService.complete(task.getId());
    }

    // now complete the inner receive tasks
    List<Execution> executions = runtimeService.createExecutionQuery().activityId("receive").list();
    for (Execution execution : executions) {
      runtimeService.trigger(execution.getId());
    }

    runtimeService.trigger(runtimeService.createExecutionQuery().activityId("afterSuccess").singleResult().getId());

    assertThat(createEventSubscriptionQuery().count()).isEqualTo(0);
    assertProcessEnded(processInstance.getId());

  }

  public void testMultipleCancelBoundaryFails() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultipleCancelBoundaryFails.bpmn20.xml").deploy())
      .withMessageContaining("multiple boundary events with cancelEventDefinition not supported on same transaction");
  }

  public void testCancelBoundaryNoTransactionFails() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCancelBoundaryNoTransactionFails.bpmn20.xml")
        .deploy())
      .withMessageContaining("boundary event with cancelEventDefinition only supported on transaction subprocesses");
  }

  public void testCancelEndNoTransactionFails() {
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCancelEndNoTransactionFails.bpmn20.xml")
        .deploy())
      .withMessageContaining("end event with cancelEventDefinition only supported inside transaction subprocess");
  }

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }

  @Deployment
  public void testParseWithDI() {

    // this test simply makes sure we can parse a transaction subprocess with DI information
    // the actual transaction behavior is tested by other test cases

    // failing case

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TransactionSubProcessTest");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariable(task.getId(), "confirmed", false);

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    // success case

    processInstance = runtimeService.startProcessInstanceByKey("TransactionSubProcessTest");

    task = taskService.createTaskQuery().singleResult();
    taskService.setVariable(task.getId(), "confirmed", true);

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }
}
