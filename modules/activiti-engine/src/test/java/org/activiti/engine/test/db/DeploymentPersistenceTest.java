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

import org.activiti.engine.Deployment;
import org.activiti.engine.test.ProcessEngineImplTestCase;



/**
 * @author Tom Baeyens
 */
public class DeploymentPersistenceTest extends ProcessEngineImplTestCase {

  public void testDeployment() {
    Deployment deployment = repositoryService
      .createDeployment()
      .addString("org/activiti/test/HelloWorld.string", "hello world")
      .addString("org/activiti/test/TheAnswer.string", "42")
      .deploy();
    
    List<Deployment> deployments = repositoryService.findDeployments();
    assertEquals(1, deployments.size());
    
    repositoryService.deleteDeploymentCascade(deployment.getId());
  }
}
