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
package org.activiti.engine.test.api.v6;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * These are the first tests ever written for Activiti 6.
 * Keeping them here for nostalgic reasons.
 *
 * @author Joram Barrez
 */
public class Activiti6Test extends PluggableActivitiTestCase {

  @Test
  public void testSimplestProcessPossible() {
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/v6/Activiti6Test.simplestProcessPossible.bpmn20.xml").deploy();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd");
    assertNotNull(processInstance);
    assertTrue(processInstance.isEnded());

    // Cleanup
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("The famous task", task.getName());
    assertEquals("kermit", task.getAssignee());

    taskService.complete(task.getId());
  }

  @Test
  @org.activiti.engine.test.Deployment(resources = "org/activiti/engine/test/api/v6/Activiti6Test.testOneTaskProcess.bpmn20.xml")
  public void testOneTaskProcessCleanupInMiddleOfProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("The famous task", task.getName());
    assertEquals("kermit", task.getAssignee());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testSimpleParallelGateway() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleParallelGateway");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("simpleParallelGateway").orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("Task a", tasks.get(0).getName());
    assertEquals("Task b", tasks.get(1).getName());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testSimpleNestedParallelGateway() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleParallelGateway");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("simpleParallelGateway").orderByTaskName().asc().list();
    assertEquals(4, tasks.size());
    assertEquals("Task a", tasks.get(0).getName());
    assertEquals("Task b1", tasks.get(1).getName());
    assertEquals("Task b2", tasks.get(2).getName());
    assertEquals("Task c", tasks.get(3).getName());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  /*
   * This fails on Activiti 5
   */
  @Test
  @org.activiti.engine.test.Deployment
  public void testLongServiceTaskLoop() {
    int maxCount = 3210; // You can make this as big as you want (as long as
                         // it still fits within transaction timeouts). Go
                         // on, try it!
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("counter", new Integer(0));
    vars.put("maxCount", maxCount);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testLongServiceTaskLoop", vars);
    assertNotNull(processInstance);
    assertTrue(processInstance.isEnded());

    assertEquals(maxCount, CountingServiceTaskTestDelegate.CALL_COUNT.get());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertEquals(maxCount, historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId()).activityId("serviceTask").count());
    }
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testScriptTask() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("a", 1);
    variableMap.put("b", 2);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variableMap);
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    Number sumVariable = (Number) runtimeService.getVariable(processInstance.getId(), "sum");
    assertEquals(3, sumVariable.intValue());

    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).onlyChildExecutions().singleResult();
    assertNotNull(execution);

    runtimeService.trigger(execution.getId());

    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testSimpleTimerBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleBoundaryTimer");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    Job job = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Task after timer", task.getName());

    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testSimpleTimerBoundaryEventTimerDoesNotFire() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleBoundaryTimer");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    assertEquals(1, managementService.createTimerJobQuery().count());

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("The famous task", task.getName());
    taskService.complete(task.getId());

    assertEquals(0, managementService.createTimerJobQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testSimpleNonInterruptingTimerBoundaryEvent() {

    // First test: first the task associated with the parent execution, then
    // the one with the child
    // (see the task name ordering in the query to get that specific order)

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleBoundaryTimer");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    Job job = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());

    // Completing them both should complete the process instance
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertEquals(0, runtimeService.createExecutionQuery().count());

    // Second test: complete tasks: first task associated with child
    // execution, then parent execution (easier case)
    processInstance = runtimeService.startProcessInstanceByKey("simpleBoundaryTimer");

    job = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());

    tasks = taskService.createTaskQuery().orderByTaskName().desc().list(); // Not the desc() here: Task B, Task A will be the result (task b being associated with the child execution)
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testConditionsWithoutExclusiveGateway() {

    // 3 conditions are true for input = 2
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testConditions", CollectionUtil.singletonMap("input", 2));
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("B", tasks.get(1).getName());
    assertEquals("C", tasks.get(2).getName());

    for (Task t : tasks) {
      taskService.complete(t.getId());
    }

    // 2 conditions are true for input = 20
    processInstance = runtimeService.startProcessInstanceByKey("testConditions", CollectionUtil.singletonMap("input", 20));
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("B", tasks.get(0).getName());
    assertEquals("C", tasks.get(1).getName());

    for (Task t : tasks) {
      taskService.complete(t.getId());
    }

    // 1 condition is true for input = 200
    processInstance = runtimeService.startProcessInstanceByKey("testConditions", CollectionUtil.singletonMap("input", 200));
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(1, tasks.size());
    assertEquals("C", tasks.get(0).getName());

    for (Task t : tasks) {
      taskService.complete(t.getId());
    }
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testNonInterruptingMoreComplex() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingTimer");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("B", tasks.get(1).getName());

    // Triggering the timers cancels B, but A is not interrupted
    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertEquals(2, jobs.size());
    for (Job job : jobs) {
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
    }

    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(5, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("C", tasks.get(1).getName());
    assertEquals("D", tasks.get(2).getName());
    assertEquals("E", tasks.get(3).getName());
    assertEquals("F", tasks.get(4).getName());

    // Firing timer shouldn't cancel anything, but create new task
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());
    managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(0).getId());

    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(6, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("C", tasks.get(1).getName());
    assertEquals("D", tasks.get(2).getName());
    assertEquals("E", tasks.get(3).getName());
    assertEquals("F", tasks.get(4).getName());
    assertEquals("G", tasks.get(5).getName());

    // Completing all tasks in this order should give the engine a bit
    // exercise (parent executions first)
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Test
  @org.activiti.engine.test.Deployment
  public void testNonInterruptingMoreComplex2() {

    // Use case 1: no timers fire
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingWithInclusiveMerge");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("B", tasks.get(1).getName());
    assertEquals(2, managementService.createTimerJobQuery().count());

    // Completing A
    taskService.complete(tasks.get(0).getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(1, tasks.size());
    assertEquals("B", tasks.get(0).getName());
    assertEquals(1, managementService.createTimerJobQuery().count());

    // Completing B should end the process
    taskService.complete(tasks.get(0).getId());
    assertEquals(0, managementService.createTimerJobQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());

    // Use case 2: The non interrupting timer on B fires
    processInstance = runtimeService.startProcessInstanceByKey("nonInterruptingWithInclusiveMerge");
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("B", tasks.get(1).getName());
    assertEquals(2, managementService.createTimerJobQuery().count());

    // Completing B
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(1, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals(1, managementService.createTimerJobQuery().count());

    // Firing the timer should activate E and F too
    String jobId = managementService.createTimerJobQuery().singleResult().getId();
    managementService.moveTimerToExecutableJob(jobId);
    managementService.executeJob(jobId);
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("C", tasks.get(1).getName());
    assertEquals("D", tasks.get(2).getName());

    // Firing the timer on D
    assertEquals(1, managementService.createTimerJobQuery().count());
    jobId = managementService.createTimerJobQuery().singleResult().getId();
    managementService.moveTimerToExecutableJob(jobId);
    managementService.executeJob(jobId);
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(4, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("C", tasks.get(1).getName());
    assertEquals("D", tasks.get(2).getName());
    assertEquals("G", tasks.get(3).getName());

    // Completing C, D, A and G in that order to give the engine a bit of exercise
    taskService.complete(taskService.createTaskQuery().taskName("C").singleResult().getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("D", tasks.get(1).getName());
    assertEquals("G", tasks.get(2).getName());

    taskService.complete(taskService.createTaskQuery().taskName("D").singleResult().getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("G", tasks.get(1).getName());

    taskService.complete(taskService.createTaskQuery().taskName("A").singleResult().getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(1, tasks.size());
    assertEquals("G", tasks.get(0).getName());

    taskService.complete(taskService.createTaskQuery().taskName("G").singleResult().getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());

  }

  /**
   * Based on the process and use cases described in http://www.bp-3.com/blogs/2013/09/joins-and-ibm-bpm-diving-deeper/
   */
  @Test
  @org.activiti.engine.test.Deployment(resources = "org/activiti/engine/test/api/v6/Activiti6Test.testInclusiveTrickyMerge.bpmn20.xml")
  public void testInclusiveTrickyMergeEasy() {

    // Use case 1 (easy):
    // "When C completes, depending on the data, we can immediately issue E no matter what the status is of A or B."
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("trickyInclusiveMerge");
    assertNotNull(processInstance);
    assertFalse(processInstance.isEnded());
    assertEquals(3, taskService.createTaskQuery().count());

    Task taskC = taskService.createTaskQuery().taskName("C").singleResult();
    taskService.complete(taskC.getId());
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("B", tasks.get(1).getName());
    assertEquals("E", tasks.get(2).getName());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("D", tasks.get(0).getName());
    assertEquals("E", tasks.get(1).getName());

    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  /**
   * Based on the process and use cases described in http://www.bp-3.com/blogs/2013/09/joins-and-ibm-bpm-diving-deeper/
   */
  @Test
  @org.activiti.engine.test.Deployment(resources = "org/activiti/engine/test/api/v6/Activiti6Test.testInclusiveTrickyMerge.bpmn20.xml")
  public void testInclusiveTrickyMergeDifficult() {

    // Use case 2 (tricky):
    // "If A and B are complete and C routes to E, D will be issued in Parallel to E"
    // It's tricky cause the inclusive gateway is not visited directly.
    // Instead, it's done by the InactivatedActivityBehavior

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("trickyInclusiveMerge");
    assertEquals(3, taskService.createTaskQuery().count());

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("A", tasks.get(0).getName());
    assertEquals("B", tasks.get(1).getName());
    assertEquals("C", tasks.get(2).getName());
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // C should still be open
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(1, tasks.size());
    assertEquals("C", tasks.get(0).getName());

    // If C is now completed, the inclusive gateway should also be completed
    // and D and E should be open tasks
    taskService.complete(tasks.get(0).getId());
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("D", tasks.get(0).getName());
    assertEquals("E", tasks.get(1).getName());

    // Completing them should just end the process instance
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  /**
   * Simple test that checks if all databases have correcly added the process definition tag.
   */
  @Test
  @org.activiti.engine.test.Deployment(resources = "org/activiti/engine/test/api/v6/Activiti6Test.testOneTaskProcess.bpmn20.xml")
  public void testProcessDefinitionTagCreated() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNull(((ProcessDefinitionEntity) processDefinition).getEngineVersion());
  }

}
