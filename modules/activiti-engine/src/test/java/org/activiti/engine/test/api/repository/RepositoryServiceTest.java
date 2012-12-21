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

package org.activiti.engine.test.api.repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class RepositoryServiceTest extends PluggableActivitiTestCase {

  @Deployment(resources = {
  "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testStartProcessInstanceById() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
  
    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertNotNull(processDefinition.getId());
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testFindProcessDefinitionById() {
    List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, definitions.size());
  
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(definitions.get(0).getId()).singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNotNull(processDefinition);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertEquals("The One Task Process", processDefinition.getName());

    processDefinition = repositoryService.getProcessDefinition(definitions.get(0).getId());    
    assertEquals("This is a process for testing purposes", processDefinition.getDescription());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteDeploymentWithRunningInstances() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    // Try to delete the deployment
    try {
      repositoryService.deleteDeployment(processDefinition.getDeploymentId());
      fail("Exception expected");
    } catch (RuntimeException ae) {
      // Exception expected when deleting deployment with running process
    }
  }
  
  public void testDeleteDeploymentNullDeploymentId() {
    try {
      repositoryService.deleteDeployment(null);    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
  }
  
  public void testDeleteDeploymentCascadeNullDeploymentId() {
    try {
      repositoryService.deleteDeployment(null, true);    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteDeploymentCascadeWithRunningInstances() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    // Try to delete the deployment, no exception should be thrown
    repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
  }
  
  public void testFindDeploymentResourceNamesNullDeploymentId() {
    try {
      repositoryService.getDeploymentResourceNames(null);    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
  }
  
  public void testDeploymentWithDelayedProcessDefinitionActivation() {
    
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    Date inThreeDays = new Date(startTime.getTime() + (3 * 24 * 60 * 60 * 1000));
    
    // Deploy process, but activate after three days
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment()
            .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
            .addClasspathResource("org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml")
            .activateProcessDefinitionsOn(inThreeDays)
            .deploy();
    
    assertEquals(1, repositoryService.createDeploymentQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    
    // Shouldn't be able to start a process instance
    try {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
      fail();
    } catch (ActivitiException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    
    // Move time four days forward, the timer will fire and the process definitions will be active
    Date inFourDays = new Date(startTime.getTime() + (4 * 24 * 60 * 60 * 1000));
    ClockUtil.setCurrentTime(inFourDays);
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    
    assertEquals(1, repositoryService.createDeploymentQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    // Should be able to start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    // Cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetResourceAsStreamUnexistingResourceInExistingDeployment() {
    // Get hold of the deployment id
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    
    try {
      repositoryService.getResourceAsStream(deployment.getId(), "org/activiti/engine/test/api/unexistingProcess.bpmn.xml");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("no resource found with name", ae.getMessage());
    }
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetResourceAsStreamUnexistingDeployment() {
    
    try {
      repositoryService.getResourceAsStream("unexistingdeployment", "org/activiti/engine/test/api/unexistingProcess.bpmn.xml");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("no resource found with name", ae.getMessage());
    }
  }
  

  public void testGetResourceAsStreamNullArguments() {
    try {
      repositoryService.getResourceAsStream(null, "resource");    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
    
    try {
      repositoryService.getResourceAsStream("deployment", null);    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("resourceName is null", ae.getMessage());
    }
  }

  public void testNewModelPersistence() {
    Model model = repositoryService.newModel();
    assertNotNull(model);
    
    model.setName("Test model");
    model.setCategory("test");
    model.setMetaInfo("meta");
    repositoryService.saveModel(model);
    
    assertNotNull(model.getId());
    model = repositoryService.getModel(model.getId());
    assertNotNull(model);
    assertEquals("Test model", model.getName());
    assertEquals("test", model.getCategory());
    assertEquals("meta", model.getMetaInfo());
    assertNotNull(model.getCreateTime());
    assertEquals(Integer.valueOf(1), model.getVersion());
    
    repositoryService.deleteModel(model.getId());
  }
  
  public void testNewModelWithSource() throws Exception {
    Model model = repositoryService.newModel();
    model.setName("Test model");
    byte[] testSource = "modelsource".getBytes("utf-8");
    repositoryService.saveModel(model);
    
    assertNotNull(model.getId());
    repositoryService.addModelEditorSource(model.getId(), testSource);
    
    model = repositoryService.getModel(model.getId());
    assertNotNull(model);
    assertEquals("Test model", model.getName());
    
    byte[] editorSourceBytes = repositoryService.getModelEditorSource(model.getId());
    assertEquals("modelsource", new String(editorSourceBytes, "utf-8"));
    
    repositoryService.deleteModel(model.getId());
  }
  
  public void testUpdateModelPersistence() throws Exception {
    Model model = repositoryService.newModel();
    assertNotNull(model);
    
    model.setName("Test model");
    model.setCategory("test");
    model.setMetaInfo("meta");
    repositoryService.saveModel(model);
    
    assertNotNull(model.getId());
    model = repositoryService.getModel(model.getId());
    assertNotNull(model);
    
    model.setName("New name");
    model.setCategory("New category");
    model.setMetaInfo("test");
    model.setVersion(2);
    repositoryService.saveModel(model);
    
    assertNotNull(model.getId());
    repositoryService.addModelEditorSource(model.getId(), "new".getBytes("utf-8"));
    repositoryService.addModelEditorSourceExtra(model.getId(), "new".getBytes("utf-8"));
    
    model = repositoryService.getModel(model.getId());
    
    assertEquals("New name", model.getName());
    assertEquals("New category", model.getCategory());
    assertEquals("test", model.getMetaInfo());
    assertNotNull(model.getCreateTime());
    assertEquals(Integer.valueOf(2), model.getVersion());
    assertEquals("new", new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
    assertEquals("new", new String(repositoryService.getModelEditorSourceExtra(model.getId()), "utf-8"));
    
    repositoryService.deleteModel(model.getId());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testProcessDefinitionEntitySerializable() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getProcessDefinition(procDefId);
    
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new ObjectOutputStream(baos).writeObject(processDefinition);
      
      byte[] bytes = baos.toByteArray();
      assertTrue(bytes.length > 0);
      System.out.println("-----> " + bytes.length);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }
}
