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

package org.activiti.engine.test.db;

import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ProcessDefinitionPersistenceTest extends PluggableActivitiTestCase {

  public void testProcessDefinitionPersistence() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml").deploy().getId();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();

    assertEquals(2, processDefinitions.size());

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionIntrospection() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml").deploy().getId();

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(procDefId);

    assertEquals(procDefId, processDefinition.getId());
    assertEquals("Process One", processDefinition.getName());
    
    Process process = repositoryService.getBpmnModel(processDefinition.getId()).getMainProcess();
    StartEvent startElement = (StartEvent) process.getFlowElement("start");
    assertNotNull(startElement);
    assertEquals("start", startElement.getId());
    assertEquals("S t a r t", startElement.getName());
    assertEquals("the start event", startElement.getDocumentation());
    List<SequenceFlow> outgoingFlows = startElement.getOutgoingFlows();
    assertEquals(1, outgoingFlows.size());
    assertEquals("${a == b}", outgoingFlows.get(0).getConditionExpression());

    EndEvent endElement = (EndEvent) process.getFlowElement("end");
    assertNotNull(endElement);
    assertEquals("end", endElement.getId());

    assertEquals("flow1", outgoingFlows.get(0).getId());
    assertEquals("Flow One", outgoingFlows.get(0).getName());
    assertEquals("The only transitions in the process", outgoingFlows.get(0).getDocumentation());
    assertSame(startElement, outgoingFlows.get(0).getSourceFlowElement());
    assertSame(endElement, outgoingFlows.get(0).getTargetFlowElement());

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionQuery() {
    String deployment1Id = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml").deploy().getId();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().orderByProcessDefinitionVersion().asc().list();

    assertEquals(2, processDefinitions.size());

    String deployment2Id = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml").deploy().getId();

    assertEquals(4, repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().latestVersion().orderByProcessDefinitionName().asc().count());

    repositoryService.deleteDeployment(deployment1Id);
    repositoryService.deleteDeployment(deployment2Id);
  }

  public void testProcessDefinitionGraphicalNotationFlag() {
    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/db/process-with-di.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/db/process-without-di.bpmn20.xml").deploy().getId();

    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    ProcessDefinition processWithDi = repositoryService.createProcessDefinitionQuery().processDefinitionKey("processWithDi").singleResult();
    assertTrue(processWithDi.hasGraphicalNotation());

    ProcessDefinition processWithoutDi = repositoryService.createProcessDefinitionQuery().processDefinitionKey("processWithoutDi").singleResult();
    assertFalse(processWithoutDi.hasGraphicalNotation());

    repositoryService.deleteDeployment(deploymentId);

  }

}
