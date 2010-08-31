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

import java.util.List;

import org.activiti.engine.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;


/**
 * @author Tom Baeyens
 */
public class DeploymentQueryImpl extends AbstractQuery<Deployment> implements DeploymentQuery {

  protected String deploymentId;
  protected String nameLike;

  public DeploymentQueryImpl() {
  }

  public DeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DeploymentQueryImpl deploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  public DeploymentQueryImpl nameLike(String nameLike) {
    this.nameLike = nameLike;
    return this;
  }

  public DeploymentQueryImpl orderAsc(String column) {
    super.addOrder(column, SORTORDER_ASC);
    return this;
  }
  
  public DeploymentQueryImpl orderDesc(String column) {
    super.addOrder(column, SORTORDER_DESC);
    return this;
  }
  
  @Override
  public long executeCount(CommandContext commandContext) {
    return commandContext
      .getRepositorySession()
      .findDeploymentCountByQueryCriteria(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Deployment> executeList(CommandContext commandContext, Page page) {
    return (List) commandContext
      .getRepositorySession()
      .findDeploymentsByQueryCriteria(this, page);
  }
  
  public String getDeploymentId() {
    return deploymentId;
  }

  public String getNameLike() {
    return nameLike;
  }
}
