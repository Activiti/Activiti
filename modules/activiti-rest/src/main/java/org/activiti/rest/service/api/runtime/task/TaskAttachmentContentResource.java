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

package org.activiti.rest.service.api.runtime.task;

import java.io.InputStream;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.rest.common.api.ActivitiUtil;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskAttachmentContentResource extends TaskBaseResource {

  @Get
  public InputRepresentation getAttachmentContent() {
    if(!authenticate())
      return null;
    
    HistoricTaskInstance task = getHistoricTaskFromRequest();
    
    String attachmentId = getAttribute("attachmentId");
    if(attachmentId == null) {
      throw new ActivitiIllegalArgumentException("AttachmentId is required.");
    }
    
    Attachment attachment = ActivitiUtil.getTaskService().getAttachment(attachmentId);
    
    if(attachment == null || !task.getId().equals(attachment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Attachment.class);
    }
    
    InputStream attachmentStream = ActivitiUtil.getTaskService().getAttachmentContent(attachmentId);
    if(attachmentStream == null) {
      throw new ActivitiObjectNotFoundException("Attachment with id '" + attachmentId + "' doesn't have content associated with it.", Attachment.class);
    }
    
    // Try extracting media-type is type is set and is a valid type
    MediaType type = null;
    if(attachment.getType() != null && MediaType.valueOf(attachment.getType()) != null) {
      type = MediaType.valueOf(attachment.getType());
    }
    
    if(type == null || !type.isConcrete()) {
      type = MediaType.APPLICATION_OCTET_STREAM;
    }
    
    return new InputRepresentation(attachmentStream, type);
  }
}
