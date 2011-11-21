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

package org.activiti.rest.api.task;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Attachment;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

/**
 * @author Tijs Rademakers
 */
public class TaskUrlAddResource extends SecuredResource {
  
  @Put
  public AttachmentResponse addUrl(Representation entity) {
    if(authenticate() == false) return null;
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    if(taskId == null || taskId.length() == 0) {
      throw new ActivitiException("No taskId provided");
    }
    
    try {
      String taskParams = entity.getText();
      JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
      
      String name = null;
      if(taskJSON.path("name") != null && taskJSON.path("name").getTextValue() != null) {
        name = taskJSON.path("name").getTextValue();
      }
      
      String description = null;
      if(taskJSON.path("description") != null && taskJSON.path("description").getTextValue() != null) {
        description = taskJSON.path("description").getTextValue();
      }
      
      String url = null;
      if(taskJSON.path("url") != null && taskJSON.path("url").getTextValue() != null) {
        url = taskJSON.path("url").getTextValue();
      }
      
      Attachment attachment = ActivitiUtil.getTaskService().createAttachment(
          "url", taskId, null, name, description, url);
      
      return new AttachmentResponse(attachment);
      
    } catch(Exception e) {
      throw new ActivitiException("Unable to add new attachment to task " + taskId);
    }
  }
}
