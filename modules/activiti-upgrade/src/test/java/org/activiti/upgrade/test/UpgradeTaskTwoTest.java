package org.activiti.upgrade.test;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.task.Task;
import org.activiti.upgrade.UpgradeTestCase;


public class UpgradeTaskTwoTest extends UpgradeTestCase {
  
  public static void main(String[] args) {
    runBeforeAndAfterInDevelopmentMode(new UpgradeTaskOneTest());
  }

  public void runInTheOldVersion() {
    processEngine.getRepositoryService()
      .createDeployment()
      .name("simpleTaskProcess")
      .addClasspathResource("org/activiti/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml")
      .deploy();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("instrument", "trumpet");
    variables.put("player", "gonzo");
    runtimeService.startProcessInstanceByKey("taskWithExecutionVariablesProcess", variables);
  }

  public void testTaskWithExecutionVariables() {
    Task task = taskService
      .createTaskQuery()
      .taskName("taskWithExecutionVariables")
      .singleResult();

    String taskId = task.getId();

    assertNotNull(task);

    String processInstanceId = task.getProcessInstanceId();
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));

    expectedVariables.put("instrument", "trumpet");
    expectedVariables.put("player", "gonzo");

    assertEquals(expectedVariables, taskService.getVariables(taskId));

    taskService.complete(taskId);

    assertEquals(0, runtimeService
      .createExecutionQuery()
      .processInstanceId(processInstanceId)
      .list()
      .size());
  }
}
