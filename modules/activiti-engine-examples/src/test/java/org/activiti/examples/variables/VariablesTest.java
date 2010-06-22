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
package org.activiti.examples.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ProcessInstance;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.junit.Test;

/**
 * @author Tom Baeyens
 */
public class VariablesTest extends ActivitiTestCase {

  @Test
  @ProcessDeclared
  public void testBasicVariableOperations() {
 
    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();

    // Start process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("cost center", 928374L);
    variables.put("customer", "coca-cola");
    variables.put("message", "<xml />");
    variables.put("start date", now);
    variables.put("nihil", null);
    variables.put("numbers", serializable);
    variables.put("manybits", bytes);
    ProcessInstance processInstance = processEngineBuilder.getProcessService().startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = processEngineBuilder.getProcessService().getVariables(processInstance.getId());
    assertEquals(928374L, variables.get("cost center"));
    assertEquals("coca-cola", variables.get("customer"));
    assertEquals("<xml />", variables.get("message"));
    assertEquals(now, variables.get("start date"));
    assertEquals(null, variables.get("nihil"));
    assertEquals(serializable, variables.get("numbers"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("manybits")));
    assertEquals(7, variables.size());

    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "cost center", null);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "customer", null);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "message", null);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "start date", null);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "nihil", null);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "numbers", null);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "manybits", null);

    variables = processEngineBuilder.getProcessService().getVariables(processInstance.getId());
    assertEquals(null, variables.get("cost center"));
    assertEquals(null, variables.get("customer"));
    assertEquals(null, variables.get("message"));
    assertEquals(null, variables.get("start date"));
    assertEquals(null, variables.get("nihil"));
    assertEquals(null, variables.get("numbers"));
    assertEquals(null, variables.get("manybits"));
    assertEquals(7, variables.size());

    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "new var", "hi");
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "cost center", 9987L);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "customer", "colgate");
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "message", "{json}");
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "start date", now);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "numbers", serializable);
    processEngineBuilder.getProcessService().setVariable(processInstance.getId(), "manybits", bytes);

    variables = processEngineBuilder.getProcessService().getVariables(processInstance.getId());
    assertEquals("hi", variables.get("new var"));
    assertEquals(9987L, variables.get("cost center"));
    assertEquals("colgate", variables.get("customer"));
    assertEquals("{json}", variables.get("message"));
    assertEquals(now, variables.get("start date"));
    assertEquals(null, variables.get("nihil"));
    assertEquals(serializable, variables.get("numbers"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("manybits")));
    assertEquals(8, variables.size());
  }
}
