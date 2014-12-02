package org.activiti.examples.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Christian Stettler
 */
public class MethodExpressionServiceTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("okReturningService", new OkReturningService());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);

    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
  }

  @Deployment
  public void testSetServiceResultToProcessVariablesWithSkipExpression() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("okReturningService", new OkReturningService());
    variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", false);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables);

    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
    
    Map<String,Object> variables2 = new HashMap<String, Object>();
    variables2.put("okReturningService", new OkReturningService());
    variables2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables2.put("skip", true);

    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables2);

    assertEquals(null, runtimeService.getVariable(pi2.getId(), "result"));
    
  }  
}