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

package org.activiti.examples.bpmn.executionlistener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.Deployment;


public class CustomFlowExecutionListenerTest extends ResourceActivitiTestCase {

  public CustomFlowExecutionListenerTest() {
    super("org/activiti/examples/bpmn/executionlistener/custom.flow.parse.handler.activiti.cfg.xml");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/CustomFlowExecutionListenerTest.bpmn20.xml" })
  public void testScriptExecutionListener() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("customFlowBean", new CustomFlowBean());
    runtimeService.startProcessInstanceByKey("scriptExecutionListenerProcess", variableMap);
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().variableName("flow1_activiti_conditions").singleResult();
    assertThat(variable).isNotNull();
    assertThat(variable.getVariableName()).isEqualTo("flow1_activiti_conditions");
    @SuppressWarnings("unchecked")
    List<String> conditions = (List<String>) variable.getValue();
    assertThat(conditions).hasSize(2);
    assertThat(conditions.get(0)).isEqualTo("hello");
    assertThat(conditions.get(1)).isEqualTo("world");
  }
}
