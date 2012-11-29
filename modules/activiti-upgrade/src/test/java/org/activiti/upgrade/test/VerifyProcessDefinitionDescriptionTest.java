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
package org.activiti.upgrade.test;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.upgrade.UpgradeUtil;

/**
 * This is an upgrade test added for the 5.11 release. In that release, we've added a database
 * column to store the process definition description (documentation element in bpmn 2.0 xml).
 * 
 * @author Joram Barrez
 */
public class VerifyProcessDefinitionDescriptionTest extends UpgradeTestCase {
  
  public void testProcessDefinitionDescription() {
    
    // Test makes only sense on 5.11
    if (UpgradeUtil.getProcessEngineVersion(processEngine) == 11 && isTestRunningInUpgrade("5.10", "5.11")) {
      
      // We don't upgrade the process definition description, we only add the column.
      // So we'll just verify if the process definition description is null and if 
      // we can add a process description afterwards
      
      ProcessDefinition processDefinition = getLatestVersionOfProcessDefinition();
      assertNotNull(processDefinition);
      assertNull(processDefinition.getDescription());
      
      // If we now redeploy the same process definition, the description should be set
      deployTestProcess();
      processDefinition = getLatestVersionOfProcessDefinition();
      assertEquals("This is not really a very usable process...", processDefinition.getDescription());
      
    }
  }
  
  protected void deployTestProcess() {
    processEngine.getRepositoryService()
      .createDeployment()
      .name("verifyProcessDefinitionDescription")
      .addClasspathResource("org/activiti/upgrade/test/VerifyProcessDefinitionDescriptionTest.bpmn20.xml")
      .deploy();
  }
  
  protected ProcessDefinition getLatestVersionOfProcessDefinition() {
    return processEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey("verifyProcessDefinitionDescription")
            .latestVersion()
            .singleResult();
  }

}
