package org.activiti.examples.bpmn.usertask;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class FinancialReportProcessTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Before
  public void setUp() throws Exception {
    deployer.getIdentityService().saveUser(deployer.getIdentityService().newUser("fozzie"));
    deployer.getIdentityService().saveUser(deployer.getIdentityService().newUser("kermit"));
    
    deployer.getIdentityService().saveGroup(deployer.getIdentityService().newGroup("accountancy"));
    deployer.getIdentityService().saveGroup(deployer.getIdentityService().newGroup("management"));
    
    deployer.getIdentityService().createMembership("fozzie", "accountancy");
    deployer.getIdentityService().createMembership("kermit", "management");
  }
  
  @After
  public void tearDown() throws Exception {
    deployer.getIdentityService().deleteUser("fozzie");
    deployer.getIdentityService().deleteUser("kermit");
    deployer.getIdentityService().deleteGroup("accountancy");
    deployer.getIdentityService().deleteGroup("management");
  }
  
  @Test
  @Deployment(resources={"FinancialReportProcess.bpmn20.xml"})
  public void testProcess() {
    
    ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceByKey("financialReport");
    
    List<Task> tasks = deployer.getTaskService().findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Write monthly financial report", task.getName());
    
    deployer.getTaskService().claim(task.getId(), "fozzie");
    tasks = deployer.getTaskService().findAssignedTasks("fozzie");
    assertEquals(1, tasks.size());
    deployer.getTaskService().complete(task.getId());

    tasks = deployer.getTaskService().findUnassignedTasks("fozzie");
    assertEquals(0, tasks.size());
    tasks = deployer.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("Verify monthly financial report", tasks.get(0).getName());
    deployer.getTaskService().complete(tasks.get(0).getId());

    deployer.assertProcessEnded(processInstance.getId());
  }

}
