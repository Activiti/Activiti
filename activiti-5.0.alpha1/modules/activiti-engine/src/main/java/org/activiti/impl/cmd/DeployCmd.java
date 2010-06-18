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
package org.activiti.impl.cmd;

import org.activiti.Configuration;
import org.activiti.Deployment;
import org.activiti.impl.Cmd;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.repository.DeploymentImpl;
import org.activiti.impl.tx.TransactionContext;


/**
 * @author Tom Baeyens
 */
public class DeployCmd<T> implements Cmd<Deployment> {

  DeploymentImpl deployment;
  
  public DeployCmd(DeploymentImpl deployment) {
    this.deployment = deployment;
  }

  public Deployment execute(TransactionContext transactionContext) {
    DeployerManager deployerManager = transactionContext.getProcessEngine()
      .getConfigurationObject(Configuration.NAME_DEPLOYERMANAGER, DeployerManager.class);
    deployerManager.deploy(deployment, transactionContext);
    return deployment;
  }
}
