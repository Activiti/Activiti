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

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.repository.Deployment;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class DeploymentResourceDataResource extends SecuredResource {

  @Get
  public InputRepresentation getDeploymentResource() {
 if(authenticate() == false) return null;
    
    String deploymentId = getAttribute("deploymentId");
    if(deploymentId == null) {
      throw new ActivitiIllegalArgumentException("No deployment id provided");
    }
    String resourceId = getAttribute("resourceId");
    if(resourceId == null) {
      throw new ActivitiIllegalArgumentException("No resource id provided");
    }
    
    // Check if deployment exists
    Deployment deployment = ActivitiUtil.getRepositoryService().createDeploymentQuery().deploymentId(deploymentId).singleResult();
    if(deployment == null) {
      throw new ActivitiObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.", Deployment.class);
    }
    
    List<String> resourceList = ActivitiUtil.getRepositoryService().getDeploymentResourceNames(deploymentId);

    if (resourceList.contains(resourceId)) {
      final InputStream resourceStream = ActivitiUtil.getRepositoryService().getResourceAsStream(deploymentId, resourceId);

      // TODO: Externalize mime-type-mappings?
      MediaType mediaType = null;
      String lowerResourceName = resourceId.toLowerCase();
      if (lowerResourceName.endsWith("png")) {
        mediaType = MediaType.IMAGE_PNG;

      } else if (lowerResourceName.endsWith("xml") || lowerResourceName.endsWith("bpmn")) {
        mediaType = MediaType.TEXT_XML;

      } else {
        mediaType = MediaType.APPLICATION_ALL;
      }

      return new InputRepresentation(resourceStream, mediaType);
    } else {
      // Resource not found in deployment
      throw new ActivitiObjectNotFoundException("Could not find a resource with id '" + resourceId + "' in deployment '" + deploymentId + "'.", String.class);
    }
  }
}
