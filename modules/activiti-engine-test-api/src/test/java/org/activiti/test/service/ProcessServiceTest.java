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
package org.activiti.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessDefinition;
import org.activiti.engine.ProcessInstance;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joram Barrez
 */
public class ProcessServiceTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @ProcessDeclared(resources = {"oneTaskProcess.bpmn20.xml"})
  public void testStartProcessInstanceById() {
    List<ProcessDefinition> processDefinitions = deployer.getRepositoryService().findProcessDefinitions();
    assertEquals(1, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertNotNull(processDefinition.getId());

    ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceById(processDefinition.getId());
    assertNotNull(processInstance);
    assertNotNull(deployer.getTaskService().createTaskQuery().singleResult());
  }

  @Test
  @ProcessDeclared(resources={"oneTaskProcess.bpmn20.xml"})
  public void testFindProcessDefinitionById() {
    List<ProcessDefinition> definitions = deployer.getRepositoryService().findProcessDefinitions();
    assertEquals(1, definitions.size());

    ProcessDefinition processDefinition = deployer.getRepositoryService().findProcessDefinitionById(definitions.get(0).getId());
    assertNotNull(processDefinition);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertEquals("The One Task Process", processDefinition.getName());
  }

  @Test
  public void testFindProcessDefinitionByNullId() {
    exception.expect(ActivitiException.class);
    exception.expectMessage("Couldn't find process definiton");
    deployer.getRepositoryService().findProcessDefinitionById(null);
  }
}
