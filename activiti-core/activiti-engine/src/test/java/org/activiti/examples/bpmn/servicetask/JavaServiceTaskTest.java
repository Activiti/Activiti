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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiClassLoadingException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 */
public class JavaServiceTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testJavaServiceDelegation() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("javaServiceDelegation", singletonMap("input", "Activiti BPM Engine"));
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();
    assertThat(runtimeService.getVariable(execution.getId(), "input")).isEqualTo("ACTIVITI BPM ENGINE");
  }

  @Deployment
  public void testFieldInjection() {
    // Process contains 2 service-tasks using field-injection. One should
    // use the exposed setter,
    // the other is using the private field.
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("fieldInjection");
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();

    assertThat(runtimeService.getVariable(execution.getId(), "var")).isEqualTo("HELLO WORLD");
    assertThat(runtimeService.getVariable(execution.getId(), "setterVar")).isEqualTo("HELLO SETTER");
  }

  @Deployment
  public void testExpressionFieldInjection() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("name", "kermit");
    vars.put("gender", "male");
    vars.put("genderBean", new GenderBean());

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("expressionFieldInjection", vars);
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();

    assertThat(runtimeService.getVariable(execution.getId(), "var2")).isEqualTo("timrek .rM olleH");
    assertThat(runtimeService.getVariable(execution.getId(), "var1")).isEqualTo("elam :si redneg ruoY");
  }

  @Deployment
  public void testExpressionFieldInjectionWithSkipExpression() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("name", "kermit");
    vars.put("gender", "male");
    vars.put("genderBean", new GenderBean());
    vars.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    vars.put("skip", false);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("expressionFieldInjectionWithSkipExpression", vars);
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();

    assertThat(runtimeService.getVariable(execution.getId(), "var2")).isEqualTo("timrek .rM olleH");
    assertThat(runtimeService.getVariable(execution.getId(), "var1")).isEqualTo("elam :si redneg ruoY");

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("name", "kermit");
    vars2.put("gender", "male");
    vars2.put("genderBean", new GenderBean());
    vars2.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
    vars2.put("skip", true);

    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("expressionFieldInjectionWithSkipExpression", vars2);
    Execution execution2 = runtimeService.createExecutionQuery().processInstanceId(pi2.getId()).activityId("waitState").singleResult();

    assertThat(execution2).isEqualTo(null);
  }

  @Deployment
  public void testUnexistingClassDelegation() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("unexistingClassDelegation"))
      .withMessageContaining("couldn't instantiate class org.activiti.BogusClass")
      .withCauseInstanceOf(ActivitiClassLoadingException.class);
  }

  public void testIllegalUseOfResultVariableName() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/examples/bpmn/servicetask/JavaServiceTaskTest.testIllegalUseOfResultVariableName.bpmn20.xml")
        .deploy())
      .withMessageContaining("resultVariable");
  }

  @Deployment
  public void testExceptionHandling() {

    // If variable value is != 'throw-exception', process goes
    // through service task and ends immediately
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", "no-exception");
    runtimeService.startProcessInstanceByKey("exceptionHandling", vars);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // If variable value == 'throw-exception', process executes
    // service task, which generates and catches exception,
    // and takes sequence flow to user task
    vars.put("var", "throw-exception");
    runtimeService.startProcessInstanceByKey("exceptionHandling", vars);
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Fix Exception");
  }

  @Deployment
  public void testGetBusinessKeyFromDelegateExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("businessKeyProcess", "1234567890");
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("businessKeyProcess").count()).isEqualTo(1);

    // Check if business-key was available from the process
    String key = (String) runtimeService.getVariable(processInstance.getId(), "businessKeySetOnExecution");
    assertThat(key).isNotNull();
    assertThat(key).isEqualTo("1234567890");
  }

}
