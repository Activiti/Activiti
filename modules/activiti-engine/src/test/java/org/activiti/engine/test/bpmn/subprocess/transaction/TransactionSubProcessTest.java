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

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.EventSubscription;
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
    
    assertEquals(6,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
            
    Task task = taskService.createTaskQuery()
      .singleResult();
    
    taskService.setVariable(task.getId(), "confirmed", true);
    
    taskService.complete(task.getId());
        
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookFlight")
            .count());
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookHotel")
            .count());
    
    assertProcessEnded(processInstance.getId());   
    
  }
  
  @Deployment(resources={"org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testSimpleCase.bpmn20.xml"})
  public void testSimpleCaseTxCancelled() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    Task task = taskService.createTaskQuery()
      .singleResult();
    
    taskService.setVariable(task.getId(), "confirmed", false);
    
    taskService.complete(task.getId());
    
    waitForJobExecutorToProcessAllJobs(10000, 100);
    
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookFlight")
            .count());
    
    assertEquals(5, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookHotel")
            .count());
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoChargeCard")
            .count());
    
    assertProcessEnded(processInstance.getId());
    
  }
  

  @Deployment
  public void testCancelEndConcurrent() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    assertEquals(6,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());

    Task task = taskService.createTaskQuery()
      .singleResult();
    
    taskService.setVariable(task.getId(), "confirmed", false);
    
    taskService.complete(task.getId());
    
    waitForJobExecutorToProcessAllJobs(10000, 100);
    
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
    
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookFlight")
            .count());
    
    assertEquals(5, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookHotel")
            .count());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testNestedCancelInner() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    Task task = taskService.createTaskQuery()
      .singleResult();
    
    taskService.setVariable(task.getId(), "confirmed", false);
    
    taskService.complete(task.getId());
        
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
    
    assertEquals(5, historyService.createHistoricActivityInstanceQuery()
      .activityId("innerTxundoBookHotel")
      .count());
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
            .activityId("innerTxundoBookFlight")
            .count());
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
            .activityId("undoBookFlight")
            .count());
    
    assertProcessEnded(processInstance.getId());    
  }
  
  @Deployment
  public void testNestedCancelOuter() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    Task task = taskService.createTaskQuery()
      .taskDefinitionKey("taskInOuterTx")
      .singleResult();
    
    taskService.complete(task.getId());
        
    assertEquals(0,runtimeService.createEventSubscriptionQuery().eventType("compensate").count());
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery()
            .activityId("innerTxundoBookHotel")
            .count());
          
    assertEquals(0, historyService.createHistoricActivityInstanceQuery()
                  .activityId("innerTxundoBookFlight")
                  .count());
          
    assertEquals(1, historyService.createHistoricActivityInstanceQuery()
                  .activityId("undoBookFlight")
                  .count());
    
    assertProcessEnded(processInstance.getId());
    
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
    
    List<EventSubscription> eventSubscriptions = runtimeService
      .createEventSubscriptionQuery()
      .eventType("compensate")
      .list();
    
    // there are 10 compensation event subscriptions
    assertEquals(10, eventSubscriptions.size());
    
    // the event subscriptions are all under the same execution (the execution of the multi-instance wrapper)
    String executionId = eventSubscriptions.get(0).getExecutionId();
    for (EventSubscription eventSubscription : eventSubscriptions) {
      if(!executionId.equals(eventSubscription.getExecutionId())) {
        fail("subscriptions not under same execution");
      }
    }
    
    Task task = taskService.createTaskQuery().listPage(0, 1).get(0);
    
    // canceling one instance triggers compensation for all other instances:    
    taskService.setVariable(task.getId(), "confirmed", false);    
    taskService.complete(task.getId());
    
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
    
    assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookFlight").count());
    assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());   
    
    assertProcessEnded(processInstance.getId());    
  }
  
  @Deployment(resources={"org/activiti/engine/test/bpmn/subprocess/transaction/TransactionSubProcessTest.testMultiInstanceTx.bpmn20.xml"})
  public void testMultiInstanceTxSuccessful() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");
    
    // there are now 5 instances of the transaction:
    
    List<EventSubscription> eventSubscriptions = runtimeService
      .createEventSubscriptionQuery()
      .eventType("compensate")
      .list();
    
    // there are 10 compensation event subscriptions
    assertEquals(10, eventSubscriptions.size());
    
    // the event subscriptions are all under the same execution (the execution of the multi-instance wrapper)
    String executionId = eventSubscriptions.get(0).getExecutionId();
    for (EventSubscription eventSubscription : eventSubscriptions) {
      if(!executionId.equals(eventSubscription.getExecutionId())) {
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
   
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());
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
}
