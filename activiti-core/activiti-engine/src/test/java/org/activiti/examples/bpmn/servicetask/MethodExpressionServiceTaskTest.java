package org.activiti.examples.bpmn.servicetask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**

 */
public class MethodExpressionServiceTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("okReturningService", new OkReturningService());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);

    assertThat(runtimeService.getVariable(pi.getId(), "result")).isEqualTo("ok");
  }

  @Deployment
  public void testSetServiceResultToProcessVariablesWithSkipExpression() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("okReturningService", new OkReturningService());
    variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", false);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables);

    assertThat(runtimeService.getVariable(pi.getId(), "result")).isEqualTo("ok");

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("okReturningService", new OkReturningService());
    variables2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables2.put("skip", true);

    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables2);

    assertThat(runtimeService.getVariable(pi2.getId(), "result")).isEqualTo(null);

  }
}
