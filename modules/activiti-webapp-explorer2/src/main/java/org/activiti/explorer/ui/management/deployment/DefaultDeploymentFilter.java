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

package org.activiti.explorer.ui.management.deployment;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.explorer.ui.management.deployment.DeploymentListQuery.DeploymentListitem;


/**
 * @author Frederik Heremans
 */
public class DefaultDeploymentFilter implements DeploymentFilter {

  protected static final String PROPERTY_ID = "id";
  protected static final String PROPERTY_NAME = "name";
  protected static final String PROPERTY_KEY = "key";
  
  
  public DeploymentQuery getQuery(RepositoryService repositoryService) {
    return repositoryService.createDeploymentQuery()
    .orderByDeploymentName().asc()
    .orderByDeploymentId().asc();
  }

  public DeploymentQuery getCountQuery(RepositoryService repositoryService) {
    return repositoryService.createDeploymentQuery();
  }

  public DeploymentListitem createItem(Deployment deployment) {
    return new DeploymentListitem(deployment);
  }

}
