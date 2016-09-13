package org.activiti.examples.bpmn.usertask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class SkipExpressionUserTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void test() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("skipExpressionUserTask");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertEquals(0, taskService.createTaskQuery().list().size());

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables2.put("skip", false);
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("skipExpressionUserTask", variables2);
    List<Task> tasks2 = taskService.createTaskQuery().list();
    assertEquals(1, tasks2.size());
    taskService.complete(tasks2.get(0).getId());
    assertEquals(0, taskService.createTaskQuery().list().size());

    Map<String, Object> variables3 = new HashMap<String, Object>();
    variables3.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables3.put("skip", true);
    ProcessInstance pi3 = runtimeService.startProcessInstanceByKey("skipExpressionUserTask", variables3);
    List<Task> tasks3 = taskService.createTaskQuery().list();
    assertEquals(0, tasks3.size());
  }
  
  @Deployment
  public void testWithCandidateGroups() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    vars.put("skip", true);
    runtimeService.startProcessInstanceByKey("skipExpressionUserTask", vars);
    assertEquals(0, taskService.createTaskQuery().list().size());
  }

  @Deployment
  public void testSkipMultipleTasks()
  {
    Map<String,Object> variables = new HashMap<String,Object>();
    variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables.put("skip1", true);
    variables.put("skip2", true);
    variables.put("skip3", false);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("skipExpressionUserTask-testSkipMultipleTasks", variables);
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    assertEquals("Task3", tasks.get(0).getName());
  }
}
