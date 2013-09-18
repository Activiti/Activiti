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

import java.io.InputStream;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
@Deprecated
public class DeploymentArtifactResource extends SecuredResource {
  
  @Get
  public InputRepresentation getDefinitionDiagram() {
    if(authenticate() == false) return null;
    
    String deploymentId = (String) getRequest().getAttributes().get("deploymentId");
    String resourceName = (String) getRequest().getAttributes().get("resourceName");
    
    if(deploymentId == null) {
      throw new ActivitiIllegalArgumentException("No deployment id provided");
    }
    
    if(resourceName == null) {
      throw new ActivitiIllegalArgumentException("No resource name provided");
    }

    final InputStream resourceStream = ActivitiUtil.getRepositoryService().getResourceAsStream(
        deploymentId, resourceName);

    MediaType mediaType = null;
    String lowerResourceName = resourceName.toLowerCase();
    if (lowerResourceName.endsWith("png")) {
      mediaType = MediaType.IMAGE_PNG;
      
    } else if (lowerResourceName.endsWith("xml") || lowerResourceName.endsWith("bpmn")) {
      mediaType = MediaType.TEXT_XML;
    
    } else {
      mediaType = MediaType.APPLICATION_ALL;
    }
    
    InputRepresentation output = new InputRepresentation(resourceStream, mediaType);
    return output;
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
