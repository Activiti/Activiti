package org.activiti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.engine.test.ActivitiDmnRule;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

public class CallCenter1Test
{

  @Rule
  public ActivitiRule activitiRule = new ActivitiRule();

  @Rule
  public ActivitiDmnRule activitiDmnRule = new ActivitiDmnRule();

  @Test
  public void test() {

    // Resources.
    activitiRule.getRepositoryService().createDeployment().name("EmergencyCallCenter").addClasspathResource("org/activiti/test/Emergency_Call_Center.bpmn20.xml").deploy();
    activitiRule.getRepositoryService().createDeployment().name("EmergencyCallCenterDecisionaboutOrganizations").addClasspathResource("org/activiti/test/Emergency Call Center - Decision about Organizations.dmn").deploy();

    ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("EmergencyCallCenter");

    assertNotNull(processInstance);
    System.out.println("Emergency Call Center process started with process instance id [" + processInstance.getProcessInstanceId() + "] key [" + processInstance.getProcessDefinitionKey() + "]");

    Task task = activitiRule.getTaskService().createTaskQuery().singleResult();

    assertEquals("Collect details", task.getName());
    System.out.print("Processing Task [" + task.getName() + "]...");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("contactName", "Francesco");
    variables.put("contactDetails", "francesco.corti@alfresco.com");
    variables.put("description", "Something happened.");
    variables.put("type", "Fire");
    variables.put("isEmergencyConfirmed", "Yes");
    // TODO: Completing the task, test fails because DMN is not included in the deployemnt.
    //activitiRule.getTaskService().complete(task.getId(), variables);

    System.out.println("completed!");

    //... to be completed!
  }
}
