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

package org.activiti.engine.impl;

import org.activiti.engine.Deployment;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.repository.DeploymentBuilderImpl;
import org.activiti.impl.cmd.DeployCmd;
import org.activiti.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class RepositoryServiceImpl extends ServiceImpl implements RepositoryService {

  public RepositoryServiceImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public Deployment deploy(DeploymentBuilderImpl deploymentBuilder) {
    return commandExecutor.execute(new DeployCmd<Deployment>(deploymentBuilder));
  }
}
