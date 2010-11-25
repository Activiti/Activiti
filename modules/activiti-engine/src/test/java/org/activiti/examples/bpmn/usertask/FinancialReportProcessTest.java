package org.activiti.examples.bpmn.usertask;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class FinancialReportProcessTest extends PluggableActivitiTestCase {
  
  public void setUp() throws Exception {
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("kermit"));
    
    identityService.saveGroup(identityService.newGroup("accountancy"));
    identityService.saveGroup(identityService.newGroup("management"));
    
    identityService.createMembership("fozzie", "accountancy");
    identityService.createMembership("kermit", "management");
  }
  
  public void tearDown() throws Exception {
    identityService.deleteUser("fozzie");
    identityService.deleteUser("kermit");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
  }
  
  @Deployment(resources={
    "org/activiti/examples/bpmn/usertask/FinancialReportProcess.bpmn20.xml"})
  public void testProcess() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("financialReport");
    
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Write monthly financial report", task.getName());
    
    taskService.claim(task.getId(), "fozzie");
    tasks = taskService
      .createTaskQuery()
      .taskAssignee("fozzie")
      .list();
    
    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
    assertEquals(0, tasks.size());
    tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    assertEquals("Verify monthly financial report", tasks.get(0).getName());
    taskService.complete(tasks.get(0).getId());

    assertProcessEnded(processInstance.getId());
  }

}
