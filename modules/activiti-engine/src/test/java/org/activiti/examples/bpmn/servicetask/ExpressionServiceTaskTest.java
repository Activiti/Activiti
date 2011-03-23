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

//  @Deployment
//  public void testSetServiceResultToProcessVariables() {
//    Map<String,Object> variables = new HashMap<String, Object>();
//    variables.put("bean", new ValueBean("ok"));
//    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);
//    assertEquals("ok", runtimeService.getVariable(pi.getId(), "result"));
//  }

  @Deployment
  public void testBackwardsCompatibleExpression() {
    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put("var", "---");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("BackwardsCompatibleExpressionProcess", variables);
    assertEquals("...---...", runtimeService.getVariable(pi.getId(), "result"));
  }
}
