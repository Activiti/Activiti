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

package org.activiti.rest.api;

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * @author Tijs Rademakers
 */
public class DefaultResource extends ServerResource {

  @Get
  public InputRepresentation execute() {
    String path = getRequest().getResourceRef().getPath();
    if(path.contains("/deployment") && path.contains("/resource")) {
      String deploymentId = path.substring(path.indexOf("/deployment/") + 12, path.indexOf("/", path.indexOf("/deployment/") + 12));
      String resourceName = path.substring(path.indexOf("/resource/") + 10);
      InputStream resource = ActivitiUtil.getRepositoryService().getResourceAsStream(deploymentId, resourceName);
      if (resource != null) {
        InputRepresentation output = new InputRepresentation(resource);
        return output;
      } else {
        throw new ActivitiException("There is no resource with name '" + resourceName + "' for deployment with id '" + deploymentId + "'.");
      }
    } else {
      throw new ActivitiException("No router defined");
    }
  }

}
