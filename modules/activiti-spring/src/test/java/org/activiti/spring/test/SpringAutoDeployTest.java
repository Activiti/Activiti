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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.pvm.test.PvmTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author Tom Baeyens
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
    String deploymentId = repositoryService.findDeployments().get(0).getId();
    repositoryService.deleteDeploymentCascade(deploymentId);
    
    applicationContext.destroy();
  }
}
