package org.activiti.examples.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Christian Stettler
 */
public class MethodExpressionServiceTaskTest extends ActivitiInternalTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("okReturningService", new OkReturningService());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);

    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
  }

}