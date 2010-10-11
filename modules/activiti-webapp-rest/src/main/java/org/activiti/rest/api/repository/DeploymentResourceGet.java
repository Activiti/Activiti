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

import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiStreamingWebScript;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DeploymentResourceGet extends ActivitiStreamingWebScript {

  /**
   * Prepares details about the process engine for the webscript template.
   *
   * @param req The activiti webscript request
   * @param res The webscript response
   */
  @Override
  protected void executeStreamingWebScript(ActivitiRequest req, WebScriptResponse res) {
    String deploymentId = req.getMandatoryPathParameter("deploymentId"),
      resourceName = req.getMandatoryPathParameter("resourceName");
    InputStream resource = getRepositoryService().getResourceAsStream(deploymentId, resourceName);
    if (resource != null) {
      try {
        streamResponse(res, resource, new Date(0), null, true, resourceName, getMimeType(resource));
      } catch (IOException e) {
        throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "The resource with name '" + resourceName + "' for deployment with id '" + deploymentId + "' could not be streamed: " + e.getMessage());
      }
    }
    else {
      throw new WebScriptException(Status.STATUS_NOT_FOUND, "There is no resource with name '" + resourceName + "' for deployment with id '" + deploymentId + "'.");
    }
  }

}