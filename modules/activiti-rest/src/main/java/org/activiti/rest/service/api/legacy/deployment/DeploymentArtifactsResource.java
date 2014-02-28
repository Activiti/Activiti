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

import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.RequestUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
@Deprecated
public class DeploymentArtifactsResource extends SecuredResource {
  
  @Get
  public ObjectNode getDeploymentArtifacts() {
    if(authenticate() == false) return null;
    
    String deploymentId = (String) getRequest().getAttributes().get("deploymentId");
    
    if(deploymentId == null) {
      throw new ActivitiIllegalArgumentException("No deployment id provided");
    }

    Deployment deployment = ActivitiUtil.getRepositoryService().createDeploymentQuery().deploymentId(deploymentId).singleResult();
    List<String> resourceList = ActivitiUtil.getRepositoryService().getDeploymentResourceNames(deploymentId);

    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    responseJSON.put("id", deployment.getId());
    responseJSON.put("name", deployment.getName());
    responseJSON.put("deploymentTime", RequestUtil.dateToString(deployment.getDeploymentTime()));
    responseJSON.put("category", deployment.getCategory());
    
    ArrayNode resourceArray = new ObjectMapper().createArrayNode();
    
    for (String resourceName : resourceList) {
      resourceArray.add(resourceName);
    }
    
    responseJSON.put("resources", resourceArray);
    
    return responseJSON;
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
