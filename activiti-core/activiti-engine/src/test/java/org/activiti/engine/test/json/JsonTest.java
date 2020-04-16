/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonTest extends PluggableActivitiTestCase {

  public static final String MY_JSON_OBJ = "myJsonObj";
  public static final String BIG_JSON_OBJ = "bigJsonObj";

  protected ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Deployment
  public void testJsonObjectAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();

    ObjectNode varNode = objectMapper.createObjectNode();
    varNode.put("var", "myValue");
    vars.put(MY_JSON_OBJ, varNode);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJsonAvailableProcess", vars);

    // Check JSON has been parsed as expected
    ObjectNode value = (ObjectNode) runtimeService.getVariable(processInstance.getId(), MY_JSON_OBJ);
    assertThat(value).isNotNull();
    assertThat(value.get("var").asText()).isEqualTo("myValue");

    ObjectNode var2Node = objectMapper.createObjectNode();
    var2Node.put("var", "myValue");
    var2Node.put("var2", "myOtherValue");
    runtimeService.setVariable(processInstance.getId(), MY_JSON_OBJ, var2Node);

    // Check JSON has been updated as expected
    value = (ObjectNode) runtimeService.getVariable(processInstance.getId(), MY_JSON_OBJ);
    assertThat(value).isNotNull();
    assertThat(value.get("var").asText()).isEqualTo("myValue");
    assertThat(value.get("var2").asText()).isEqualTo("myOtherValue");

    Task task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();
    ObjectNode var3Node = objectMapper.createObjectNode();
    var3Node.put("var", "myValue");
    var3Node.put("var2", "myOtherValue");
    var3Node.put("var3", "myThirdValue");

    vars = new HashMap<String, Object>();
    vars.put(MY_JSON_OBJ, var3Node);
    vars.put(BIG_JSON_OBJ, createBigJsonObject());
    taskService.complete(task.getId(), vars);
    value = (ObjectNode) runtimeService.getVariable(processInstance.getId(), MY_JSON_OBJ);
    assertThat(value).isNotNull();
    assertThat(value.get("var").asText()).isEqualTo("myValue");
    assertThat(value.get("var2").asText()).isEqualTo("myOtherValue");
    assertThat(value.get("var3").asText()).isEqualTo("myThirdValue");

    value = (ObjectNode) runtimeService.getVariable(processInstance.getId(), BIG_JSON_OBJ);
    assertThat(value).isNotNull();
    assertThat(value.toString()).isEqualTo(createBigJsonObject().toString());

    task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("userTaskSuccess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
          .processInstanceId(processInstance.getProcessInstanceId()).orderByVariableName().asc().list();
      assertThat(historicVariableInstances).hasSize(2);

      assertThat(historicVariableInstances.get(0).getVariableName()).isEqualTo(BIG_JSON_OBJ);
      value = (ObjectNode) historicVariableInstances.get(0).getValue();
      assertThat(value).isNotNull();
      assertThat(value.toString()).isEqualTo(createBigJsonObject().toString());

      assertThat(historicVariableInstances.get(1).getVariableName()).isEqualTo(MY_JSON_OBJ);
      value = (ObjectNode) historicVariableInstances.get(1).getValue();
      assertThat(value).isNotNull();
      assertThat(value.get("var").asText()).isEqualTo("myValue");
      assertThat(value.get("var2").asText()).isEqualTo("myOtherValue");
      assertThat(value.get("var3").asText()).isEqualTo("myThirdValue");
    }

    // It should be possible do remove a json variable
    runtimeService.removeVariable(processInstance.getId(), MY_JSON_OBJ);
    assertThat(runtimeService.getVariable(processInstance.getId(), MY_JSON_OBJ)).isNull();

    // It should be possible do remove a longJson variable
    runtimeService.removeVariable(processInstance.getId(), BIG_JSON_OBJ);
    assertThat(runtimeService.getVariable(processInstance.getId(), BIG_JSON_OBJ)).isNull();
  }

  @Deployment
  public void testDirectJsonPropertyAccess() {
    Map<String, Object> vars = new HashMap<String, Object>();

    ObjectNode varNode = objectMapper.createObjectNode();
    varNode.put("var", "myValue");
    vars.put("myJsonObj", varNode);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJsonAvailableProcess", vars);

    // Check JSON has been parsed as expected
    ObjectNode value = (ObjectNode) runtimeService.getVariable(processInstance.getId(), "myJsonObj");
    assertThat(value).isNotNull();
    assertThat(value.get("var").asText()).isEqualTo("myValue");

    Task task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();
    ObjectNode var3Node = objectMapper.createObjectNode();
    var3Node.put("var", "myValue");
    var3Node.put("var2", "myOtherValue");
    var3Node.put("var3", "myThirdValue");

    vars.put("myJsonObj", var3Node);
    taskService.complete(task.getId(), vars);

    value = (ObjectNode) runtimeService.getVariable(processInstance.getId(), "myJsonObj");
    assertThat(value).isNotNull();
    assertThat(value.get("var").asText()).isEqualTo("myValue");
    assertThat(value.get("var2").asText()).isEqualTo("myOtherValue");
    assertThat(value.get("var3").asText()).isEqualTo("myThirdValue");

    task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("userTaskSuccess");
  }

  @Deployment
  public void testJsonArrayAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();

    ArrayNode varArray = objectMapper.createArrayNode();
    ObjectNode varNode = objectMapper.createObjectNode();
    varNode.put("var", "myValue");
    varArray.add(varNode);
    vars.put("myJsonArr", varArray);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJsonAvailableProcess", vars);

    // Check JSON has been parsed as expected
    ArrayNode value = (ArrayNode) runtimeService.getVariable(processInstance.getId(), "myJsonArr");
    assertThat(value).isNotNull();
    assertThat(value.get(0).get("var").asText()).isEqualTo("myValue");

    ArrayNode varArray2 = objectMapper.createArrayNode();
    varNode = objectMapper.createObjectNode();
    varNode.put("var", "myValue");
    varArray2.add(varNode);
    varNode = objectMapper.createObjectNode();
    varNode.put("var", "myOtherValue");
    varArray2.add(varNode);
    runtimeService.setVariable(processInstance.getId(), "myJsonArr", varArray2);

    // Check JSON has been updated as expected
    value = (ArrayNode) runtimeService.getVariable(processInstance.getId(), "myJsonArr");
    assertThat(value).isNotNull();
    assertThat(value.get(0).get("var").asText()).isEqualTo("myValue");
    assertThat(value.get(1).get("var").asText()).isEqualTo("myOtherValue");

    Task task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();
    ArrayNode varArray3 = objectMapper.createArrayNode();
    varNode = objectMapper.createObjectNode();
    varNode.put("var", "myValue");
    varArray3.add(varNode);
    varNode = objectMapper.createObjectNode();
    varNode.put("var", "myOtherValue");
    varArray3.add(varNode);
    varNode = objectMapper.createObjectNode();
    varNode.put("var", "myThirdValue");
    varArray3.add(varNode);
    vars = new HashMap<String, Object>();
    vars.put("myJsonArr", varArray3);
    taskService.complete(task.getId(), vars);
    value = (ArrayNode) runtimeService.getVariable(processInstance.getId(), "myJsonArr");
    assertThat(value).isNotNull();
    assertThat(value.get(0).get("var").asText()).isEqualTo("myValue");
    assertThat(value.get(1).get("var").asText()).isEqualTo("myOtherValue");
    assertThat(value.get(2).get("var").asText()).isEqualTo("myThirdValue");

    task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("userTaskSuccess");

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
          .processInstanceId(processInstance.getProcessInstanceId()).singleResult();
      value = (ArrayNode) historicVariableInstance.getValue();
      assertThat(value).isNotNull();
      assertThat(value.get(0).get("var").asText()).isEqualTo("myValue");
      assertThat(value.get(1).get("var").asText()).isEqualTo("myOtherValue");
      assertThat(value.get(2).get("var").asText()).isEqualTo("myThirdValue");
    }
  }

  protected ObjectNode createBigJsonObject() {
    ObjectNode valueNode = objectMapper.createObjectNode();
    for (int i = 0; i < 1000; i++) {
      ObjectNode childNode = objectMapper.createObjectNode();
      childNode.put("test", "this is a simple test text");
      childNode.put("test2", "this is a simple test2 text");
      childNode.put("test3", "this is a simple test3 text");
      childNode.put("test4", "this is a simple test4 text");
      valueNode.put("var" + i, childNode);
    }
    return valueNode;
  }

}
