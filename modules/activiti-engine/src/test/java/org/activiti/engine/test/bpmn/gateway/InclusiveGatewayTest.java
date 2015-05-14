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
package org.activiti.engine.test.bpmn.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Tom Van Buskirk
 * @author Tijs Rademakers
 * @author Saeid Mirzaei
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
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGwDiverging", CollectionUtil.singletonMap("input", i));
      List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
      List<String> expectedNames = new ArrayList<String>();
      if (i == 1) {
        expectedNames.add(TASK1_NAME);
      }
      if (i <= 2) {
        expectedNames.add(TASK2_NAME);
      }
      expectedNames.add(TASK3_NAME);
      assertEquals(4 - i, tasks.size());
      for (Task task : tasks) {
        expectedNames.remove(task.getName());
      }
      assertEquals(0, expectedNames.size());
      runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
    }
  }

  @Deployment
  public void testMergingInclusiveGateway() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGwMerging", CollectionUtil.singletonMap("input", 2));
    assertEquals(1, taskService.createTaskQuery().count());
    
    runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
  }
  
  @Deployment
  public void testPartialMergingInclusiveGateway() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("partialInclusiveGwMerging", CollectionUtil.singletonMap("input", 2));
    Task partialTask = taskService.createTaskQuery().singleResult();
    assertEquals("partialTask", partialTask.getTaskDefinitionKey());
    
    taskService.complete(partialTask.getId());
    
    Task fullTask = taskService.createTaskQuery().singleResult();
    assertEquals("theTask", fullTask.getTaskDefinitionKey());
    
    runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
  }

  @Deployment
  public void testNoSequenceFlowSelected() {
    try {
      runtimeService.startProcessInstanceByKey("inclusiveGwNoSeqFlowSelected", CollectionUtil.singletonMap("input", 4));
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("No outgoing sequence flow of the inclusive gateway 'inclusiveGw' could be selected for continuing the process", e.getMessage());
    }
  }

  /**
   * Test for ACT-1216: When merging a concurrent execution the parent is not activated correctly
   */
  @Deployment
  public void testParentActivationOnNonJoiningEnd() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parentActivationOnNonJoiningEnd");
    
    List<Execution> executionsBefore = runtimeService.createExecutionQuery().list();
    assertEquals(3, executionsBefore.size());
    
    // start first round of tasks
    List<Task> firstTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    
    assertEquals(2, firstTasks.size());
    
    for (Task t: firstTasks) {
      taskService.complete(t.getId());
    }
    
    // start first round of tasks
    List<Task> secondTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    
    assertEquals(2, secondTasks.size());
    
    // complete one task
    Task task = secondTasks.get(0);
    taskService.complete(task.getId());
    
    // should have merged last child execution into parent
    List<Execution> executionsAfter = runtimeService.createExecutionQuery().list();
    assertEquals(1, executionsAfter.size());
    
    Execution execution = executionsAfter.get(0);
    
    // and should have one active activity
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(execution.getId());
    assertEquals(1, activeActivityIds.size());
    
    // Completing last task should finish the process instance
    
    Task lastTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(lastTask.getId());
    
    assertEquals(0l, runtimeService.createProcessInstanceQuery().active().count());
  }

  /**
   * Test for bug ACT-10: whitespaces/newlines in expressions lead to exceptions
   */
  @Deployment
  public void testWhitespaceInExpression() {
    // Starting a process instance will lead to an exception if whitespace are
    // incorrectly handled
    runtimeService.startProcessInstanceByKey("inclusiveWhiteSpaceInExpression", CollectionUtil.singletonMap("input", 1));
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testDivergingInclusiveGateway.bpmn20.xml" })
  public void testUnknownVariableInExpression() {
    // Instead of 'input' we're starting a process instance with the name
    // 'iinput' (ie. a typo)
    try {
      runtimeService.startProcessInstanceByKey("inclusiveGwDiverging", CollectionUtil.singletonMap("iinput", 1));
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Unknown property used in expression", e.getMessage());
    }
  }

  @Deployment
  public void testDecideBasedOnBeanProperty() {
    runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanProperty", CollectionUtil.singletonMap("order", new InclusiveGatewayTestOrder(150)));
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    Map<String, String> expectedNames = new HashMap<String, String>();
    expectedNames.put(BEAN_TASK2_NAME, BEAN_TASK2_NAME);
    expectedNames.put(BEAN_TASK3_NAME, BEAN_TASK3_NAME);
    for (Task task : tasks) {
      expectedNames.remove(task.getName());
    }
    assertEquals(0, expectedNames.size());
  }

  @Deployment
  public void testDecideBasedOnListOrArrayOfBeans() {
    List<InclusiveGatewayTestOrder> orders = new ArrayList<InclusiveGatewayTestOrder>();
    orders.add(new InclusiveGatewayTestOrder(50));
    orders.add(new InclusiveGatewayTestOrder(300));
    orders.add(new InclusiveGatewayTestOrder(175));

    try {
      runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orders));
      fail();
    } catch (ActivitiException e) {
      // expect an exception to be thrown here
    }

    orders.set(1, new InclusiveGatewayTestOrder(175));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orders));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals(BEAN_TASK3_NAME, task.getName());

    orders.set(1, new InclusiveGatewayTestOrder(125));
    pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orders));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());
    List<String> expectedNames = new ArrayList<String>();
    expectedNames.add(BEAN_TASK2_NAME);
    expectedNames.add(BEAN_TASK3_NAME);
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertEquals(0, expectedNames.size());

    // Arrays are usable in exactly the same way
    InclusiveGatewayTestOrder[] orderArray = orders.toArray(new InclusiveGatewayTestOrder[orders.size()]);
    orderArray[1].setPrice(10);
    pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orderArray));
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertNotNull(tasks);
    assertEquals(3, tasks.size());
    expectedNames.clear();
    expectedNames.add(BEAN_TASK1_NAME);
    expectedNames.add(BEAN_TASK2_NAME);
    expectedNames.add(BEAN_TASK3_NAME);
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertEquals(0, expectedNames.size());
  }

  @Deployment
  public void testDecideBasedOnBeanMethod() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanMethod",
            CollectionUtil.singletonMap("order", new InclusiveGatewayTestOrder(200)));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals(BEAN_TASK3_NAME, task.getName());

    pi = runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanMethod",
            CollectionUtil.singletonMap("order", new InclusiveGatewayTestOrder(125)));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());
    List<String> expectedNames = new ArrayList<String>();
    expectedNames.add(BEAN_TASK2_NAME);
    expectedNames.add(BEAN_TASK3_NAME);
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertEquals(0, expectedNames.size());

    try {
      runtimeService.startProcessInstanceByKey("inclusiveDecisionBasedOnBeanMethod", CollectionUtil.singletonMap("order", new InclusiveGatewayTestOrder(300)));
      fail();
    } catch (ActivitiException e) {
      // Should get an exception indicating that no path could be taken
    }

  }

  @Deployment
  public void testInvalidMethodExpression() {
    try {
      runtimeService.startProcessInstanceByKey("inclusiveInvalidMethodExpression", CollectionUtil.singletonMap("order", new InclusiveGatewayTestOrder(50)));
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Unknown method used in expression", e.getMessage());
    }
  }

  @Deployment
  public void testDefaultSequenceFlow() {
    // Input == 1 -> default is not selected, other 2 tasks are selected
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGwDefaultSequenceFlow", CollectionUtil.singletonMap("input", 1));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());
    Map<String, String> expectedNames = new HashMap<String, String>();
    expectedNames.put("Input is one", "Input is one");
    expectedNames.put("Input is three or one", "Input is three or one");
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertEquals(0, expectedNames.size());
    runtimeService.deleteProcessInstance(pi.getId(), null);

    // Input == 3 -> default is not selected, "one or three" is selected
    pi = runtimeService.startProcessInstanceByKey("inclusiveGwDefaultSequenceFlow", CollectionUtil.singletonMap("input", 3));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Input is three or one", task.getName());

    // Default input
    pi = runtimeService.startProcessInstanceByKey("inclusiveGwDefaultSequenceFlow", CollectionUtil.singletonMap("input", 5));
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Default input", task.getName());
  }

  @Deployment
  public void testNoIdOnSequenceFlow() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveNoIdOnSequenceFlow", CollectionUtil.singletonMap("input", 3));
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Input is more than one", task.getName());

    // Both should be enabled on 1
    pi = runtimeService.startProcessInstanceByKey("inclusiveNoIdOnSequenceFlow", CollectionUtil.singletonMap("input", 1));
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());
    Map<String, String> expectedNames = new HashMap<String, String>();
    expectedNames.put("Input is one", "Input is one");
    expectedNames.put("Input is more than one", "Input is more than one");
    for (Task t : tasks) {
      expectedNames.remove(t.getName());
    }
    assertEquals(0, expectedNames.size());
  }
  
  /** This test the isReachable() check thaty is done to check if 
   * upstream tokens can reach the inclusive gateway.
   * 
   * In case of loops, special care needs to be taken in the algorithm,
   * or else stackoverflows will happen very quickly.
   */
  @Deployment
  public void testLoop() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveTestLoop", 
            CollectionUtil.singletonMap("counter", 1));
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task C", task.getName());
    
    taskService.complete(task.getId());
    assertEquals(0, taskService.createTaskQuery().count());
    

    for (Execution execution : runtimeService.createExecutionQuery().list()) {
      System.out.println(execution.getActivityId());
    }
    
    assertEquals("Found executions: " + runtimeService.createExecutionQuery().list(), 0, runtimeService.createExecutionQuery().count());
    assertProcessEnded(pi.getId());
  }
  
  @Deployment	
  public void testJoinAfterSubprocesses() {
	  // Test case to test act-1204
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("a", 1);
		variableMap.put("b", 1);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("InclusiveGateway", variableMap);
		assertNotNull(processInstance.getId());
		
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
		assertEquals(2, taskService.createTaskQuery().count());

		taskService.complete(tasks.get(0).getId());
		assertEquals(1, taskService.createTaskQuery().count());

		taskService.complete(tasks.get(1).getId());

		Task task = taskService.createTaskQuery().taskAssignee("c").singleResult();
		assertNotNull(task);
		taskService.complete(task.getId());
		
		processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNull(processInstance);
		
		variableMap = new HashMap<String, Object>();
    variableMap.put("a", 1);
    variableMap.put("b", 2);
    processInstance = runtimeService.startProcessInstanceByKey("InclusiveGateway", variableMap);
    assertNotNull(processInstance.getId());
    
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, taskService.createTaskQuery().count());
    
    task = tasks.get(0);
    assertEquals("a", task.getAssignee());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().taskAssignee("c").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull(processInstance);
    
    variableMap = new HashMap<String, Object>();
    variableMap.put("a", 2);
    variableMap.put("b", 2);
    try {
      runtimeService.startProcessInstanceByKey("InclusiveGateway", variableMap);
      fail();
    } catch(ActivitiException e) {
      assertTrue(e.getMessage().contains("No outgoing sequence flow"));
    }
	}
  
  @Deployment(resources={"org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testJoinAfterCall.bpmn20.xml",
		                 "org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testJoinAfterCallSubProcess.bpmn20.xml"})	
  public void testJoinAfterCall() {
	  // Test case to test act-1026
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("InclusiveGatewayAfterCall");
		assertNotNull(processInstance.getId());
		assertEquals(3, taskService.createTaskQuery().count());
		
		// now complete task A and check number of remaining tasks. 
		// inclusive gateway should wait for the "Task B" and "Task C"
		Task taskA = taskService.createTaskQuery().taskName("Task A").singleResult();
		assertNotNull(taskA);
		taskService.complete(taskA.getId());
		assertEquals(2, taskService.createTaskQuery().count());
		
		// now complete task B and check number of remaining tasks
		// inclusive gateway should wait for "Task C"
		Task taskB = taskService.createTaskQuery().taskName("Task B").singleResult();
		assertNotNull(taskB);
		taskService.complete(taskB.getId());
		assertEquals(1, taskService.createTaskQuery().count());

		// now complete task C. Gateway activates and "Task C" remains
		Task taskC = taskService.createTaskQuery().taskName("Task C").singleResult();
		assertNotNull(taskC);
		taskService.complete(taskC.getId());
		assertEquals(1, taskService.createTaskQuery().count());
	
		// check that remaining task is in fact task D
		Task taskD = taskService.createTaskQuery().taskName("Task D").singleResult();
		assertNotNull(taskD);
		assertEquals("Task D", taskD.getName());
		taskService.complete(taskD.getId());
		
		processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNull(processInstance);
	}
  
  @Deployment
  public void testAsyncBehavior() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("async");
    waitForJobExecutorToProcessAllJobs(10000, 500);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
  
  @Deployment
  public void testDirectSequenceFlow() {
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("input", 1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwDirectSequenceFlow", varMap);
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("theTask1", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    varMap = new HashMap<String, Object>();
    varMap.put("input", 3);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwDirectSequenceFlow", varMap);
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    varMap = new HashMap<String, Object>();
    varMap.put("input", 0);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwDirectSequenceFlow", varMap);
    assertTrue(processInstance.isEnded());
  }
  
  @Deployment
  public void testSkipExpression() {
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    varMap.put("input", 10);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwSkipExpression", varMap);
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("theTask1", task.getTaskDefinitionKey());
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    varMap = new HashMap<String, Object>();
    varMap.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    varMap.put("input", 30);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwSkipExpression", varMap);
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(2, tasks.size());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    varMap = new HashMap<String, Object>();
    varMap.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    varMap.put("input", 3);
    processInstance = runtimeService.startProcessInstanceByKey("inclusiveGwSkipExpression", varMap);
    assertTrue(processInstance.isEnded());    
  }
  
  /*@Deployment
  public void testAsyncBehavior() {
    for (int i = 0; i < 100; i++) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("async");
    }
    assertEquals(200, managementService.createJobQuery().count());
    waitForJobExecutorToProcessAllJobs(120000, 5000);
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }*/

//  /* This test case is related to ACT-1877 */
//  
//  @Deployment(resources={"org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.testWithSignalBoundaryEvent.bpmn20.xml"})	
//  public void testJoinAfterBoudarySignalEvent() {
//		
//	
//		ProcessInstance processInstanceId = runtimeService.startProcessInstanceByKey("InclusiveGatewayAfterSignalBoundaryEvent");
//		
//		/// Gets the execution waiting for a message notification*/
//		String subcriptedExecutionId = runtimeService.createExecutionQuery().processInstanceId(processInstanceId.getId()).messageEventSubscriptionName("MyMessage").singleResult().getId();
//	
//		/*Notify message received: this makes one execution to go on*/
//		runtimeService.messageEventReceived("MyMessage", subcriptedExecutionId);
//	
//		/*The other execution goes on*/
//		Task userTask = taskService.createTaskQuery().processInstanceId(processInstanceId.getId()).singleResult();
//		assertEquals("There's still an active execution waiting in the first task",
//				"usertask1",userTask.getTaskDefinitionKey());
//		
//		taskService.complete( userTask.getId());
//		
//		/*The two executions become one because of Inclusive Gateway*/
//		/*The process ends*/
//		userTask = taskService.createTaskQuery().processInstanceId(processInstanceId.getId()).singleResult();
//		assertEquals("Only when both executions reach the inclusive gateway, flow arrives to the last user task",
//				"usertask2",userTask.getTaskDefinitionKey());
//		taskService.complete(userTask.getId());
//		
//		long nExecutions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId.getId()).count();
//		assertEquals(0, nExecutions);
//  
//  }
}
