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
package org.activiti.rest.api.repository;

import java.util.Map;

import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.rest.util.ActivitiPagingWebScript;
import org.activiti.rest.util.ActivitiRequest;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

/**
 * Returns a list of deployments
 *
 * @author Erik Winlof
 */
public class DeploymentsGet extends ActivitiPagingWebScript
{

  public DeploymentsGet() {
    properties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
    properties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
    properties.put("deploymentTime", DeploymentQueryProperty.DEPLOY_TIME);
  }

  /**
   * Prepares deployment info for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    DeploymentQuery deploymentQuery = getRepositoryService().createDeploymentQuery();
    paginateList(req, deploymentQuery, "deployments", model, "id");
  }

}
