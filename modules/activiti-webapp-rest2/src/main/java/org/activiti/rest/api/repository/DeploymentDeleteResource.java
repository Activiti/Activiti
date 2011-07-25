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

import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Delete;

/**
 * @author Tijs Rademakers
 */
public class DeploymentDeleteResource extends SecuredResource {
  
  @Delete
  public ObjectNode deleteDeployment() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    String deploymentId = (String) getRequest().getAttributes().get("deploymentId");
    Boolean cascade = RequestUtil.getBoolean(getQuery(), "cascade", false);
    if (cascade) {
      ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId, true);
    }
    else {
      ActivitiUtil.getRepositoryService().deleteDeployment(deploymentId);
    }
    ObjectNode successNode = new ObjectMapper().createObjectNode();
    successNode.put("success", true);
    return successNode;
  }
}
