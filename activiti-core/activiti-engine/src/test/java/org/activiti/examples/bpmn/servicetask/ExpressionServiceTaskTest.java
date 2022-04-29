/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.examples.bpmn.servicetask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


public class ExpressionServiceTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSetServiceResultToProcessVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("bean", new ValueBean("ok"));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariables", variables);
    assertThat(runtimeService.getVariable(pi.getId(), "result")).isEqualTo("ok");
  }

  @Deployment
  public void testSetServiceResultToProcessVariablesWithSkipExpression() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("bean", new ValueBean("ok"));
    variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables.put("skip", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables);
    assertThat(runtimeService.getVariable(pi.getId(), "result")).isEqualTo("ok");

    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("bean", new ValueBean("ok"));
    variables2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    variables2.put("skip", true);
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables2);
    assertThat(runtimeService.getVariable(pi2.getId(), "result")).isEqualTo(null);

    Map<String, Object> variables3 = new HashMap<String, Object>();
    variables3.put("bean", new ValueBean("okBean"));
    variables3.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", false);
    ProcessInstance pi3 = runtimeService.startProcessInstanceByKey("setServiceResultToProcessVariablesWithSkipExpression", variables3);
    assertThat(runtimeService.getVariable(pi3.getId(), "result")).isEqualTo("okBean");
  }

  @Deployment
  public void testBackwardsCompatibleExpression() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "---");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("BackwardsCompatibleExpressionProcess", variables);
    assertThat(runtimeService.getVariable(pi.getId(), "result")).isEqualTo("...---...");
  }
}
