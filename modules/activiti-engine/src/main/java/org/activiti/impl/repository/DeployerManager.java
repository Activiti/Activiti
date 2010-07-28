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
package org.activiti.impl.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.persistence.repository.Deployer;
import org.activiti.impl.persistence.PersistenceSession;


/**
 * @author Tom Baeyens
 */
public class DeployerManager implements Serializable {

  private static final long serialVersionUID = 1L;

  List<Deployer> deployers = new ArrayList<Deployer>();

  public void deploy(DeploymentImpl deployment, PersistenceSession persistenceSession) {
    for (Deployer deployer: deployers) {
      //deployer.deploy(deployment, persistenceSession);
    }
  }

  public DeployerManager addDeployer(Deployer deployer) {
    deployers.add(deployer);
    return this;
  }
  
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  public DeployerManager setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
    return this;
  }
}
