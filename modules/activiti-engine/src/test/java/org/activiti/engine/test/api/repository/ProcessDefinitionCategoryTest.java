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

import java.util.HashSet;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;


public class ProcessDefinitionCategoryTest extends PluggableActivitiTestCase {

  
  public void testQueryByCategoryNotEquals() {
    Deployment deployment = repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/repository/processCategoryOne.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/repository/processCategoryTwo.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/repository/processCategoryThree.bpmn20.xml")
    .deploy();
    
    HashSet<String> processDefinitionNames = getProcessDefinitionNames(repositoryService.createProcessDefinitionQuery()
      .processDefinitionCategoryNotEquals("one")
      .list());
    HashSet<String> expectedProcessDefinitionNames = new HashSet<String>();
    expectedProcessDefinitionNames.add("processTwo");
    expectedProcessDefinitionNames.add("processThree");
    assertEquals(expectedProcessDefinitionNames, processDefinitionNames);
    
    processDefinitionNames = getProcessDefinitionNames(repositoryService.createProcessDefinitionQuery()
      .processDefinitionCategoryNotEquals("two")
      .list());
    expectedProcessDefinitionNames = new HashSet<String>();
    expectedProcessDefinitionNames.add("processOne");
    expectedProcessDefinitionNames.add("processThree");
    assertEquals(expectedProcessDefinitionNames, processDefinitionNames);
    
    repositoryService.deleteDeployment(deployment.getId());
  }

  private HashSet<String> getProcessDefinitionNames(List<ProcessDefinition> processDefinitions) {
    HashSet<String> processDefinitionNames = new HashSet<String>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processDefinitionNames.add(processDefinition.getKey());
    }
    return processDefinitionNames;
  }
}
