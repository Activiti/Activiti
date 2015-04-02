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

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class TaskAttachmentContentResource extends TaskBaseResource {

  @RequestMapping(value="/runtime/tasks/{taskId}/attachments/{attachmentId}/content", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getAttachmentContent(@PathVariable("taskId") String taskId, 
      @PathVariable("attachmentId") String attachmentId, HttpServletResponse response) {
    
    HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
    Attachment attachment = taskService.getAttachment(attachmentId);
    
    if (attachment == null || !task.getId().equals(attachment.getTaskId())) {
      throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Attachment.class);
    }
    
    InputStream attachmentStream = taskService.getAttachmentContent(attachmentId);
    if (attachmentStream == null) {
      throw new ActivitiObjectNotFoundException("Attachment with id '" + attachmentId + 
          "' doesn't have content associated with it.", Attachment.class);
    }
    
    HttpHeaders responseHeaders = new HttpHeaders();
    MediaType mediaType = null;
    if (attachment.getType() != null) {
      try {
        mediaType = MediaType.valueOf(attachment.getType());
        responseHeaders.set("Content-Type", attachment.getType());
      } catch (Exception e) {
        // ignore if unknown media type
      }
    }
    
    if (mediaType == null) {
      responseHeaders.set("Content-Type", "application/octet-stream");
    }
    
    try {
      return new ResponseEntity<byte[]>(IOUtils.toByteArray(attachmentStream), responseHeaders, HttpStatus.OK);
    } catch (Exception e) {
      throw new ActivitiException("Error creating attachment data", e);
    }
  }
}
