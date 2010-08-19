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
package org.activiti.engine.test.service;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessDefinition;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Joram Barrez
 */
public class ProcessServiceTest extends ProcessEngineTestCase {

  @Deployment(resources = {"oneTaskProcess.bpmn20.xml"})
  public void testStartProcessInstanceById() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertNotNull(processDefinition.getId());

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    assertNotNull(processInstance);
    assertNotNull(taskService.createTaskQuery().singleResult());
  }

  @Deployment(resources={"oneTaskProcess.bpmn20.xml"})
  public void testFindProcessDefinitionById() {
    List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, definitions.size());

    ProcessDefinition processDefinition = repositoryService.findProcessDefinitionById(definitions.get(0).getId());
    assertNotNull(processDefinition);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertEquals("The One Task Process", processDefinition.getName());
  }

  public void testFindProcessDefinitionByNullId() {
    try {
      repositoryService.findProcessDefinitionById(null);
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Couldn't find process definiton", e.getMessage());
    }
  }
}
