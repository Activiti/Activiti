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

package org.activiti.engine.test.bpmn.gateway;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 */
public class InclusiveGatewayTest extends PluggableActivitiTestCase {

  private static final String TASK1_NAME = "Task 1";
  private static final String TASK2_NAME = "Task 2";
  private static final String TASK3_NAME = "Task 3";

  private static final String BEAN_TASK1_NAME = "Basic service";
  private static final String BEAN_TASK2_NAME = "Standard service";
  private static final String BEAN_TASK3_NAME = "Gold Member service";

  @Deployment
  public void testDivergingInclusiveGateway() {
    for (int i = 1; i <= 3; i++) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGwDiverging", singletonMap("input", i));
      List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
      List<String> expectedNames = new ArrayList<>();
      if (i == 1) {
        expectedNames.add(TASK1_NAME);
      }
      if (i <= 2) {
        expectedNames.add(TASK2_NAME);
      }
      expectedNames.add(TASK3_NAME);
      assertThat(tasks).hasSize(4 - i);
      for (Task task : tasks) {
        expectedNames.remove(task.getName());
      }
      assertThat(expectedNames).hasSize(0);
      runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
    }
  }

  @Deployment
  public void testMergingInclusiveGateway() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGwMerging", singletonMap("input", 2));
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

    runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
  }

  @Deployment
  public void testPartialMergingInclusiveGateway() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("partialInclusiveGwMerging", singletonMap("input", 2));
    Task partialTask = taskService.createTaskQuery().singleResult();
    assertThat(partialTask.getTaskDefinitionKey()).isEqualTo("partialTask");

    taskService.complete(partialTask.getId());

    Task fullTask = taskService.createTaskQuery().singleResult();
    assertThat(fullTask.getTaskDefinitionKey()).isEqualTo("theTask");

    runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
  }

  @Deployment
  public void testNoSequenceFlowSelected() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("inclusiveGwNoSeqFlowSelected", singletonMap("input", 4)));
  }

  /**
   * Test for ACT-1216: When merging a concurrent execution the parent is not activated correctly
   */
  @Deployment
  public void testParentActivationOnNonJoiningEnd() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parentActivationOnNonJoiningEnd");

    List<Execution> executionsBefore = runtimeService.createExecutionQuery().list();
    assertThat(executionsBefore).hasSize(3);

    // start first round of tasks
    List<Task> firstTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(firstTasks).hasSize(2);

    for (Task t : firstTasks) {
      taskService.complete(t.getId());
    }

    // start second round of tasks
    List<Task> secondTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(secondTasks).hasSize(2);

    // complete one task
    Task task = secondTasks.get(0);
    taskService.complete(task.getId());

    List<Execution> executionsAfter = runtimeService.createExecutionQuery().list();
    assertThat(executionsAfter).hasSize(2);

    Execution execution = null;
    for (Execution e : executionsAfter) {
      if (e.getParentId() != null) {
        execution = e;
      }
    }

    // and should have one active activity
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(execution.getId());
    assertThat(activeActivityIds).hasSize(1);

    // Completing last task should finish the process instance

    Task lastTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(lastTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(0l);
  }

  /**
   * Test for bug ACT-10: whitespaces/newlines in expressions lead to exceptions
   */
  @Deployment
  public void testWhitespaceInExpression() {
    // Starting a process instance will lead to an exception if whitespace
    // are
    // incorrectly handled
    runtimeService.startProcessInstanceByKey("inclusiveWhiteSpaceInExpression", singletonMap("input", 1));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testDivergingInclusiveGateway.bpmn20.xml" })
  public void testUnknownVariableInExpression() {
    // Instead of 'input' we're starting a process instance with the name 'iinput' (ie. a typo)
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("inclusiveGwDiverging", singletonMap("iinput", 1)))
      .withMessageContaining("Unknown property used in expression");
  }

  @Deployment
  public void testDecideBasedOnBeanProperty() {
    runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanProperty", singletonMap("order", new InclusiveGatewayTestOrder(150)));
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);
    Map<String, String> expectedNames = new HashMap<String, String>();
    expectedNames.put(BEAN_TASK2_NAME, BEAN_TASK2_NAME);
    expectedNames.put(BEAN_TASK3_NAME, BEAN_TASK3_NAME);
    for (Task task : tasks) {
      expectedNames.remove(task.getName());
    }
    assertThat(expectedNames).hasSize(0);
  }

  @Deployment
  public void testDecideBasedOnListOrArrayOfBeans() {
    List<InclusiveGatewayTestOrder> orders = new ArrayList<InclusiveGatewayTestOrder>();
    orders.add(new InclusiveGatewayTestOrder(50));
    orders.add(new InclusiveGatewayTestOrder(300));
    orders.add(new InclusiveGatewayTestOrder(175));

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", singletonMap("orders", orders)));

    orders.set(1, new InclusiveGatewayTestOrder(175));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", singletonMap("orders", orders));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo(BEAN_TASK3_NAME);

    orders.set(1, new InclusiveGatewayTestOrder(125));
    pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", singletonMap("orders", orders));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(2);
    List<String> expectedNames = new ArrayList<String>();
    expectedNames.add(BEAN_TASK2_NAME);
    expectedNames.add(BEAN_TASK3_NAME);
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertThat(expectedNames).hasSize(0);

    // Arrays are usable in exactly the same way
    InclusiveGatewayTestOrder[] orderArray = orders.toArray(new InclusiveGatewayTestOrder[orders.size()]);
    orderArray[1].setPrice(10);
    pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", singletonMap("orders", orderArray));
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(3);
    expectedNames.clear();
    expectedNames.add(BEAN_TASK1_NAME);
    expectedNames.add(BEAN_TASK2_NAME);
    expectedNames.add(BEAN_TASK3_NAME);
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertThat(expectedNames).hasSize(0);
  }

  @Deployment
  public void testDecideBasedOnBeanMethod() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanMethod", singletonMap("order", new InclusiveGatewayTestOrder(200)));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo(BEAN_TASK3_NAME);

    pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanMethod", singletonMap("order", new InclusiveGatewayTestOrder(125)));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);
    List<String> expectedNames = new ArrayList<String>();
    expectedNames.add(BEAN_TASK2_NAME);
    expectedNames.add(BEAN_TASK3_NAME);
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertThat(expectedNames).hasSize(0);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanMethod", singletonMap("order", new InclusiveGatewayTestOrder(300))));

  }

  @Deployment
  public void testInvalidMethodExpression() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("inclusiveInvalidMethodExpression", singletonMap("order", new InclusiveGatewayTestOrder(50))))
      .withMessageContaining("Unknown method used in expression");
  }

  @Deployment
  public void testDefaultSequenceFlow() {
    // Input == 1 -> default is not selected, other 2 tasks are selected
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGwDefaultSequenceFlow", singletonMap("input", 1));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);
    Map<String, String> expectedNames = new HashMap<String, String>();
    expectedNames.put("Input is one", "Input is one");
    expectedNames.put("Input is three or one", "Input is three or one");
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertThat(expectedNames).hasSize(0);
    runtimeService.deleteProcessInstance(pi.getId(), null);

    // Input == 3 -> default is not selected, "one or three" is selected
    pi = runtimeService.startProcessInstanceByKey("inclusiveGwDefaultSequenceFlow", singletonMap("input", 3));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Input is three or one");

    // Default input
    pi = runtimeService.startProcessInstanceByKey("inclusiveGwDefaultSequenceFlow", singletonMap("input", 5));
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Default input");
  }

  @Deployment
  public void testNoIdOnSequenceFlow() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveNoIdOnSequenceFlow", singletonMap("input", 3));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Input is more than one");

    // Both should be enabled on 1
    pi = runtimeService.startProcessInstanceByKey("inclusiveNoIdOnSequenceFlow", singletonMap("input", 1));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);
    Map<String, String> expectedNames = new HashMap<String, String>();
    expectedNames.put("Input is one", "Input is one");
    expectedNames.put("Input is more than one", "Input is more than one");
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertThat(expectedNames).hasSize(0);
  }

  /**
   * This test the isReachable() check that is done to check if upstream tokens can reach the inclusive gateway.
   *
   * In case of loops, special care needs to be taken in the algorithm, or else stackoverflows will happen very quickly.
   */
  @Deployment
  public void testLoop() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveTestLoop", singletonMap("counter", 1));

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task C");

    taskService.complete(task.getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);

    assertThat(runtimeService.createExecutionQuery().count()).as("Found executions: " + runtimeService.createExecutionQuery().list()).isEqualTo(0);
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testJoinAfterSubprocesses() {
    // Test case to test act-1204
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("a", 1);
    variableMap.put("b", 1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("InclusiveGateway", variableMap);
    assertThat(processInstance.getId()).isNotNull();

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(taskService.createTaskQuery().count()).isEqualTo(2);

    taskService.complete(tasks.get(0).getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

    taskService.complete(tasks.get(1).getId());

    Task task = taskService.createTaskQuery().taskAssignee("c").singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(processInstance).isNull();

    variableMap = new HashMap<String, Object>();
    variableMap.put("a", 1);
    variableMap.put("b", 2);
    processInstance = runtimeService.startProcessInstanceByKey("InclusiveGateway", variableMap);
    assertThat(processInstance.getId()).isNotNull();

    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

    task = tasks.get(0);
    assertThat(task.getAssignee()).isEqualTo("a");
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().taskAssignee("c").singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(processInstance).isNull();

    Map<String, Object> variableMapForException = new HashMap();
    variableMapForException.put("a", 2);
    variableMapForException.put("b", 2);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("InclusiveGateway", variableMapForException))
      .withMessageContaining("No outgoing sequence flow");
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testJoinAfterCall.bpmn20.xml",
      "org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testJoinAfterCallSubProcess.bpmn20.xml" })
  public void testJoinAfterCall() {
    // Test case to test act-1026
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("InclusiveGatewayAfterCall");
    assertThat(processInstance.getId()).isNotNull();
    assertThat(taskService.createTaskQuery().count()).isEqualTo(3);

    // now complete task A and check number of remaining tasks.
    // inclusive gateway should wait for the "Task B" and "Task C"
    Task taskA = taskService.createTaskQuery().taskName("Task A").singleResult();
    assertThat(taskA).isNotNull();
    taskService.complete(taskA.getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(2);

    // now complete task B and check number of remaining tasks
    // inclusive gateway should wait for "Task C"
    Task taskB = taskService.createTaskQuery().taskName("Task B").singleResult();
    assertThat(taskB).isNotNull();
    taskService.complete(taskB.getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

    // now complete task C. Gateway activates and "Task C" remains
    Task taskC = taskService.createTaskQuery().taskName("Task C").singleResult();
    assertThat(taskC).isNotNull();
    taskService.complete(taskC.getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

    // check that remaining task is in fact task D
    Task taskD = taskService.createTaskQuery().taskName("Task D").singleResult();
    assertThat(taskD).isNotNull();
    assertThat(taskD.getName()).isEqualTo("Task D");
    taskService.complete(taskD.getId());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(processInstance).isNull();
  }

  @Deployment
  public void testAsyncBehavior() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("async");
    waitForJobExecutorToProcessAllJobs(5000L);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
  }

  @Deployment
  public void testDirectSequenceFlow() {
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("input", 1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwDirectSequenceFlow", varMap);
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask1");
    taskService.complete(task.getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    varMap = new HashMap<String, Object>();
    varMap.put("input", 3);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwDirectSequenceFlow", varMap);
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    varMap = new HashMap<String, Object>();
    varMap.put("input", 0);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwDirectSequenceFlow", varMap);
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Deployment
  public void testSkipExpression() {
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    varMap.put("input", 10);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwSkipExpression", varMap);
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask1");
    taskService.complete(task.getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    varMap = new HashMap<String, Object>();
    varMap.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    varMap.put("input", 30);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwSkipExpression", varMap);
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    varMap = new HashMap<String, Object>();
    varMap.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    varMap.put("input", 3);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwSkipExpression", varMap);
    assertThat(processInstance.isEnded()).isTrue();
  }

  @Deployment
  public void testMultipleProcessInstancesMergedBug() {

    // Start first process instance, continue A. Process instance should be in C
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("testMultipleProcessInstancesMergedBug");
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).taskName("A").singleResult().getId());
    Task taskCInPi1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    assertThat(taskCInPi1).isNotNull();

    // Start second process instance, continue A. Process instance should be in B
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("testMultipleProcessInstancesMergedBug", singletonMap("var", "goToB"));
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).taskName("A").singleResult().getId());
    Task taskBInPi2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    assertThat(taskBInPi2).isNotNull();

    // Verify there is an inactive execution in the inclusive gateway before the task complete of process instance 1
    // (cannot combine activityId and inactive together, hence the workaround)
    assertThat(getInactiveExecutionsInActivityId("inclusiveGw")).hasSize(2);

    // Completing C of PI 1 should not trigger C
    taskService.complete(taskCInPi1.getId());

    // Verify structure after complete.
    // Before bugfix: in BOTH process instances the inactive execution was removed (result was 0)
    assertThat(getInactiveExecutionsInActivityId("inclusiveGw")).hasSize(1);

    assertThat(taskService.createTaskQuery().taskName("After Merge").count()).isEqualTo(1L);

    // Finish both processes

    List<Task> tasks = taskService.createTaskQuery().list();
    while (tasks.size() > 0) {
      for (Task task : tasks) {
        taskService.complete(task.getId());
      }
      tasks = taskService.createTaskQuery().list();
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0L);

  }

  protected List<Execution> getInactiveExecutionsInActivityId(String activityId) {
    List<Execution> result = new ArrayList<Execution>();
    List<Execution> executions = runtimeService.createExecutionQuery().list();
    Iterator<Execution> iterator = executions.iterator();
    while (iterator.hasNext()) {
      Execution execution = iterator.next();
      if (execution.getActivityId() != null
          && execution.getActivityId().equals(activityId)
          && !((ExecutionEntity) execution).isActive()) {
        result.add(execution);
      }
    }
    return result;
  }

  /*
   * @Deployment public void testAsyncBehavior() { for (int i = 0; i < 100; i++) { ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("async"); } assertThat(200,
   * managementService.createJobQuery().count()); waitForJobExecutorToProcessAllJobs(120000, 5000); assertThat(managementService.createJobQuery().count()).isEqualTo(0); assertThat(0,
   * runtimeService.createProcessInstanceQuery().count()); }
   */

  // /* This test case is related to ACT-1877 */
  //
  // @Deployment(resources={"org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testWithSignalBoundaryEvent.bpmn20.xml"})
  // public void testJoinAfterBoudarySignalEvent() {
  //
  //
  // ProcessInstance processInstanceId =
  // runtimeService.startProcessInstanceByKey("InclusiveGatewayAfterSignalBoundaryEvent");
  //
  // /// Gets the execution waiting for a message notification*/
  // String subcriptedExecutionId =
  // runtimeService.createExecutionQuery().processInstanceId(processInstanceId.getId()).messageEventSubscriptionName("MyMessage").singleResult().getId();
  //
  // /*Notify message received: this makes one execution to go on*/
  // runtimeService.messageEventReceived("MyMessage", subcriptedExecutionId);
  //
  // /*The other execution goes on*/
  // Task userTask =
  // taskService.createTaskQuery().processInstanceId(processInstanceId.getId()).singleResult();
  // assertThat("There's still an active execution waiting in the first task",
  // "usertask1",userTask.getTaskDefinitionKey());
  //
  // taskService.complete( userTask.getId());
  //
  // /*The two executions become one because of Inclusive Gateway*/
  // /*The process ends*/
  // userTask =
  // taskService.createTaskQuery().processInstanceId(processInstanceId.getId()).singleResult();
  // assertThat("Only when both executions reach the inclusive gateway, flow arrives to the last user task",
  // "usertask2",userTask.getTaskDefinitionKey());
  // taskService.complete(userTask.getId());
  //
  // long nExecutions =
  // runtimeService.createExecutionQuery().processInstanceId(processInstanceId.getId()).count();
  // assertThat(nExecutions).isEqualTo(0);
  //
  // }
}
