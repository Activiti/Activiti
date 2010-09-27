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

package org.activiti.spring.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.pvm.test.PvmTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SpringAutoDeployTest extends PvmTestCase {
  
  public void testBasicActivitiSpringIntegration() {
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/activiti/spring/test/SpringAutoDeployTest-context.xml");
    
    RepositoryService repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();
    
    Set<String> processDefinitionKeys = new HashSet<String>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processDefinitionKeys.add(processDefinition.getKey());
    }
    
    Set<String> expectedProcessDefinitionKeys = new HashSet<String>();
    expectedProcessDefinitionKeys.add("a");
    expectedProcessDefinitionKeys.add("b");
    expectedProcessDefinitionKeys.add("c");
    
    assertEquals(expectedProcessDefinitionKeys, processDefinitionKeys);
    
    // clean up the deployment, required for subsequent tests
    String deploymentId = repositoryService.createDeploymentQuery().list().get(0).getId();
    repositoryService.deleteDeploymentCascade(deploymentId);
    
    applicationContext.destroy();
  }
  
  public void testAutomaticRedeployment() throws Exception {
    String appContextResource = "org/activiti/spring/test/SpringAutoDeployTest-context.xml";
    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(appContextResource);
    
    RepositoryService repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertEquals(1, deploymentQuery.count());
    ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
    assertEquals(3, processDefinitionQuery.count());
    
    // closing down Spring app context and recreating it doesn't lead to more deployments
    String appContextResourceNoDrop = "org/activiti/spring/test/SpringAutoDeployTest-no-drop-context.xml";
    applicationContext = new ClassPathXmlApplicationContext(appContextResourceNoDrop);
    repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
    assertEquals(1, deploymentQuery.count());
    assertEquals(3, processDefinitionQuery.count());
    
    // Updating the bpmn20 file should lead to a new deployment when restarting the Spring container
    applicationContext.destroy();
    
    String filePath = "org/activiti/spring/test/autodeploy.a.bpmn20.xml";
    String originalBpmnFileContent = readFileAsString(filePath);
    String updatedBpmnFileContent = originalBpmnFileContent.replace("flow1", "fromStartToEndFlow");
    writeStringToFile(updatedBpmnFileContent, filePath);
    
    // Classic produced/consumer problem here:
    // The file is already written in Java, but not yet completely persisted by the OS
    // Constructing the new app context reads the same file which is sometimes not yet fully written to disk
    waitUntilFileIsWritten(filePath, updatedBpmnFileContent.length());
    
    applicationContext = new ClassPathXmlApplicationContext(appContextResourceNoDrop);
    repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
    
    // Reset file content
    writeStringToFile(originalBpmnFileContent, filePath);
    
    // Assertions come AFTER the file write! Otherwise the process file is messed up if the assertions fail.
    assertEquals(2, deploymentQuery.count());
    assertEquals(6, processDefinitionQuery.count());
    
    // Remove deployments
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeploymentCascade(deployment.getId());
    }
  }
 
  private String readFileAsString(String filePath) throws java.io.IOException, URISyntaxException {
    byte[] buffer = new byte[(int) getFile(filePath).length()];
    BufferedInputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(getFile(filePath)));
      inputStream.read(buffer);
    } finally {
      if (inputStream != null) {
          inputStream.close();
      }
    }
    return new String(buffer);
  }
  
  
  private File getFile(String filePath) throws URISyntaxException {
    URL url = this.getClass().getClassLoader().getResource(filePath);
    return new File(url.toURI());
  }
  
  private void writeStringToFile(String content, String filePath) throws IOException, URISyntaxException {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(getFile(filePath)));
      outputStream.write(content.getBytes());
      outputStream.flush();
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }
  
  private boolean waitUntilFileIsWritten(String filePath, int expectedBytes) throws URISyntaxException {
    while (getFile(filePath).length() != (long) expectedBytes) {
      try {
        wait(100L);
      } catch (InterruptedException e) {
        fail(e.getMessage());
      }
    }
    return true;
  }
  
}