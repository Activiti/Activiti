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
package org.activiti.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;

/**
 * @author Tijs Rademakers
 */
public class ProcessInstanceAndVariablesQueryTest extends PluggableActivitiTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static String PROCESS_DEFINITION_KEY_2 = "oneTaskProcess2";
  private static String PROCESS_DEFINITION_KEY_3 = "oneTaskProcess3";
  
  private List<String> processInstanceIds;

  /**
   * Setup starts 4 process instances of oneTaskProcess 
   * and 1 instance of oneTaskProcess2
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess3.bpmn20.xml")
      .deploy();
    
    Map<String, Object> startMap = new HashMap<String, Object>();
    startMap.put("test", "test");
    startMap.put("test2", "test2");
    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "", startMap).getId());
    }
    
    startMap.clear();
    startMap.put("anothertest", 123);
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "1", startMap).getId());
    
    startMap.clear();
    startMap.put("casetest", "MyCaseTest");
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_3, "1", startMap).getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }
  
  public void testQuery() {
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .variableValueEquals("anothertest", 123).singleResult();
    Map<String, Object> variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().includeProcessVariables().list();
    assertEquals(6, instanceList.size());
    
    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .variableValueLike("casetest", "MyCase%").singleResult();
    variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals("MyCaseTest", variableMap.get("casetest"));
    
    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .variableValueLikeIgnoreCase("casetest", "mycase%").singleResult();
    variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals("MyCaseTest", variableMap.get("casetest"));
    
    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .variableValueLikeIgnoreCase("casetest", "mycase2%").singleResult();
    assertNull(processInstance);
    
    instanceList = runtimeService.createProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY).list();
    assertEquals(4, instanceList.size());
    processInstance = instanceList.get(0);
    variableMap = processInstance.getProcessVariables();
    assertEquals(2, variableMap.size());
    assertEquals("test", variableMap.get("test"));
    assertEquals("test2", variableMap.get("test2"));
    
    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();
    variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));
    
    instanceList = runtimeService.createProcessInstanceQuery()
        .includeProcessVariables()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .listPage(0, 5);
    assertEquals(4, instanceList.size());
    processInstance = instanceList.get(0);
    variableMap = processInstance.getProcessVariables();
    assertEquals(2, variableMap.size());
    assertEquals("test", variableMap.get("test"));
    assertEquals("test2", variableMap.get("test2"));
    
    instanceList = runtimeService.createProcessInstanceQuery()
        .includeProcessVariables()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .listPage(0, 1);
    assertEquals(1, instanceList.size());
    processInstance = instanceList.get(0);
    variableMap = processInstance.getProcessVariables();
    assertEquals(2, variableMap.size());
    assertEquals("test", variableMap.get("test"));
    assertEquals("test2", variableMap.get("test2"));
    
    instanceList = runtimeService.createProcessInstanceQuery()
        .includeProcessVariables()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .orderByProcessDefinitionKey()
        .asc()
        .listPage(2, 4);
    assertEquals(2, instanceList.size());
    processInstance = instanceList.get(0);
    variableMap = processInstance.getProcessVariables();
    assertEquals(2, variableMap.size());
    assertEquals("test", variableMap.get("test"));
    assertEquals("test2", variableMap.get("test2"));
    
    instanceList = runtimeService.createProcessInstanceQuery()
        .includeProcessVariables()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .orderByProcessDefinitionKey()
        .asc()
        .listPage(4, 5);
    assertEquals(0, instanceList.size());
  }

  public void testOrQuery() {
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .or().variableValueEquals("undefined", 999).variableValueEquals("anothertest", 123).endOr().singleResult();
    Map<String, Object> variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));

    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .or().variableValueEquals("undefined", 999).endOr().singleResult();
    assertNull(processInstance);

    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .or().variableValueEquals("anothertest", 123).variableValueEquals("undefined", 999).endOr().singleResult();
    variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));

    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .or().variableValueEquals("anothertest", 999).endOr().singleResult();
    assertNull(processInstance);

    processInstance = runtimeService.createProcessInstanceQuery().includeProcessVariables()
        .or().variableValueEquals("anothertest", 999).variableValueEquals("anothertest", 123).endOr().singleResult();
    variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));
  }

  public void testOrQueryMultipleVariableValues() {
    ProcessInstanceQuery query0 = runtimeService.createProcessInstanceQuery().includeProcessVariables().or();
    for (int i = 0; i < 20; i++) {
        query0 = query0.variableValueEquals("anothertest", i);
    }
    query0 = query0.endOr();
    assertNull(query0.singleResult());

    ProcessInstanceQuery query1 = runtimeService.createProcessInstanceQuery().includeProcessVariables().or().variableValueEquals("anothertest", 123);
    for (int i = 0; i < 20; i++) {
        query1 = query1.variableValueEquals("anothertest", i);
    }
    query1 = query1.endOr();
    assertNull(query0.singleResult());

    ProcessInstance processInstance = query1.singleResult();
    Map<String, Object> variableMap = processInstance.getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));

    ProcessInstanceQuery query2 = runtimeService.createProcessInstanceQuery().includeProcessVariables().or();
    for (int i = 0; i < 20; i++) {
        query2 = query2.variableValueEquals("anothertest", i);
    }
    query2 = query2.endOr()
        .or()
        .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
        .processDefinitionId("undefined")
        .endOr();
    assertNull(query2.singleResult());

    ProcessInstanceQuery query3 = runtimeService.createProcessInstanceQuery().includeProcessVariables().or().variableValueEquals("anothertest", 123);
    for (int i = 0; i < 20; i++) {
        query3 = query3.variableValueEquals("anothertest", i);
    }
    query3 = query3.endOr()
        .or()
        .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
        .processDefinitionId("undefined")
        .endOr();
    variableMap = query3.singleResult().getProcessVariables();
    assertEquals(1, variableMap.size());
    assertEquals(123, variableMap.get("anothertest"));
  }

  public void testOrProcessVariablesLikeIgnoreCase() {
      List<ProcessInstance> instanceList = runtimeService
          .createProcessInstanceQuery().or()
          .variableValueLikeIgnoreCase("test", "TES%")
          .variableValueLikeIgnoreCase("test", "%XYZ").endOr()
          .list();
      assertEquals(4, instanceList.size());
  }

}
