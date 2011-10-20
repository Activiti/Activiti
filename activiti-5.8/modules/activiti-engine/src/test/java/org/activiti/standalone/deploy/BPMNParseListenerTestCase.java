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

package org.activiti.standalone.deploy;

import java.io.InputStream;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 */
public class BPMNParseListenerTestCase extends ResourceActivitiTestCase {
  
  public BPMNParseListenerTestCase() {
    super("org/activiti/standalone/deploy/bpmn.parse.listener.activiti.cfg.xml");
  }

  @Deployment
  public void testAlterProcessDefinitionKeyWhenDeploying() throws Exception {
    // Check if process-definition has different key
    assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess-modified").count());
    
    // Check if process has an automatically-generated diagram
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess-modified").singleResult();
    assertNotNull(processDefinition.getDiagramResourceName());
   
    // Get diagram
    InputStream diagramStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName());
    try {
      // Validate if retrieving the image resource works
      byte[] buffer = new byte[1];
      assertEquals(1, diagramStream.read(buffer));
      
    } finally {
      if(diagramStream != null) {
        diagramStream.close();
      }
    }
    
  }
}
