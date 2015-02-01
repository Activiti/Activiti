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

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer (camunda)
 * @author Kristin Polenz (camunda)
 * @author Christian Lipphardt (camunda)
 */
public class MessageBoundaryEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testSingleBoundaryMessageEvent() {
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(2, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    
    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNotNull(execution);
    
    // 1. case: message received cancels the task
    
    runtimeService.messageEventReceived("messageName", execution.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    // 2nd. case: complete the user task cancels the message subscription
    
    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNull(execution);    
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
  }
  
  public void testDoubleBoundaryMessageEventSameMessageId() {
    // deployment fails when two boundary message events have the same messageId
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageBoundaryEventTest.testDoubleBoundaryMessageEventSameMessageId.bpmn20.xml")
        .deploy();
      fail("Deployment should fail because Activiti cannot handle two boundary message events with same messageId.");
    } catch (Exception e) {
      assertEquals(0, repositoryService.createDeploymentQuery().count());
    }
  }
  
  @Deployment
  public void testDoubleBoundaryMessageEvent() {
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(2, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    
    // the executions for both messageEventSubscriptionNames are the same
    Execution execution1 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_1")
      .singleResult();
    assertNotNull(execution1);
    
    Execution execution2 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_2")
      .singleResult();
    assertNotNull(execution2);
    
    assertEquals(execution1.getId(), execution2.getId());
    
    ///////////////////////////////////////////////////////////////////////////////////
    // 1. first message received cancels the task and the execution and both subscriptions
    runtimeService.messageEventReceived("messageName_1", execution1.getId());
    
    // this should then throw an exception because execution2 no longer exists
    try {
      runtimeService.messageEventReceived("messageName_2", execution2.getId());
      fail();
    } catch (Exception e) {
      // This is good
    }
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage_1", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    /////////////////////////////////////////////////////////////////////
    // 2. complete the user task cancels the message subscriptions
    
    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    execution1 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_1")
      .singleResult();
    assertNull(execution1);
    execution2 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_2")
      .singleResult();
    assertNull(execution2);
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
  @Deployment
  public void testDoubleBoundaryMessageEventMultiInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    // assume we have 7 executions
    // one process instance
    // one execution for scope created for boundary message event
    // five execution because we have loop cardinality 5
    assertEquals(7, runtimeService.createExecutionQuery().count());
    
    assertEquals(5, taskService.createTaskQuery().count());
    
    Execution execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    Execution execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    // both executions are the same
    assertEquals(execution1.getId(), execution2.getId());
    
    ///////////////////////////////////////////////////////////////////////////////////
    // 1. first message received cancels all tasks and the executions and both subscriptions
    runtimeService.messageEventReceived("messageName_1", execution1.getId());
    
    // this should then throw an exception because execution2 no longer exists
    try {
      runtimeService.messageEventReceived("messageName_2", execution2.getId());
      fail();
    } catch (Exception e) {
      // This is good
    }
    
    // only process instance left
    assertEquals(1, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage_1", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertProcessEnded(processInstance.getId());
    
    
    ///////////////////////////////////////////////////////////////////////////////////
    // 2. complete the user task cancels the message subscriptions
    
    processInstance = runtimeService.startProcessInstanceByKey("process");
    // assume we have 7 executions
    // one process instance
    // one execution for scope created for boundary message event
    // five execution because we have loop cardinality 5
    assertEquals(7, runtimeService.createExecutionQuery().count());
    
    assertEquals(5, taskService.createTaskQuery().count());
    
    execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    // both executions are the same
    assertEquals(execution1.getId(), execution2.getId());
 
    List<Task> userTasks = taskService.createTaskQuery().list();
    assertNotNull(userTasks);
    assertEquals(5, userTasks.size());
    
    // as long as tasks exists, the message subscriptions exist
    for (int i = 0; i < userTasks.size()-1; i++) {
      Task task = userTasks.get(i);
      taskService.complete(task.getId());
      
      execution1 = runtimeService.createExecutionQuery()
              .messageEventSubscriptionName("messageName_1")
              .singleResult();
      assertNotNull(execution1);
      execution2 = runtimeService.createExecutionQuery()
              .messageEventSubscriptionName("messageName_2")
              .singleResult();
      assertNotNull(execution2);
    }
    
    // only one task left
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    // after last task is completed, no message subscriptions left
    execution1 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_1")
      .singleResult();
    assertNull(execution1);
    execution2 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_2")
      .singleResult();
    assertNull(execution2);
    
    // complete last task to end process
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testBoundaryMessageEventInsideSubprocess() {
    
    // this time the boundary events are placed on a user task that is contained inside a sub process
    
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(3, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    
    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNotNull(execution);
    
    ///////////////////////////////////////////////////
    // 1. case: message received cancels the task
    
    runtimeService.messageEventReceived("messageName", execution.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    ///////////////////////////////////////////////////
    // 2nd. case: complete the user task cancels the message subscription
    
    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNull(execution);    
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
  @Deployment
  public void testBoundaryMessageEventOnSubprocessAndInsideSubprocess() {
    
    // this time the boundary events are placed on a user task that is contained inside a sub process 
    // and on the subprocess itself
    
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(3, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    
    Execution execution1 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNotNull(execution1);
    
    Execution execution2 = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName2")
      .singleResult();
    assertNotNull(execution2);
    
    assertNotSame(execution1.getId(), execution2.getId());
    
    /////////////////////////////////////////////////////////////
    // first case: we complete the inner usertask.
    
    taskService.complete(userTask.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    
    // the inner subscription is cancelled
    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNull(execution);
    
    // the outer subscription still exists
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName2")
            .singleResult();
    assertNotNull(execution);
    
    // now complete the second usertask
    taskService.complete(userTask.getId());
    
    // now the outer event subscription is cancelled as well
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName2")
            .singleResult();
    assertNull(execution);
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterSubprocess", userTask.getTaskDefinitionKey());
    
    // now complete the outer usertask
    taskService.complete(userTask.getId());
    
    /////////////////////////////////////////////////////////////
    // second case: we signal the inner message event
    
    runtimeService.startProcessInstanceByKey("process");
    
    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    runtimeService.messageEventReceived("messageName", execution.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage", userTask.getTaskDefinitionKey());
    
    // the inner subscription is removes
    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNull(execution);
    
    // the outer subscription still exists
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName2")
            .singleResult();
    assertNotNull(execution);
    
    // now complete the second usertask
    taskService.complete(userTask.getId());
    
    // now the outer event subscription is cancelled as well
    execution = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName2")
            .singleResult();
    assertNull(execution);
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterSubprocess", userTask.getTaskDefinitionKey());
    
    // now complete the outer usertask
    taskService.complete(userTask.getId());
    
    /////////////////////////////////////////////////////////////
    // third case: we signal the outer message event
    
    runtimeService.startProcessInstanceByKey("process");
    
    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName2")
      .singleResult();
    runtimeService.messageEventReceived("messageName2", execution.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterOuterMessageBoundary", userTask.getTaskDefinitionKey());
    
    // the inner subscription is removed
    execution = runtimeService.createExecutionQuery()
    .messageEventSubscriptionName("messageName")
    .singleResult();
    assertNull(execution);
    
    // the outer subscription is removed
    execution = runtimeService.createExecutionQuery()
    .messageEventSubscriptionName("messageName2")
    .singleResult();
    assertNull(execution);
    
    // now complete the second usertask
    taskService.complete(userTask.getId());
    
    // and we are done
    
  }
  

  @Deployment
  public void testBoundaryMessageEventOnSubprocess() {
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(2, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    
    // 1. case: message one received cancels the task

    Execution executionMessageOne = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName_one")
            .singleResult();
    assertNotNull(executionMessageOne);

    runtimeService.messageEventReceived("messageName_one", executionMessageOne.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage_one", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    // 2nd. case: message two received cancels the task
    
    runtimeService.startProcessInstanceByKey("process");

    Execution executionMessageTwo = runtimeService.createExecutionQuery()
            .messageEventSubscriptionName("messageName_two")
            .singleResult();
    assertNotNull(executionMessageTwo);    
    
    runtimeService.messageEventReceived("messageName_two", executionMessageTwo.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage_two", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    
    // 3rd. case: complete the user task cancels the message subscription
    
    runtimeService.startProcessInstanceByKey("process");
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    executionMessageOne = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName_one")
      .singleResult();
    assertNull(executionMessageOne);
    
    executionMessageTwo = runtimeService.createExecutionQuery()
     .messageEventSubscriptionName("messageName_two")
     .singleResult();
    assertNull(executionMessageTwo);
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterSubProcess", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());      
    
  }  
  
  @Deployment
  public void testBoundaryMessageEventOnSubprocessAndInsideSubprocessMultiInstance() {
    
    // this time the boundary events are placed on a user task that is contained inside a sub process 
    // and on the subprocess itself
    
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(17, runtimeService.createExecutionQuery().count());
    
    // 5 user tasks
    List<Task> userTasks = taskService.createTaskQuery().list();
    assertNotNull(userTasks);
    assertEquals(5, userTasks.size());
    
    // there are 5 event subscriptions to the event on the inner user task
    List<Execution> executions = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .list();
    assertNotNull(executions);
    assertEquals(5, executions.size());
    
    // there is a single event subscription for the event on the subprocess
    executions = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName2")
      .list();
    assertNotNull(executions);
    assertEquals(1, executions.size());
    
    // if we complete the outer message event, all inner executions are removed
    Execution outerScopeExecution = executions.get(0);
    runtimeService.messageEventReceived("messageName2", outerScopeExecution.getId());
    
    executions = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .list();
    assertEquals(0, executions.size());
    
    Task userTask = taskService.createTaskQuery()
      .singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterOuterMessageBoundary", userTask.getTaskDefinitionKey());
    
    taskService.complete(userTask.getId());
    
    // and we are done
    
  }
  
  @Deployment
  public void testSingleBoundaryMessageEventWithBoundaryTimerEvent() {
    final Date startTime = new Date();

    runtimeService.startProcessInstanceByKey("process");

    assertEquals(2, runtimeService.createExecutionQuery().count());

    Execution execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("messageName")
        .singleResult();
    assertNull(execution);

    // ///////////////////////////////////
    // Verify the first task
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("task", userTask.getTaskDefinitionKey());

    // ///////////////////////////////////
    // Advance the clock to trigger the timer.
     final JobQuery jobQuery =
           managementService.createJobQuery().processInstanceId(userTask.getProcessInstanceId());
     assertEquals(1, jobQuery.count());

    // After setting the clock to time '1 hour and 5 seconds', the timer should fire.
    processEngineConfiguration.getClock().setCurrentTime(
        new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorOnCondition(12000L, 100L, new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return taskService.createTaskQuery().count() == 2;
      }
    });
    
    // It is a repeating job, so it will come back.
    assertEquals(1L, jobQuery.count());

    // ///////////////////////////////////
    // Verify and complete the first task
    userTask = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());

    // ///////////////////////////////////
    // Complete the after timer task
    userTask = taskService.createTaskQuery().taskDefinitionKey("taskTimer").singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    // Timer job of boundary event of task should be deleted and timer job of task timer boundary event should be created.
    assertEquals(1L, jobQuery.count());

    // ///////////////////////////////////
    // Verify that the message exists
    userTask = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());

    execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("messageName")
        .singleResult();
    assertNotNull(execution);

    // ///////////////////////////////////
    // Send the message and verify that we went back to the right spot.
    runtimeService.messageEventReceived("messageName", execution.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("task", userTask.getTaskDefinitionKey());

    execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("messageName")
        .singleResult();
    assertNull(execution);

    // ///////////////////////////////////
    // Complete the first task (again).
    taskService.complete(userTask.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());

    execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("messageName")
        .singleResult();
    assertNotNull(execution);

    // ///////////////////////////////////
    // Verify timer firing.

    // After setting the clock to time '2 hours and 5 seconds', the timer should fire.
    processEngineConfiguration.getClock().setCurrentTime(
        new Date(startTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
    waitForJobExecutorOnCondition(2000L, 100L, new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return taskService.createTaskQuery().count() == 2;
      }
    });
    
    // It is a repeating job, so it will come back.
    assertEquals(1L, jobQuery.count());

    // After setting the clock to time '3 hours and 5 seconds', the timer should fire again.
    processEngineConfiguration.getClock().setCurrentTime(
        new Date(startTime.getTime() + ((3 * 60 * 60 * 1000) + 5000)));
    waitForJobExecutorOnCondition(2000L, 100L, new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return taskService.createTaskQuery().list().size() == 3;
      }
    });
    // It is a repeating job, so it will come back.
    assertEquals(1L, jobQuery.count());

    // ///////////////////////////////////
    // Complete the after timer tasks
    final List<Task> tasks =
        taskService.createTaskQuery().taskDefinitionKey("taskAfterTaskTimer").list();
    assertEquals(2, tasks.size());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // ///////////////////////////////////
    // Complete the second task
    taskService.complete(userTask.getId());

    // ///////////////////////////////////
    // Complete the third task
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTaskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());

    // ///////////////////////////////////
    // We should be done at this point
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    execution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName("messageName")
        .singleResult();
    assertNull(execution);
  }

}
