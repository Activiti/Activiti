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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class BpmnDeploymentTest extends ActivitiInternalTestCase {
  
  @Deployment
  public void testGetBpmnXmlFileThroughService() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);
    
    // verify bpmn file name
    assertEquals(1, deploymentResources.size());
    String bpmnResourceName = "org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    assertEquals(bpmnResourceName, deploymentResources.get(0));
    
    // verify content
    InputStream deploymentInputStream = repositoryService.getResourceAsStream(deploymentId, bpmnResourceName);
    String contentFromDeployment = readInputStreamToString(deploymentInputStream);
    assertTrue(contentFromDeployment.length() > 0);
    assertTrue(contentFromDeployment.contains("process id=\"emptyProcess\""));
    
    InputStream fileInputStream = ReflectUtil.getClassLoader().getResourceAsStream("org/activiti/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml");
    String contentFromFile = readInputStreamToString(fileInputStream);
    assertEquals(contentFromFile, contentFromDeployment);
  }
  
  private String readInputStreamToString(InputStream inputStream) {
    assertNotNull("Provided inputstream is null", inputStream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder strb = new StringBuilder();
    try {
      String line = reader.readLine();
      while (line != null) {
        strb.append(line);
        line = reader.readLine();
      }
    } catch (IOException e) {
      fail("Couldnt read from inputstream");
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          fail("Couldn't close reader");
        }
      }
    }
    return strb.toString();
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

}
