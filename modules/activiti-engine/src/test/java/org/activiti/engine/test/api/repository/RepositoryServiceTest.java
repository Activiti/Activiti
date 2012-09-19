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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
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

    // See http://jira.codehaus.org/browse/ACT-1020, we have to query the process definition via the extra method to get all information
    // otherwise it is null:
    assertNull(processDefinition.getDescription());
    // and here we get it:
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
 
}
