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

package org.activiti.rest.service.api.legacy.management;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class JobsExecuteResource extends SecuredResource {
  
  @Post
  public ObjectNode startProcessInstance(Representation entity) {
    try {
      if(authenticate(SecuredResource.ADMIN) == false) return null;
      
      String startParams = entity.getText();
      JsonNode startJSON = new ObjectMapper().readTree(startParams);
      ArrayNode jobIdsJSON = (ArrayNode) startJSON.get("jobIds");
      for (JsonNode jobId : jobIdsJSON) {
        ActivitiUtil.getManagementService().executeJob(jobId.textValue());
      }
      
      ObjectNode successNode = new ObjectMapper().createObjectNode();
      successNode.put("success", true);
      return successNode;
      
    } catch (Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      } else {
        throw new ActivitiException("Failed to execute jobs", e);
      }
    }
  }
  
  protected Status getAuthenticationFailureStatus() {
    return Status.CLIENT_ERROR_FORBIDDEN;
  }
}
