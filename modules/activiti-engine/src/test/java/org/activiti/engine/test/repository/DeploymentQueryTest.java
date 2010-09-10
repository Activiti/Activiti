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

package org.activiti.engine.test.repository;

import java.util.List;

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;


/**
 * @author Tom Baeyens
 */
public class DeploymentQueryTest extends ActivitiInternalTestCase {

  public void testDeploymentQueries() {
    String deploymentOneId = repositoryService
      .createDeployment()
      .name("org/activiti/engine/test/repository/one.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml")
      .deploy()
      .getId();
 
    String deploymentTwoId = repositoryService
      .createDeployment()
      .name("org/activiti/engine/test/repository/two.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml")
      .deploy()
      .getId();
 
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
      .orderAsc(DeploymentQuery.PROPERTY_NAME)
      .list();
    
    Deployment deploymentOne = deployments.get(0);
    assertEquals("org/activiti/engine/test/repository/one.bpmn20.xml", deploymentOne.getName());
    assertEquals(deploymentOneId, deploymentOne.getId());

    Deployment deploymentTwo = deployments.get(1);
    assertEquals("org/activiti/engine/test/repository/two.bpmn20.xml", deploymentTwo.getName());
    assertEquals(deploymentTwoId, deploymentTwo.getId());
    
    deployments = repositoryService.createDeploymentQuery()
      .nameLike("%one%")
      .orderAsc(DeploymentQuery.PROPERTY_NAME)
      .list();
    
    assertEquals("org/activiti/engine/test/repository/one.bpmn20.xml", deployments.get(0).getName());
    assertEquals(1, deployments.size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderAsc(DeploymentQuery.PROPERTY_ID)
      .list()
      .size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderAsc(DeploymentQuery.PROPERTY_DEPLOY_TIME)
      .list()
      .size());

    repositoryService.deleteDeploymentCascade(deploymentOneId);
    repositoryService.deleteDeploymentCascade(deploymentTwoId);
  }

}
