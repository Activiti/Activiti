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

package org.activiti.rest.service.api.legacy.deployment;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
@Deprecated
public class DeploymentsResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public DeploymentsResource() {
    properties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
    properties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
    properties.put("deploymentTime", DeploymentQueryProperty.DEPLOY_TIME);
  }
  
  @Get("json")
  public DataResponse getDeployments() {
    if(authenticate() == false) return null;
    
    DataResponse response = new LegacyDeploymentsPaginateList().paginateList(getQuery(), 
        ActivitiUtil.getRepositoryService().createDeploymentQuery(), "id", properties);
    return response;
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
