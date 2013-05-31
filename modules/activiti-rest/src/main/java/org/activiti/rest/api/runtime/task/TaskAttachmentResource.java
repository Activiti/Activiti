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

package org.activiti.rest.api.runtime.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.AttachmentResponse;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;


/**
 * @author Frederik Heremans
 */
public class TaskAttachmentResource extends TaskBaseResource {

  @Get
  public AttachmentResponse getAttachment() {
    if(!authenticate())
      return null;
    
    Task task = getTaskFromRequest();
    
    String attachmentId = getAttribute("attachmentId");
    if(attachmentId == null) {
      throw new ActivitiIllegalArgumentException("AttachmentId is required.");
    }
    
    Attachment attachment = ActivitiUtil.getTaskService().getAttachment(attachmentId);
    if(attachment == null || !task.getId().equals(attachment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Comment.class);
    }
    
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createAttachmentResponse(this, attachment);
  }
  
  @Delete
  public void delegteAttachment() {
    if(!authenticate())
      return;
    
    Task task = getTaskFromRequest();
    
    String attachmentId = getAttribute("attachmentId");
    if(attachmentId == null) {
      throw new ActivitiIllegalArgumentException("AttachmentId is required.");
    }
    
    Attachment attachment = ActivitiUtil.getTaskService().getAttachment(attachmentId);
    if(attachment == null || !task.getId().equals(attachment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Comment.class);
    }
    
    ActivitiUtil.getTaskService().deleteAttachment(attachmentId);
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}
