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

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tim Stephenson
 */
public class JsonTest extends PluggableActivitiTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Deployment
  public void testJsonObjectAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();
   
    vars.put("myJsonObj", "{\"var\":\"myValue\"}");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJsonAvailableProcess", vars);
    
    // Check JSON has been parsed as expected
    JsonObject value = (JsonObject) runtimeService.getVariable(processInstance.getId(), "myJsonObj");
    assertNotNull(value);
    assertEquals("myValue", value.getString("var"));

//    JsonObjectBuilder builder= Json.createObjectBuilder();
//    value.put("var2",builder.add("var2", "myOtherValue").build());
    runtimeService.setVariable(processInstance.getId(), "myJsonObj", "{\"var\":\"myValue\",\"var2\":\"myOtherValue\"}");

    // Check JSON has been updated as expected
    value = (JsonObject) runtimeService.getVariable(processInstance.getId(), "myJsonObj");
    assertNotNull(value);
    assertEquals("myValue", value.getString("var"));
    assertEquals("myOtherValue", value.getString("var2"));


    Task task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);
    vars.put("myJsonObj", "{\"var\":\"myValue\",\"var2\":\"myOtherValue\", \"var3\":\"myThirdValue\"}");
    taskService.complete(task.getId(), vars);
    value = (JsonObject) runtimeService.getVariable(processInstance.getId(), "myJsonObj");
    assertNotNull(value);
    assertEquals("myValue", value.getString("var"));
    assertEquals("myOtherValue", value.getString("var2"));
    assertEquals("myThirdValue", value.getString("var3"));

    task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);
    assertEquals("userTaskSuccess", task.getTaskDefinitionKey());

//    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
//        .processInstanceId(processInstance.getProcessInstanceId()).finished().singleResult();
//    assertNotNull(historicProcessInstance);

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getProcessInstanceId()).singleResult();
    value = (JsonObject) historicVariableInstance.getValue();
    assertNotNull(value);
    assertEquals("myValue", value.getString("var"));
    assertEquals("myOtherValue", value.getString("var2"));
    assertEquals("myThirdValue", value.getString("var3"));
  }

  @Deployment
  public void testJsonArrayAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();

    vars.put("myJsonArr", "[{\"var\":\"myValue\"}]");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJsonAvailableProcess", vars);

    // Check JSON has been parsed as expected
    JsonArray value = (JsonArray) runtimeService.getVariable(processInstance.getId(), "myJsonArr");
    assertNotNull(value);
    assertEquals("myValue", value.getJsonObject(0).getString("var"));

    runtimeService.setVariable(processInstance.getId(), "myJsonArr",
        "[{\"var\":\"myValue\"},{\"var\":\"myOtherValue\"}]");

    // Check JSON has been updated as expected
    value = (JsonArray) runtimeService.getVariable(processInstance.getId(), "myJsonArr");
    assertNotNull(value);
    assertEquals("myValue", value.getJsonObject(0).getString("var"));
    assertEquals("myOtherValue", value.getJsonObject(1).getString("var"));


    Task task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);
    vars.put("myJsonArr", "[{\"var\":\"myValue\"},{\"var\":\"myOtherValue\"},{\"var\":\"myThirdValue\"}]");
    taskService.complete(task.getId(), vars);
    value = (JsonArray) runtimeService.getVariable(processInstance.getId(), "myJsonArr");
    assertNotNull(value);
    assertEquals("myValue", value.getJsonObject(0).getString("var"));
    assertEquals("myOtherValue", value.getJsonObject(1).getString("var"));
    assertEquals("myThirdValue", value.getJsonObject(2).getString("var"));

    task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);
    assertEquals("userTaskSuccess", task.getTaskDefinitionKey());

//    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
//        .processInstanceId(processInstance.getProcessInstanceId()).finished().singleResult();
//    assertNotNull(historicProcessInstance);

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstance.getProcessInstanceId()).singleResult();
    value = (JsonArray) historicVariableInstance.getValue();
    assertNotNull(value);
    assertEquals("myValue", value.getJsonObject(0).getString("var"));
    assertEquals("myOtherValue", value.getJsonObject(1).getString("var"));
    assertEquals("myThirdValue", value.getJsonObject(2).getString("var"));
  }

}
