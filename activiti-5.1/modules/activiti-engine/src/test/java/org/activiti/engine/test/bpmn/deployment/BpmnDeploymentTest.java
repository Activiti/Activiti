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

package org.activiti.engine.test.bpmn.deployment;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class BpmnDeploymentTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testGetBpmnXmlFileThroughService() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);
    
    // verify bpmn file name
    assertEquals(1, deploymentResources.size());
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    assertEquals(bpmnResourceName, deploymentResources.get(0));
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertEquals(bpmnResourceName, processDefinition.getResourceName());
    assertNull(processDefinition.getDiagramResourceName());
    assertFalse(processDefinition.hasStartFormKey());
    
    ReadOnlyProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinition.getId());
    assertNull(readOnlyProcessDefinition.getDiagramResourceName());
    
    // verify content
    InputStream deploymentInputStream = repositoryService.getResourceAsStream(deploymentId, bpmnResourceName);
    String contentFromDeployment = readInputStreamToString(deploymentInputStream);
    assertTrue(contentFromDeployment.length() > 0);
    assertTrue(contentFromDeployment.contains("process id=\"emptyProcess\""));
    
    InputStream fileInputStream = ReflectUtil.getResourceAsStream("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml");
    String contentFromFile = readInputStreamToString(fileInputStream);
    assertEquals(contentFromFile, contentFromDeployment);
  }
  
  private String readInputStreamToString(InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, "input stream");
    return new String(bytes);
  }
  
  public void testViolateProcessDefinitionIdMaximumLength() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/deployment/processWithLongId.bpmn20.xml")
        .deploy();
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("id can be maximum 64 characters", e.getMessage());
    }
    
    // Verify that nothing is deployed
    assertEquals(0, repositoryService.createDeploymentQuery().count());
  }

  @Deployment(resources={
    "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml",
    "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg"
  })
  public void testProcessDiagramResource() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    
    assertEquals("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml", processDefinition.getResourceName());
    assertTrue(processDefinition.hasStartFormKey());

    String diagramResourceName = processDefinition.getDiagramResourceName();
    assertEquals("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg", diagramResourceName);
    
    InputStream diagramStream = repositoryService.getResourceAsStream(deploymentId, "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg");
    byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertEquals(33343, diagramBytes.length);
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.bpmn20.xml",
          "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg",
          "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg",
          "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg"
        })
  public void testMultipleDiagramResourcesProvided() {
    ProcessDefinition processA = repositoryService.createProcessDefinitionQuery().processDefinitionKey("a").singleResult();
    ProcessDefinition processB = repositoryService.createProcessDefinitionQuery().processDefinitionKey("b").singleResult();
    ProcessDefinition processC = repositoryService.createProcessDefinitionQuery().processDefinitionKey("c").singleResult();
    
    assertEquals("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg", processA.getDiagramResourceName());
    assertEquals("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg", processB.getDiagramResourceName());
    assertEquals("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg", processC.getDiagramResourceName());
  }
  
}
