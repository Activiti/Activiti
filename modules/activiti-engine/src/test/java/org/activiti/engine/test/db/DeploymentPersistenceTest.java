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

import org.activiti.engine.impl.persistence.RepositorySession;
import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.engine.test.ProcessEngineImplTestCase;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;



/**
 * @author Tom Baeyens
 */
public class DeploymentPersistenceTest extends ProcessEngineImplTestCase {

  public void testDeployment() {
    final DeploymentEntity deployment = new DeploymentEntity();
    
    final int deploymentId = commandExecutor.execute(new Command<Integer>() {
      public Integer execute(CommandContext commandContext) {
        RepositorySession repositorySession = commandContext.getRepositorySession();
        repositorySession.insertDeployment(deployment);
        
//        for (ResourceEntity resource: deployment.getResources().values()) {
//          repositorySession.insertResource(resource);
//        }
        
        return 4;
      }
    });
  }
}
