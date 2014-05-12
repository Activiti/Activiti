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

package org.activiti.rest.service.api.legacy;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.task.Attachment;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
public class TaskUrlAddResource extends SecuredResource {
  
  @Put
  public AttachmentResponse addUrl(Representation entity) {
    if(authenticate() == false) return null;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    if(taskId == null || taskId.length() == 0) {
      throw new ActivitiIllegalArgumentException("No taskId provided");
    }
    
    try {
      String taskParams = entity.getText();
      JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
      
      String name = null;
      if(taskJSON.path("name") != null && taskJSON.path("name").textValue() != null) {
        name = taskJSON.path("name").textValue();
      }
      
      String description = null;
      if(taskJSON.path("description") != null && taskJSON.path("description").textValue() != null) {
        description = taskJSON.path("description").textValue();
      }
      
      String url = null;
      if(taskJSON.path("url") != null && taskJSON.path("url").textValue() != null) {
        url = taskJSON.path("url").textValue();
      }
      
      Attachment attachment = ActivitiUtil.getTaskService().createAttachment(
          "url", taskId, null, name, description, url);
      
      return new AttachmentResponse(attachment);
      
    } catch(Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      }
      throw new ActivitiException("Unable to add new attachment to task " + taskId);
    }
  }
}
