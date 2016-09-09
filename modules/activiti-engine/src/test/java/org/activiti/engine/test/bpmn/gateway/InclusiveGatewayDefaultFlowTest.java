package org.activiti.engine.test.bpmn.gateway;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;

public class InclusiveGatewayDefaultFlowTest extends PluggableActivitiTestCase {

  private static final String PROCESS_DEFINITION_KEY = "InclusiveGatewayDefaultFlowTest";

  private String deploymentId;

  protected void setUp() throws Exception {
    super.setUp();
    deploymentId = repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/bpmn/gateway/InclusiveGatewayTest.defaultFlowTest.bpmn20.xml")
          .deploy().getId();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentId, true);
    super.tearDown();
  }
  
  public void testDefaultFlowOnly() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("usertask1").singleResult();
    assertNotNull(execution);
    assertEquals("usertask1", execution.getActivityId());
  }
  
  public void testCompatibleConditionFlow() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "true");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
    
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("usertask2").singleResult();
    assertNotNull(execution);
    assertEquals("usertask2", execution.getActivityId());
  }
}
