package org.activiti.examples.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Christian Stettler
 */
public class ExpressionServiceTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("bean", new ValueBean("ok"));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);
    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
  }

  @Deployment
  public void testSetServiceResultToProcessVariablesWithSkipExpression() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("bean", new ValueBean("ok"));
    variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables.put("skip", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables);
    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
    
    Map<String,Object> variables2 = new HashMap<String, Object>();
    variables2.put("bean", new ValueBean("ok"));
    variables2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables2.put("skip", true);
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables2);
    assertEquals(null, runtimeService.getVariable(pi2.getId(), "result"));
    
    Map<String,Object> variables3 = new HashMap<String, Object>();
    variables3.put("bean", new ValueBean("ok"));
    variables3.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    ProcessInstance pi3 = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables2);
    assertEquals(null, runtimeService.getVariable(pi3.getId(), "result"));
  }
  
  @Deployment
  public void testBackwardsCompatibleExpression() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("var", "---");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("BackwardsCompatibleExpressionProcess", variables);
    assertEquals("...---...", runtimeService.getVariable(pi.getId(), "result"));
  }
}
