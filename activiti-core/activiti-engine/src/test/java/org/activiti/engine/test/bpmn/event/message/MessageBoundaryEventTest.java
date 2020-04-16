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

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class MessageBoundaryEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSingleBoundaryMessageEvent() {
    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(3);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();

    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNotNull();

    // 1. case: message received cancels the task

    runtimeService.messageEventReceived("messageName", execution.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // 2nd. case: complete the user task cancels the message subscription

    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

  }

  public void testDoubleBoundaryMessageEventSameMessageId() {
    // deployment fails when two boundary message events have the same messageId
    assertThatExceptionOfType(Exception.class)
      .as("Deployment should fail because Activiti cannot handle two boundary message events with same messageId.")
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageBoundaryEventTest.testDoubleBoundaryMessageEventSameMessageId.bpmn20.xml")
        .deploy());
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testDoubleBoundaryMessageEvent() {
    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(4);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();

    // the executions for both messageEventSubscriptionNames are not the same
    Execution execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    assertThat(execution1).isNotNull();

    Execution execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    assertThat(execution2).isNotNull();

    assertThat(execution1.getId().equals(execution2.getId())).isFalse();

    // /////////////////////////////////////////////////////////////////////////////////
    // 1. first message received cancels the task and the execution and both subscriptions
    runtimeService.messageEventReceived("messageName_1", execution1.getId());

    // this should then throw an exception because execution2 no longer exists
    String executionIdForException = execution2.getId();
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.messageEventReceived("messageName_2", executionIdForException));

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage_1");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // ///////////////////////////////////////////////////////////////////
    // 2. complete the user task cancels the message subscriptions

    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    assertThat(execution1).isNull();
    execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    assertThat(execution2).isNull();

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testDoubleBoundaryMessageEventMultiInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    // assume we have 9 executions one process instance
    // one execution for scope created for boundary message event
    // five execution because we have loop cardinality 5
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(9);

    assertThat(taskService.createTaskQuery().count()).isEqualTo(5);

    Execution execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    Execution execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    // both executions are not the same
    assertThat(execution1.getId().equals(execution2.getId())).isFalse();

    // /////////////////////////////////////////////////////////////////////////////////
    // 1. first message received cancels all tasks and the executions and both subscriptions
    runtimeService.messageEventReceived("messageName_1", execution1.getId());

    // this should then throw an exception because execution2 no longer exists
    String executionIdForException = execution2.getId();
    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> runtimeService.messageEventReceived("messageName_2", executionIdForException));

    // only process instance and running execution left
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(2);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage_1");
    taskService.complete(userTask.getId());
    assertProcessEnded(processInstance.getId());

    // /////////////////////////////////////////////////////////////////////////////////
    // 2. complete the user task cancels the message subscriptions

    processInstance = runtimeService.startProcessInstanceByKey("process");
    // assume we have 7 executions one process instance
    // one execution for scope created for boundary message event
    // five execution because we have loop cardinality 5
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(9);

    assertThat(taskService.createTaskQuery().count()).isEqualTo(5);

    execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    // executions are not the same
    assertThat(execution1.getId().equals(execution2.getId())).isFalse();

    List<Task> userTasks = taskService.createTaskQuery().list();
    assertThat(userTasks).isNotNull();
    assertThat(userTasks).hasSize(5);

    // as long as tasks exists, the message subscriptions exist
    for (int i = 0; i < userTasks.size() - 1; i++) {
      Task task = userTasks.get(i);
      taskService.complete(task.getId());

      execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
      assertThat(execution1).isNotNull();
      execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
      assertThat(execution2).isNotNull();
    }

    // only one task left
    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    // after last task is completed, no message subscriptions left
    execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_1").singleResult();
    assertThat(execution1).isNull();
    execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_2").singleResult();
    assertThat(execution2).isNull();

    // complete last task to end process
    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");
    taskService.complete(userTask.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testBoundaryMessageEventInsideSubprocess() {

    // this time the boundary events are placed on a user task that is
    // contained inside a sub process

    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(4);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();

    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNotNull();

    // /////////////////////////////////////////////////
    // 1. case: message received cancels the task

    runtimeService.messageEventReceived("messageName", execution.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // /////////////////////////////////////////////////
    // 2nd. case: complete the user task cancels the message subscription

    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testBoundaryMessageEventOnSubprocessAndInsideSubprocess() {

    // this time the boundary events are placed on a user task that is
    // contained inside a sub process
    // and on the subprocess itself

    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(5);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();

    Execution execution1 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution1).isNotNull();

    Execution execution2 = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    assertThat(execution2).isNotNull();

    assertThat(execution2.getId()).isNotSameAs(execution1.getId());

    // ///////////////////////////////////////////////////////////
    // first case: we complete the inner usertask.

    taskService.complete(userTask.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");

    // the inner subscription is cancelled
    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    // the outer subscription still exists
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    assertThat(execution).isNotNull();

    // now complete the second usertask
    taskService.complete(userTask.getId());

    // now the outer event subscription is cancelled as well
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    assertThat(execution).isNull();

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterSubprocess");

    // now complete the outer usertask
    taskService.complete(userTask.getId());

    // ///////////////////////////////////////////////////////////
    // second case: we signal the inner message event

    runtimeService.startProcessInstanceByKey("process");

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    runtimeService.messageEventReceived("messageName", execution.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage");

    // the inner subscription is removes
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    // the outer subscription still exists
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    assertThat(execution).isNotNull();

    // now complete the second usertask
    taskService.complete(userTask.getId());

    // now the outer event subscription is cancelled as well
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    assertThat(execution).isNull();

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterSubprocess");

    // now complete the outer usertask
    taskService.complete(userTask.getId());

    // ///////////////////////////////////////////////////////////
    // third case: we signal the outer message event

    runtimeService.startProcessInstanceByKey("process");

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    runtimeService.messageEventReceived("messageName2", execution.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterOuterMessageBoundary");

    // the inner subscription is removed
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    // the outer subscription is removed
    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").singleResult();
    assertThat(execution).isNull();

    // now complete the second usertask
    taskService.complete(userTask.getId());

    // and we are done

  }

  @Deployment
  public void testBoundaryMessageEventOnSubprocess() {
    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(5);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();

    // 1. case: message one received cancels the task

    Execution executionMessageOne = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_one").singleResult();
    assertThat(executionMessageOne).isNotNull();

    runtimeService.messageEventReceived("messageName_one", executionMessageOne.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage_one");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // 2nd. case: message two received cancels the task

    runtimeService.startProcessInstanceByKey("process");

    Execution executionMessageTwo = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_two").singleResult();
    assertThat(executionMessageTwo).isNotNull();

    runtimeService.messageEventReceived("messageName_two", executionMessageTwo.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterMessage_two");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // 3rd. case: complete the user task cancels the message subscription

    runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    executionMessageOne = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_one").singleResult();
    assertThat(executionMessageOne).isNull();

    executionMessageTwo = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName_two").singleResult();
    assertThat(executionMessageTwo).isNull();

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterSubProcess");
    taskService.complete(userTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

  }

  @Deployment
  public void testBoundaryMessageEventOnSubprocessAndInsideSubprocessMultiInstance() {

    // this time the boundary events are placed on a user task that is
    // contained inside a sub process
    // and on the subprocess itself

    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(18);

    // 5 user tasks
    List<Task> userTasks = taskService.createTaskQuery().list();
    assertThat(userTasks).isNotNull();
    assertThat(userTasks).hasSize(5);

    // there are 5 event subscriptions to the event on the inner user task
    List<Execution> executions = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").list();
    assertThat(executions).isNotNull();
    assertThat(executions).hasSize(5);

    // there is a single event subscription for the event on the subprocess
    executions = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName2").list();
    assertThat(executions).isNotNull();
    assertThat(executions).hasSize(1);

    // if we complete the outer message event, all inner executions are
    // removed
    Execution outerScopeExecution = executions.get(0);
    runtimeService.messageEventReceived("messageName2", outerScopeExecution.getId());

    executions = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").list();
    assertThat(executions).hasSize(0);

    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterOuterMessageBoundary");

    taskService.complete(userTask.getId());

    // and we are done

  }

  @Deployment
  public void testSingleBoundaryMessageEventWithBoundaryTimerEvent() {
    final Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);

    runtimeService.startProcessInstanceByKey("process");

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(3);

    Execution execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    // ///////////////////////////////////
    // Verify the first task
    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("task");

    // ///////////////////////////////////
    // Advance the clock to trigger the timer.
    final TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(userTask.getProcessInstanceId());
    assertThat(jobQuery.count()).isEqualTo(1);

    // After setting the clock to time '1 hour and 5 seconds', the timer should fire.
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorOnCondition(5000L, 100L, new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return taskService.createTaskQuery().count() == 2;
      }
    });

    // It is a repeating job, so it will come back.
    assertThat(jobQuery.count()).isEqualTo(1L);

    // ///////////////////////////////////
    // Verify and complete the first task
    userTask = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    // ///////////////////////////////////
    // Complete the after timer task
    userTask = taskService.createTaskQuery().taskDefinitionKey("taskTimer").singleResult();
    assertThat(userTask).isNotNull();
    taskService.complete(userTask.getId());

    // Timer job of boundary event of task should be deleted and timer job
    // of task timer boundary event should be created.
    assertThat(jobQuery.count()).isEqualTo(1L);

    // ///////////////////////////////////
    // Verify that the message exists
    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNotNull();

    // ///////////////////////////////////
    // Send the message and verify that we went back to the right spot.
    runtimeService.messageEventReceived("messageName", execution.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("task");

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();

    // ///////////////////////////////////
    // Complete the first task (again).
    taskService.complete(userTask.getId());

    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTask");

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNotNull();

    // ///////////////////////////////////
    // Verify timer firing.

    // After setting the clock to time '2 hours and 5 seconds', the timer
    // should fire.
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
    waitForJobExecutorOnCondition(2000L, 100L, new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return taskService.createTaskQuery().count() == 2;
      }
    });

    // It is a repeating job, so it will come back.
    assertThat(jobQuery.count()).isEqualTo(1L);

    // After setting the clock to time '3 hours and 5 seconds', the timer should fire again.
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((3 * 60 * 60 * 1000) + 5000)));
    waitForJobExecutorOnCondition(2000L, 100L, new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return taskService.createTaskQuery().list().size() == 3;
      }
    });
    // It is a repeating job, so it will come back.
    assertThat(jobQuery.count()).isEqualTo(1L);

    // ///////////////////////////////////
    // Complete the after timer tasks
    final List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("taskAfterTaskTimer").list();
    assertThat(tasks).hasSize(2);

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // ///////////////////////////////////
    // Complete the second task
    taskService.complete(userTask.getId());

    // ///////////////////////////////////
    // Complete the third task
    userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getTaskDefinitionKey()).isEqualTo("taskAfterTaskAfterTask");
    taskService.complete(userTask.getId());

    // ///////////////////////////////////
    // We should be done at this point
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    execution = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(execution).isNull();
  }

}
