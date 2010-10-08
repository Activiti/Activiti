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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.DeploymentQueryProperty;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentQueryImpl extends AbstractQuery<DeploymentQuery, Deployment> implements DeploymentQuery {

  protected String deploymentId;
  protected String name;
  protected String nameLike;
  protected DeploymentQueryProperty orderProperty;

  public DeploymentQueryImpl() {
  }

  public DeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DeploymentQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiException("Deployment id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }
  
  public DeploymentQuery name(String name) {
    if (name == null) {
      throw new ActivitiException("Deployment name is null");
    }
    this.name = name;
    return this;
  }

  public DeploymentQueryImpl nameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("Namelike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  //sorting ////////////////////////////////////////////////////////
  
  public DeploymentQueryImpl orderByDeploymentId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_ID);
  }
  
  public DeploymentQuery orderByDeploymenTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }
  
  public DeploymentQuery orderByDeploymentName() {
    return orderBy(DeploymentQueryProperty.NAME);
  }
  
  public DeploymentQueryImpl orderBy(QueryProperty property) {
    if(!(property instanceof DeploymentQueryProperty)) {
      throw new ActivitiException("Only DeploymentQueryProperty can be used with orderBy");
    }
    this.orderProperty = (DeploymentQueryProperty) property;
    return this;
  }
  
  public DeploymentQuery asc() {
    return direction(Direction.ASCENDING);
  }
  
  public DeploymentQuery desc() {
    return direction(Direction.DESCENDING);
  }
  
  public DeploymentQuery direction(Direction direction) {
    if (orderProperty==null) {
      throw new ActivitiException("You should call any of the orderBy methods first before specifying a direction");
    }
    addOrder(orderProperty.getName(), direction.getName());
    orderProperty = null;
    return this;
  }
  
  //results ////////////////////////////////////////////////////////
  
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getRepositorySession()
      .findDeploymentCountByQueryCriteria(this);
  }

  @Override
  public List<Deployment> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getRepositorySession()
      .findDeploymentsByQueryCriteria(this, page);
  }
  
  protected void checkQueryOk() {
    if (orderProperty != null) {
      throw new ActivitiException("Invalid query: call asc() or desc() after using orderByXX()");
    }
  }
  
  //getters ////////////////////////////////////////////////////////
  
  public String getDeploymentId() {
    return deploymentId;
  }
  
  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }
}
