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

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class DeploymentArtifactResource extends SecuredResource {
  
  @Get
  public InputRepresentation getDefinitionDiagram() {
    if(authenticate() == false) return null;
    
    String deploymentId = (String) getRequest().getAttributes().get("deploymentId");
    String resourceName = (String) getRequest().getAttributes().get("resourceName");
    
    if(deploymentId == null) {
      throw new ActivitiException("No deployment id provided");
    }

    final InputStream resourceStream = ActivitiUtil.getRepositoryService().getResourceAsStream(
        deploymentId, resourceName);

    if (resourceStream == null) {
      throw new ActivitiException("No resource with name " + resourceName + " could be found");
    }

    InputRepresentation output = new InputRepresentation(resourceStream);
    return output;
  }
}
