package org.activiti.examples.bpmn.usertask;

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class FinancialReportProcessTest extends PluggableActivitiTestCase {


  private static final String KERMIT = "kermit";
  private static final List<String> KERMITSGROUPS = Arrays.asList("management");

  private static final String FOZZIE = "fozzie";
  private static final List<String> FOZZIESGROUPS = Arrays.asList("accountancy");


  @Deployment(resources = { "org/activiti/examples/bpmn/usertask/FinancialReportProcess.bpmn20.xml" })
  public void testProcess() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("financialReport");

    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("fozzie",FOZZIESGROUPS).list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Write monthly financial report", task.getName());

    taskService.claim(task.getId(), FOZZIE);
    tasks = taskService.createTaskQuery().taskAssignee(FOZZIE).list();

    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().taskCandidateUser(FOZZIE,FOZZIESGROUPS).list();
    assertEquals(0, tasks.size());
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertEquals(1, tasks.size());
    assertEquals("Verify monthly financial report", tasks.get(0).getName());
    taskService.complete(tasks.get(0).getId());

    assertProcessEnded(processInstance.getId());
  }

}
