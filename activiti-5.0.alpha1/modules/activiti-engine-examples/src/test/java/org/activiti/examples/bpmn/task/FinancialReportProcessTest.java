package org.activiti.examples.bpmn.task;

import java.util.Collections;
import java.util.List;

import org.activiti.Deployment;
import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.test.ActivitiTestCase;


public class FinancialReportProcessTest extends ActivitiTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("kermit"));
    
    identityService.saveGroup(identityService.newGroup("accountancy"));
    identityService.saveGroup(identityService.newGroup("management"));
    
    identityService.createMembership("fozzie", "accountancy");
    identityService.createMembership("kermit", "management");
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteUser("fozzie");
    identityService.deleteUser("kermit");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    super.tearDown();
  }
  
  public void testProcess() {
    Deployment deployment = processService.newDeployment()
      .addClasspathResource("org/activiti/examples/bpmn/task/financial_report_process.bpmn20.xml")
      .deploy();
    registerDeployment(deployment.getId());
    
    ProcessInstance processInstance = processService.startProcessInstanceByKey("financialReport");
    
    List<Task> tasks = taskService.findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Write monthly financial report", task.getName());
    
    taskService.claim(task.getId(), "fozzie");
    tasks = taskService.findAssignedTasks("fozzie");
    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    tasks = taskService.findUnassignedTasks("fozzie");
    assertEquals(0, tasks.size());
    tasks = taskService.findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("Verify monthly financial report", tasks.get(0).getName());
    taskService.complete(tasks.get(0).getId());
    
    assertProcessInstanceEnded(processInstance.getId());
    deleteDeploymentsCascade(Collections.singletonList(deployment.getId()));
  }

}
