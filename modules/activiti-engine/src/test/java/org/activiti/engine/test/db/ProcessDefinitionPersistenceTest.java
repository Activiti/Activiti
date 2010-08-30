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

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.ProcessEngineTestCase;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionPersistenceTest extends ProcessEngineTestCase {

  public void testProcessDefinitionPersistence() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/db/processOne.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/db/processTwo.bpmn20.xml")
      .deploy()
      .getId();
  
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();
    
    assertEquals(2, processDefinitions.size());
    
    repositoryService.deleteDeployment(deploymentId);
  }
}
