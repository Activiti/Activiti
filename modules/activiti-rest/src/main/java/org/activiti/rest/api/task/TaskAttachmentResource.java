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

import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Attachment;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TaskAttachmentResource extends SecuredResource {
  
  @Get
  public InputRepresentation getAttachment() {
    if(authenticate() == false) return null;
    
    String attachmentId = (String) getRequest().getAttributes().get("attachmentId");
    
    if(attachmentId == null) {
      throw new ActivitiException("No attachment id provided");
    }

    Attachment attachment = ActivitiUtil.getTaskService().getAttachment(attachmentId);
    if(attachment == null) {
      throw new ActivitiException("No attachment found for " + attachmentId);
    }
    
    String contentType = attachment.getType();
    MediaType mediatType = MediaType.IMAGE_PNG;
    if(contentType != null) {
      if(contentType.contains(";")) {
        contentType = contentType.substring(0, contentType.indexOf(";"));
      }
      mediatType = MediaType.valueOf(contentType);
    }
    InputStream resource = ActivitiUtil.getTaskService().getAttachmentContent(attachmentId);
    InputRepresentation output = new InputRepresentation(resource, mediatType);
    getResponse().getCacheDirectives().add(CacheDirective.maxAge(28800));
    return output;
  }
  
  @Delete
  public void deleteAttachment(Representation entity) {
    if(authenticate() == false) return;
    String attachmentId = (String) getRequest().getAttributes().get("attachmentId");
    
    if(attachmentId == null) {
      throw new ActivitiException("No attachment id provided");
    }
    
    ActivitiUtil.getTaskService().deleteAttachment(attachmentId);
  }
}
