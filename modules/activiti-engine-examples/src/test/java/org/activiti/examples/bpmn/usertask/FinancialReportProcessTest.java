package org.activiti.examples.bpmn.usertask;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class FinancialReportProcessTest extends ActivitiTestCase {
  
  @Before
  public void setUp() throws Exception {
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("kermit"));
    
    identityService.saveGroup(identityService.newGroup("accountancy"));
    identityService.saveGroup(identityService.newGroup("management"));
    
    identityService.createMembership("fozzie", "accountancy");
    identityService.createMembership("kermit", "management");
  }
  
  @After
  public void tearDown() throws Exception {
    identityService.deleteUser("fozzie");
    identityService.deleteUser("kermit");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
  }
  
  @Test
  @ProcessDeclared(resources={"FinancialReportProcess.bpmn20.xml"})
  public void testProcess() {
    
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
  }

}
