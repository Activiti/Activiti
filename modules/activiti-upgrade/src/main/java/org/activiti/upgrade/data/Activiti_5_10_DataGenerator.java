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
package org.activiti.upgrade.data;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Joram Barrez
 */
public class Activiti_5_10_DataGenerator implements DataGenerator {
  
  protected ProcessEngine processEngine;
  
  public static int NR_OF_INSTANCES_FOR_SUSPENSION = 5;
  
  public void run() {
    
    // VerifyProcessDefinitionDescriptionTest
    processEngine.getRepositoryService()
      .createDeployment()
      .name("verifyProcessDefinitionDescription")
      .addClasspathResource("org/activiti/upgrade/test/VerifyProcessDefinitionDescriptionTest.bpmn20.xml")
      .deploy();
    
    // SuspendAndActivateFunctionalityTest
    // Deploy test process, and start a few process instances
    Deployment deployment = processEngine.getRepositoryService().createDeployment()
      .name(Activiti_5_10_DataGenerator.class.getName())
      .addClasspathResource("org/activiti/upgrade/test/SuspendAndActivateUpgradeTest.bpmn20.xml")
      .deploy();
    
    ProcessDefinition processDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult();
    
    for (int i=0; i<NR_OF_INSTANCES_FOR_SUSPENSION; i++) {
      processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
    }
  }
  
  public void setProcessEngine(org.activiti.engine.ProcessEngine processEngine) {
    this.processEngine = processEngine;
  };

}
