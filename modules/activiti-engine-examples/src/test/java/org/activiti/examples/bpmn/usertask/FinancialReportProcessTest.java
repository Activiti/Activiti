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
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("fozzie"));
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("kermit"));
    
    processEngineBuilder.getIdentityService().saveGroup(processEngineBuilder.getIdentityService().newGroup("accountancy"));
    processEngineBuilder.getIdentityService().saveGroup(processEngineBuilder.getIdentityService().newGroup("management"));
    
    processEngineBuilder.getIdentityService().createMembership("fozzie", "accountancy");
    processEngineBuilder.getIdentityService().createMembership("kermit", "management");
  }
  
  @After
  public void tearDown() throws Exception {
    processEngineBuilder.getIdentityService().deleteUser("fozzie");
    processEngineBuilder.getIdentityService().deleteUser("kermit");
    processEngineBuilder.getIdentityService().deleteGroup("accountancy");
    processEngineBuilder.getIdentityService().deleteGroup("management");
  }
  
  @Test
  @ProcessDeclared(resources={"FinancialReportProcess.bpmn20.xml"})
  public void testProcess() {
    
    ProcessInstance processInstance = processEngineBuilder.getProcessService().startProcessInstanceByKey("financialReport");
    processEngineBuilder.expectProcessEnds(processInstance.getId());
    
    List<Task> tasks = processEngineBuilder.getTaskService().findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Write monthly financial report", task.getName());
    
    processEngineBuilder.getTaskService().claim(task.getId(), "fozzie");
    tasks = processEngineBuilder.getTaskService().findAssignedTasks("fozzie");
    assertEquals(1, tasks.size());
    processEngineBuilder.getTaskService().complete(task.getId());

    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("fozzie");
    assertEquals(0, tasks.size());
    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("Verify monthly financial report", tasks.get(0).getName());
    processEngineBuilder.getTaskService().complete(tasks.get(0).getId());
  }

}
