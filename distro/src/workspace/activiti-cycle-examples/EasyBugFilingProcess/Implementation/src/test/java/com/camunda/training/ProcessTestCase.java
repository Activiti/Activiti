package com.camunda.training;

import java.util.HashMap;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class ProcessTestCase extends ActivitiTestCase {

  public void assertInActivity(String processInstanceId, String activityId) {
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
    assertTrue("Current activities (" + activeActivityIds + ") does not contain " + activityId, activeActivityIds.contains(activityId));
  }

  @Deployment(resources = "EasyBugFilingProcess.bpmn20.xml")
  public void testHappyPath() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("email", "bernd.ruecker@camunda.com");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("EasyBugFilingProcess", variables);
    String id = processInstance.getId();
    System.out.println("Started process instance id " + id);
    assertInActivity(id, "Handle_bug");
    
    Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    variables = new HashMap<String, Object>();
    variables.put("isBug", Boolean.TRUE);
    taskService.complete(task.getId(), variables);
    
    assertProcessEnded(id);

    HistoricProcessInstance historicProcessInstance = historicDataService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult();
    assertNotNull(historicProcessInstance);

    System.out.println("Finished, took " + historicProcessInstance.getDurationInMillis() + " millis");
  }
}