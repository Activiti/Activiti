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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.engine.AttachmentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskAttachmentResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/attachments/{attachmentId}", method = RequestMethod.GET, produces="application/json")
  public AttachmentResponse getAttachment(@PathVariable("taskId") String taskId, 
      @PathVariable("attachmentId") String attachmentId, HttpServletRequest request) {
    
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    
    Attachment attachment = taskService.getAttachment(attachmentId);
    if (attachment == null || !task.getId().equals(attachment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Comment.class);
    }
    
    return restResponseFactory.createAttachmentResponse(attachment);
  }
  
  @RequestMapping(value="/runtime/tasks/{taskId}/attachments/{attachmentId}", method = RequestMethod.DELETE)
  public void deleteAttachment(@PathVariable("taskId") String taskId, 
      @PathVariable("attachmentId") String attachmentId, HttpServletResponse response) {
    
    Task task = getTaskFromRequest(taskId);
    
    Attachment attachment = taskService.getAttachment(attachmentId);
    if (attachment == null || !task.getId().equals(attachment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Comment.class);
    }
    
    taskService.deleteAttachment(attachmentId);
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}
