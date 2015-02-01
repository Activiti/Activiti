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

package org.activiti.engine.test.bpmn.subprocess.transaction;

import java.util.List;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class TransactionSubProcessTest extends PluggableActivitiTestCase {

  
  @Deployment(resources={"org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml"})
  public void testSimpleCaseTxSuccessful() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // after the process is started, we have compensate event subscriptions:
    assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    
    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    // making the tx succeed:
    taskService.setVariable(task.getId(), "confirmed", true);    
    taskService.complete(task.getId());
    
    // now the process instance execution is sitting in the 'afterSuccess' task 
    // -> has left the transaction using the "normal" sequence flow
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterSuccess"));
    
    // there is a compensate event subscription for the transaction under the process instance
    EventSubscriptionEntity eventSubscriptionEntity = createEventSubscriptionQuery().eventType("compensate").activityId("tx").executionId(processInstance.getId()).singleResult();
    
    // there is an event-scope execution associated with the event-subscription:
    assertNotNull(eventSubscriptionEntity.getConfiguration());
    Execution eventScopeExecution = runtimeService.createExecutionQuery().executionId(eventSubscriptionEntity.getConfiguration()).singleResult();
    assertNotNull(eventScopeExecution);
    
    // we still have compensate event subscriptions for the compensation handlers, only now they are part of the event scope
    assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").executionId(eventScopeExecution.getId()).count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").executionId(eventScopeExecution.getId()).count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("undoChargeCard").executionId(eventScopeExecution.getId()).count());
    
    // assert that the compensation handlers have not been invoked:
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "undoChargeCard"));
           
    // end the process instance
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());    
    assertEquals(0, runtimeService.createExecutionQuery().count());
    
  }
  
  @Deployment(resources={"org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml"})
  public void testSimpleCaseTxCancelled() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // after the process is started, we have compensate event subscriptions:
    assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    
    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);    
    taskService.complete(task.getId());
    
    // now the process instance execution is sitting in the 'afterCancellation' task 
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterCancellation"));
    
    // we have no more compensate event subscriptions
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").count());
    
    // assert that the compensation handlers have been invoked:
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoChargeCard"));
    
    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookFlight")
              .count());
      
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());
      
      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoChargeCard")
              .count());
    }
   
    // end the process instance
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());    
  }
  

  @Deployment
  public void testCancelEndConcurrent() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // after the process is started, we have compensate event subscriptions:
    assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    
    // the task is present:
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    // making the tx fail:
    taskService.setVariable(task.getId(), "confirmed", false);    
    taskService.complete(task.getId());
    
    // now the process instance execution is sitting in the 'afterCancellation' task 
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterCancellation"));
    
    // we have no more compensate event subscriptions
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").count());
    
    // assert that the compensation handlers have been invoked:
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    
    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookFlight")
              .count());
      
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());
    }
   
    // end the process instance
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());    
  }
  
  @Deployment
  public void testNestedCancelInner() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // after the process is started, we have compensate event subscriptions:
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());
    
    // the tasks are present:
    Task taskInner = taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();
    Task taskOuter = taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();
    assertNotNull(taskInner);
    assertNotNull(taskOuter);
    
    // making the tx fail:
    taskService.setVariable(taskInner.getId(), "confirmed", false);    
    taskService.complete(taskInner.getId());
        
    // now the process instance execution is sitting in the 'afterInnerCancellation' task 
    // -> has left the transaction using the cancel boundary event
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterInnerCancellation"));
    
    // we have no more compensate event subscriptions for the inner tx
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());
   
    // we do not have a subscription or the outer tx yet
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // assert that the compensation handlers have been invoked:
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "innerTxundoBookHotel"));
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "innerTxundoBookFlight"));
    
    // if we have history, we check that the invocation of the compensation handlers is recorded in history.
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("innerTxundoBookHotel")
              .count());
      
      assertEquals(1, historyService.createHistoricActivityInstanceQuery()
              .activityId("innerTxundoBookFlight")
              .count());
    }

    // complete the task in the outer tx
    taskService.complete(taskOuter.getId());
    
    // end the process instance (signal the execution still sitting in afterInnerCancellation)
    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterInnerCancellation").singleResult().getId());   
    
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());    
  }
  
  @Deployment
  public void testNestedCancelOuter() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // after the process is started, we have compensate event subscriptions:
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());
    assertEquals(5,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(1,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());
    
    // the tasks are present:
    Task taskInner = taskService.createTaskQuery().taskDefinitionKey("innerTxaskCustomer").singleResult();
    Task taskOuter = taskService.createTaskQuery().taskDefinitionKey("bookFlight").singleResult();
    assertNotNull(taskInner);
    assertNotNull(taskOuter);
    
    // making the outer tx fail (invokes cancel end event)
    taskService.complete(taskOuter.getId());
        
    // now the process instance is sitting in 'afterOuterCancellation'
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
    assertTrue(activeActivityIds.contains("afterOuterCancellation"));
    
    // we have no more compensate event subscriptions
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookHotel").count());
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("innerTxundoBookFlight").count());
    assertEquals(0,createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

    // the compensation handlers of the inner tx have not been invoked
    assertNull(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookHotel"));
    assertNull(runtimeService.getVariable(processInstance.getId(), "innerTxundoBookFlight"));

    // the compensation handler in the outer tx has been invoked
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    
    // end the process instance (signal the execution still sitting in afterOuterCancellation)
    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterOuterCancellation").singleResult().getId());   
    
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());  
    
  }
  
  /*
   * The cancel end event cancels all instances, compensation is performed for all instances
   * 
   * see spec page 470:
   * "If the cancelActivity attribute is set, the Activity the Event is attached to is then 
   * cancelled (in case of a multi-instance, all its instances are cancelled);"
   */
  @Deployment
  public void testMultiInstanceTx() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // there are now 5 instances of the transaction:
    
    List<EventSubscriptionEntity> EventSubscriptionEntitys = createEventSubscriptionQuery()
      .eventType("compensate")
      .list();
    
    // there are 10 compensation event subscriptions
    assertEquals(10, EventSubscriptionEntitys.size());
    
    // the event subscriptions are all under the same execution (the execution of the multi-instance wrapper)
    String executionId = EventSubscriptionEntitys.get(0).getExecutionId();
    for (EventSubscriptionEntity EventSubscriptionEntity : EventSubscriptionEntitys) {
      if(!executionId.equals(EventSubscriptionEntity.getExecutionId())) {
        fail("subscriptions not under same execution");
      }
    }
    
    Task task = taskService.createTaskQuery().listPage(0, 1).get(0);
    
    // canceling one instance triggers compensation for all other instances:    
    taskService.setVariable(task.getId(), "confirmed", false);    
    taskService.complete(task.getId());
    
    assertEquals(0, createEventSubscriptionQuery().count());
    
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    
    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterCancellation").singleResult().getId());   
    
    assertProcessEnded(processInstance.getId());    
  }
  
  @Deployment(resources={"org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultiInstanceTx.bpmn20.xml"})
  public void testMultiInstanceTxSuccessful() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // there are now 5 instances of the transaction:
    
    List<EventSubscriptionEntity> EventSubscriptionEntitys = createEventSubscriptionQuery()
      .eventType("compensate")
      .list();
    
    // there are 10 compensation event subscriptions
    assertEquals(10, EventSubscriptionEntitys.size());
    
    // the event subscriptions are all under the same execution (the execution of the multi-instance wrapper)
    String executionId = EventSubscriptionEntitys.get(0).getExecutionId();
    for (EventSubscriptionEntity EventSubscriptionEntity : EventSubscriptionEntitys) {
      if(!executionId.equals(EventSubscriptionEntity.getExecutionId())) {
        fail("subscriptions not under same execution");
      }
    }
    
    // first complete the inner user-tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.setVariable(task.getId(), "confirmed", true);    
      taskService.complete(task.getId());        
    }
    
    // now complete the inner receive tasks    
    List<Execution> executions = runtimeService.createExecutionQuery().activityId("receive").list();
    for (Execution execution : executions) {
      runtimeService.signal(execution.getId());      
    }
   
    runtimeService.signal(runtimeService.createExecutionQuery().activityId("afterSuccess").singleResult().getId());   
    
    assertEquals(0, createEventSubscriptionQuery().count());
    assertProcessEnded(processInstance.getId());
        
  }
  
  public void testMultipleCancelBoundaryFails() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultipleCancelBoundaryFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("multiple boundary events with cancelEventDefinition not supported on same transaction")) {
        fail("different exception expected");
      }
    }    
  }
  
  public void testCancelBoundaryNoTransactionFails() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCancelBoundaryNoTransactionFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("boundary event with cancelEventDefinition only supported on transaction subprocesses")) {
        fail("different exception expected");
      }
    }    
  }
  
  public void testCancelEndNoTransactionFails() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testCancelEndNoTransactionFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("end event with cancelEventDefinition only supported inside transaction subprocess")) {
        fail("different exception expected");
      }
    }    
  }
  
  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }
  
  @Deployment
  public void testParseWithDI() {
    
    // this test simply makes sure we can parse a transaction subprocess with DI information
    // the actual transaction behavior is tested by other testcases 
    
    //// failing case
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TransactionSubProcessTest");
    
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariable(task.getId(), "confirmed", false);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    
    ////// success case
    
    processInstance = runtimeService.startProcessInstanceByKey("TransactionSubProcessTest");
    
    task = taskService.createTaskQuery().singleResult();
    taskService.setVariable(task.getId(), "confirmed", true);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
}
